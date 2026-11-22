package br.ueg.eventos.application.service.inscricao;

import br.ueg.eventos.application.port.in.inscricao.CalcularFrequenciaCertificadoPort;
import br.ueg.eventos.application.port.in.inscricao.EmitirCertificadoPort;
import br.ueg.eventos.application.port.out.CertificadoPdfGeneratorPort;
import br.ueg.eventos.application.port.out.CertificadoRepositoryPort;
import br.ueg.eventos.application.port.out.EventoRepositoryPort;
import br.ueg.eventos.application.port.out.InscricaoEventoRepositoryPort;
import br.ueg.eventos.application.port.out.UsuarioRepositoryPort;
import br.ueg.eventos.domain.exception.AcessoNegadoException;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Evento;
import br.ueg.eventos.domain.model.InscricaoEvento;
import br.ueg.eventos.domain.model.Usuario;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class EmitirCertificadoService implements EmitirCertificadoPort {
    private final InscricaoEventoRepositoryPort inscricoes;
    private final EventoRepositoryPort eventos;
    private final UsuarioRepositoryPort usuarios;
    private final CalcularFrequenciaCertificadoPort frequencia;
    private final CertificadoRepositoryPort certificados;
    private final CertificadoPdfGeneratorPort gerador;
    private final Clock clock;

    public EmitirCertificadoService(InscricaoEventoRepositoryPort inscricoes,
                                    EventoRepositoryPort eventos,
                                    UsuarioRepositoryPort usuarios,
                                    CalcularFrequenciaCertificadoPort frequencia,
                                    CertificadoRepositoryPort certificados,
                                    CertificadoPdfGeneratorPort gerador) {
        this(inscricoes, eventos, usuarios, frequencia, certificados, gerador, Clock.systemUTC());
    }

    EmitirCertificadoService(InscricaoEventoRepositoryPort inscricoes,
                             EventoRepositoryPort eventos,
                             UsuarioRepositoryPort usuarios,
                             CalcularFrequenciaCertificadoPort frequencia,
                             CertificadoRepositoryPort certificados,
                             CertificadoPdfGeneratorPort gerador,
                             Clock clock) {
        this.inscricoes = inscricoes;
        this.eventos = eventos;
        this.usuarios = usuarios;
        this.frequencia = frequencia;
        this.certificados = certificados;
        this.gerador = gerador;
        this.clock = clock;
    }

    @Override
    public Documento executar(Long idInscricaoEvento, Long idUsuario) {
        if (idInscricaoEvento == null || idInscricaoEvento <= 0 || idUsuario == null || idUsuario <= 0) {
            throw new RegraNegocioException("Inscrição e participante devem ser válidos.");
        }
        InscricaoEvento inscricao = inscricoes.buscarPorId(idInscricaoEvento)
                .orElseThrow(() -> new RegraNegocioException("Inscrição no evento não encontrada."));
        if (!inscricao.getIdUsuario().equals(idUsuario)) {
            throw new AcessoNegadoException("O certificado só pode ser emitido para a conta inscrita.");
        }
        if (!inscricao.estaAtiva()) throw new RegraNegocioException("A inscrição no evento está cancelada.");
        Evento evento = eventos.buscarPorId(inscricao.getIdEvento())
                .orElseThrow(() -> new RegraNegocioException("Evento não encontrado."));
        ZoneId zona = ZoneId.of(evento.getFusoHorario());
        if (clock.instant().isBefore(evento.getPeriodo().getFim().atZone(zona).toInstant())) {
            throw new RegraNegocioException("O certificado será liberado depois do encerramento do evento.");
        }
        CalcularFrequenciaCertificadoPort.Resultado resultado = frequencia.executar(idInscricaoEvento, idUsuario);
        if (!resultado.certificadoHabilitado || !resultado.elegivel) {
            throw new RegraNegocioException("A frequência ainda não atingiu o mínimo necessário para emitir o certificado.");
        }
        if (resultado.cargaHorariaCertificadoMinutos <= 0 || resultado.cargaHorariaCertificadoMinutos > Integer.MAX_VALUE) {
            throw new RegraNegocioException("A carga horária registrada não pode ser emitida.");
        }
        Usuario participante = usuarios.buscarPorId(Math.toIntExact(idUsuario))
                .orElseThrow(() -> new RegraNegocioException("Participante não encontrado."));
        LocalDateTime emitidoEm = LocalDateTime.ofInstant(clock.instant(), zona);
        String nomeArquivo = "certificado-evento-" + evento.getId() + "-usuario-" + idUsuario + ".pdf";
        String resumoFrequencia = "PERCENTUAL".equals(resultado.criterioFrequencia)
                ? String.format(java.util.Locale.forLanguageTag("pt-BR"), "Frequência de %.2f%% (mínimo %d%%).",
                        resultado.percentualFrequencia, resultado.frequenciaMinima)
                : "Presença de " + formatarHoras(resultado.minutosPresentes) + " (mínimo "
                        + formatarHoras(resultado.frequenciaMinimaMinutos) + ").";
        int cargaCertificado = Math.toIntExact(resultado.cargaHorariaCertificadoMinutos);
        byte[] pdf = gerador.gerarParticipante(participante.getNome(), evento.getTitulo(),
                resumoFrequencia, cargaCertificado, emitidoEm);
        certificados.registrar(evento.getId(), idUsuario, cargaCertificado, nomeArquivo);
        return new Documento(nomeArquivo, pdf);
    }

    private String formatarHoras(long minutos) {
        long horas = minutos / 60;
        long resto = minutos % 60;
        return resto == 0 ? horas + " h" : horas + " h " + resto + " min";
    }
}
