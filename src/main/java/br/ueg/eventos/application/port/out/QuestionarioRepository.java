package br.ueg.eventos.application.port.out;

import br.ueg.eventos.domain.model.Questionario;
import java.util.Optional;

public interface QuestionarioRepository {
    Questionario salvar(Questionario questionario);
    Optional<Questionario> buscarPorId(Long id);
    // Retorna Optional para garantir a regra de 1 questionário por atividade
    Optional<Questionario> buscarPorIdAtividade(Long idAtividade); 
    void excluir(Long id);
}