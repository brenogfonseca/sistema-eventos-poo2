package br.ueg.eventos.infrastructure.adapter.out.persistence;

import br.ueg.eventos.application.port.out.QuestionarioRepositoryPort;
import br.ueg.eventos.domain.model.Questionario;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Adaptador de saída responsável por armazenar questionários em memória.
 * Implementa a porta de repositório usada pela camada de aplicação.
 */
public class InMemoryQuestionarioRepository
        implements QuestionarioRepositoryPort {

    private final Map<Long, Questionario> questionarios =
            new ConcurrentHashMap<>();

    private final AtomicLong idGenerator = new AtomicLong(1);

    /**
     * Salva um questionário. Se ele ainda não tiver ID, gera um novo.
     */
    @Override
    public Questionario salvar(Questionario questionario) {
        if (questionario == null) {
            throw new IllegalArgumentException(
                    "O questionário não pode ser nulo."
            );
        }

        Integer id = questionario.getId();

        if (id == null) {
            // A entidade usa Integer para o ID; o gerador mantém a sequência como Long.
            id = Math.toIntExact(idGenerator.getAndIncrement());

            // Reconstrói a entidade com o ID gerado.
            questionario = new Questionario(
                    id,
                    questionario.getIdEvento(),
                    questionario.getTitulo()
            );
        } else {
            // Avança o gerador para evitar reutilizar um ID já informado.
            long idSalvo = id.longValue();
            idGenerator.updateAndGet(atual -> Math.max(atual, idSalvo + 1));
        }

        questionarios.put(id.longValue(), questionario);
        return questionario;
    }

    /**
     * Busca um questionário pelo seu ID.
     */
    @Override
    public Optional<Questionario> buscarPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(questionarios.get(id));
    }

    /**
     * Busca o questionário associado ao ID recebido.
     * No modelo atual, o vínculo está armazenado como idEvento.
     */
    @Override
    public Optional<Questionario> buscarPorIdAtividade(Long idAtividade) {
        if (idAtividade == null) {
            return Optional.empty();
        }

        return questionarios.values()
                .stream()
                .filter(questionario ->
                        idAtividade.equals(
                                questionario.getIdEvento().longValue()
                        )
                )
                .findFirst();
    }

    /**
     * Remove um questionário pelo seu ID.
     */
    @Override
    public void excluir(Long id) {
        if (id != null) {
            questionarios.remove(id);
        }
    }
}