package br.ueg.eventos.application.port.in.pessoa;

import br.ueg.eventos.application.port.out.PessoaProgramacaoRepositoryPort;
import br.ueg.eventos.domain.model.PessoaProgramacao;
import java.util.List;

public interface VincularPessoaProgramacaoPort {
    PessoaProgramacao executar(Long idAtividade, PessoaProgramacao pessoa, String papel, Integer idExecutor);
    List<PessoaProgramacaoRepositoryPort.VinculoPessoa> listar(Long idAtividade);
    List<PessoaProgramacaoRepositoryPort.VinculoPessoa> listarPorEvento(Long idEvento);
}
