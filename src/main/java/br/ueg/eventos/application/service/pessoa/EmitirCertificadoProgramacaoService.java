package br.ueg.eventos.application.service.pessoa;

import br.ueg.eventos.application.port.in.pessoa.EmitirCertificadoProgramacaoPort;
import br.ueg.eventos.application.port.out.AtividadeRepositoryPort;
import br.ueg.eventos.application.port.out.CertificadoPdfGeneratorPort;
import br.ueg.eventos.application.port.out.CertificadoRepositoryPort;
import br.ueg.eventos.application.port.out.EventoRepositoryPort;
import br.ueg.eventos.application.port.out.PessoaProgramacaoRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Atividade;
import br.ueg.eventos.domain.model.Evento;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public final class EmitirCertificadoProgramacaoService implements EmitirCertificadoProgramacaoPort {
    private final EventoRepositoryPort eventos;
    private final AtividadeRepositoryPort atividades;
    private final PessoaProgramacaoRepositoryPort pessoas;
    private final CertificadoRepositoryPort certificados;
    private final CertificadoPdfGeneratorPort gerador;
    private final Clock clock;

    public EmitirCertificadoProgramacaoService(EventoRepositoryPort eventos,
                                               AtividadeRepositoryPort atividades,
                                               PessoaProgramacaoRepositoryPort pessoas,
                                               CertificadoRepositoryPort certificados,
                                               CertificadoPdfGeneratorPort gerador) {
        this(eventos, atividades, pessoas, certificados, gerador, Clock.systemUTC());
    }

    EmitirCertificadoProgramacaoService(EventoRepositoryPort eventos,
                                        AtividadeRepositoryPort atividades,
                                        PessoaProgramacaoRepositoryPort pessoas,
                                        CertificadoRepositoryPort certificados,
                                        CertificadoPdfGeneratorPort gerador,
                                        Clock clock) {
        this.eventos = eventos;
        this.atividades = atividades;
        this.pessoas = pessoas;
        this.certificados = certificados;
        this.gerador = gerador;
        this.clock = clock;
    }

    @Override
    public Documento executar(Long idEvento, Long idPessoa, String papel) {
        if (idEvento == null || idEvento <= 0 || idPessoa == null || idPessoa <= 0) {
            throw new RegraNegocioException("Evento e pessoa da programação devem ser válidos.");
        }
        String papelNormalizado = papel == null ? "" : papel.trim().toUpperCase(Locale.ROOT);
        if (!"PALESTRANTE".equals(papelNormalizado) && !"APRESENTADOR".equals(papelNormalizado)) {
            throw new RegraNegocioException("O certificado está disponível para palestrantes e apresentadores.");
        }

        Evento evento = eventos.buscarPorId(idEvento)
                .filter(item -> !item.isDeletado())
                .orElseThrow(() -> new RegraNegocioException("Evento não encontrado."));
        if (!evento.isCertificado()) {
            throw new RegraNegocioException("A emissão de certificados está desabilitada para este evento.");
        }
        ZoneId zona = ZoneId.of(evento.getFusoHorario());
        if (clock.instant().isBefore(evento.getPeriodo().getFim().atZone(zona).toInstant())) {
            throw new RegraNegocioException("O certificado será liberado depois do encerramento do evento.");
        }

        Set<Long> idsAtividades = new LinkedHashSet<>();
        String nomePessoa = null;
        for (PessoaProgramacaoRepositoryPort.VinculoPessoa vinculo : pessoas.listarPorEvento(idEvento)) {
            if (vinculo.pessoa().getId().equals(idPessoa) && papelNormalizado.equals(vinculo.papel())) {
                idsAtividades.add(vinculo.idAtividade());
                nomePessoa = vinculo.pessoa().getNome();
            }
        }
        if (idsAtividades.isEmpty()) {
            throw new RegraNegocioException("A pessoa não está vinculada como "
                    + papelNormalizado.toLowerCase(Locale.ROOT) + " a uma atividade deste evento.");
        }

        long cargaHoraria = 0;
        for (Long idAtividade : idsAtividades) {
            Atividade atividade = atividades.buscarPorId(idAtividade)
                    .orElseThrow(() -> new RegraNegocioException("Atividade da programação não encontrada."));
            LocalDateTime inicio = LocalDateTime.of(atividade.getData(), atividade.getHoraInicio());
            LocalDateTime fim = LocalDateTime.of(atividade.getData(), atividade.getHoraFim());
            if (fim.isBefore(inicio)) fim = fim.plusDays(1);
            cargaHoraria += Math.max(0, ChronoUnit.MINUTES.between(inicio, fim));
        }
        if (cargaHoraria <= 0 || cargaHoraria > Integer.MAX_VALUE) {
            throw new RegraNegocioException("A carga horária da programação não pode ser emitida.");
        }

        LocalDateTime emitidoEm = LocalDateTime.ofInstant(clock.instant(), zona);
        String nomeArquivo = "certificado-evento-" + idEvento + "-pessoa-" + idPessoa
                + "-" + papelNormalizado.toLowerCase(Locale.ROOT) + ".pdf";
        byte[] pdf = gerador.gerarProgramacao(nomePessoa, evento.getTitulo(), papelNormalizado,
                (int) cargaHoraria, emitidoEm);
        certificados.registrarPessoaProgramacao(idEvento, idPessoa, papelNormalizado,
                (int) cargaHoraria, nomeArquivo);
        return new Documento(nomeArquivo, pdf);
    }
}
