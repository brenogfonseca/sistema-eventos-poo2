package br.ueg.eventos.application.port.out;

import br.ueg.eventos.domain.model.Inscricao;
import java.util.List;
import java.util.Optional;

public interface InscricaoRepositoryPort {

    Inscricao salvar(Inscricao inscricao);

    Optional<Inscricao> buscarPorId(Long id);

    List<Inscricao> listarPorAtividade(Long idAtividade);

    List<Inscricao> listarPorUsuario(Long idUsuario);

    void excluir(Long id);
}