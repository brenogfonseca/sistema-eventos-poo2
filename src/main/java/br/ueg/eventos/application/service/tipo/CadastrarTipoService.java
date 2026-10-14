package br.ueg.eventos.application.service.tipo;

import br.ueg.eventos.application.port.in.tipo.CadastrarTipoPort;
import br.ueg.eventos.application.port.out.TipoRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Tipo;

/** Serviço de cadastro de tipo. */
public class CadastrarTipoService implements CadastrarTipoPort {

    private final TipoRepositoryPort tipoRepository;

    public CadastrarTipoService(TipoRepositoryPort tipoRepository) {
        this.tipoRepository = tipoRepository;
    }

    @Override
    public Tipo executar(ComandoCadastrarTipo comando) {
        // Verifica se o nome foi informado.
        if (comando == null || comando.nome == null || comando.nome.isBlank()) {
            throw new RegraNegocioException("O nome do tipo é obrigatório.");
        }

        return tipoRepository.salvar(new Tipo(comando.nome.trim()));
    }
}