package br.ueg.eventos.application.port.out;

import br.ueg.eventos.domain.model.PessoaProgramacao;
import java.util.List;

public interface PessoaProgramacaoRepositoryPort {
    PessoaProgramacao vincular(Long idAtividade, PessoaProgramacao pessoa, String papel);
    List<VinculoPessoa> listarPorAtividade(Long idAtividade);
    List<VinculoPessoa> listarPorEvento(Long idEvento);

    record VinculoPessoa(Long idAtividade, String papel, PessoaProgramacao pessoa) { }
}
