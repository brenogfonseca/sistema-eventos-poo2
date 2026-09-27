package br.ueg.eventos.application.service.frequencia;

import br.ueg.eventos.application.port.in.frequencia.RegistrarFrequenciaPort;
import br.ueg.eventos.application.port.out.FrequenciaRepositoryPort;
import br.ueg.eventos.application.port.out.InscricaoRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Frequencia;
import br.ueg.eventos.domain.model.Inscricao;
import br.ueg.eventos.domain.model.TipoFrequenciaEnum;

public class RegistrarFrequenciaService implements RegistrarFrequenciaPort {

    private final FrequenciaRepositoryPort frequenciaRepository;
    private final InscricaoRepositoryPort inscricaoRepository;

    public RegistrarFrequenciaService(
            FrequenciaRepositoryPort frequenciaRepository,
            InscricaoRepositoryPort inscricaoRepository
    ) {
        this.frequenciaRepository = frequenciaRepository;
        this.inscricaoRepository = inscricaoRepository;
    }

    @Override
    public Frequencia executar(ComandoRegistrarFrequencia comando) {
        if (comando == null) {
            throw new RegraNegocioException(
                    "Os dados da frequência são obrigatórios."
            );
        }

        int idInscricao = converterIdInscricao(comando.idInscricao);

        Inscricao inscricao = inscricaoRepository
                .buscarPorId(comando.idInscricao)
                .orElseThrow(() -> new RegraNegocioException(
                        "Inscrição não encontrada."
                ));

        if (!inscricao.estaAtiva()) {
            throw new RegraNegocioException(
                    "Não é possível registrar frequência em uma inscrição cancelada."
            );
        }

        if (comando.tipo == null || comando.tipo.isBlank()) {
            throw new RegraNegocioException(
                    "O tipo da frequência é obrigatório."
            );
        }

        TipoFrequenciaEnum tipo;
        try {
            tipo = TipoFrequenciaEnum.valueOf(
                    comando.tipo.trim().toUpperCase()
            );
        } catch (IllegalArgumentException e) {
            throw new RegraNegocioException(
                    "Tipo de frequência inválido. Use CHECKIN, ENTRADA_SAIDA ou MANUAL."
            );
        }

        Frequencia frequencia = new Frequencia(
                comando.dataHora,
                comando.origem,
                comando.idResponsavel,
                comando.presente,
                idInscricao,
                tipo.name()
        );

        return frequenciaRepository.salvar(frequencia);
    }

    private int converterIdInscricao(Long idInscricao) {
        if (idInscricao == null || idInscricao <= 0) {
            throw new RegraNegocioException(
                    "O id da inscrição deve ser válido."
            );
        }

        try {
            return Math.toIntExact(idInscricao);
        } catch (ArithmeticException e) {
            throw new RegraNegocioException(
                    "O id da inscrição excede o limite aceito pelo modelo de frequência."
            );
        }
    }
}