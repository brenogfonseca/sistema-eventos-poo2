package br.ueg.eventos.application.service.tipo;

import br.ueg.eventos.application.port.in.tipo.AlterarTipoPort;
import br.ueg.eventos.application.port.out.TipoRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Tipo;

/** Serviço de alteração de tipo. */
public class AlterarTipoService implements AlterarTipoPort {

    private final TipoRepositoryPort tipoRepository;

    public AlterarTipoService(TipoRepositoryPort tipoRepository) {
        this.tipoRepository = tipoRepository;
    }

    @Override
    public Tipo executar(ComandoAlterarTipo comando) {
        if (comando == null || comando.id == null || comando.id <= 0) {
            throw new RegraNegocioException("ID do tipo inválido.");
        }
        if (comando.nome == null || comando.nome.isBlank()) {
            throw new RegraNegocioException("O nome do tipo é obrigatório.");
        }

        // Confirma que o tipo existe antes de alterá-lo.
        Tipo tipo = tipoRepository.buscarPorId(comando.id)
                .orElseThrow(() ->
                        new RegraNegocioException("Tipo não encontrado."));

        tipo.setNome(comando.nome.trim());
        return tipoRepository.salvar(tipo);
    }
}