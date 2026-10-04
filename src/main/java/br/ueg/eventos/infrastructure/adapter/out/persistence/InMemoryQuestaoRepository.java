package br.ueg.eventos.infrastructure.adapter.out.persistence;

import br.ueg.eventos.application.port.out.QuestaoRepositoryPort;
import br.ueg.eventos.domain.model.Questao;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Adaptador de saída responsável por armazenar questões em memória.
 * Implementa a porta de repositório usada pela camada de aplicação.
 */
public class InMemoryQuestaoRepository implements QuestaoRepositoryPort {

    private final Map<Long, Questao> questoes = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    /**
     * Salva uma questão. Se ela ainda não tiver ID, gera um novo.
     */
    @Override
    public Questao salvar(Questao questao) {
        if (questao == null) {
            throw new IllegalArgumentException(
                    "A questão não pode ser nula."
            );
        }

        Integer id = questao.getId();

        if (id == null) {
            // A entidade usa Integer para o ID; o gerador mantém a sequência como Long.
            id = Math.toIntExact(idGenerator.getAndIncrement());

            // Reconstrói a entidade com o ID gerado.
            questao = new Questao(
                    id,
                    questao.getEnunciado(),
                    questao.getTipo(),
                    questao.getIdQuestionario()
            );
        } else {
            // Avança o gerador para evitar reutilizar um ID já informado.
            long idSalvo = id.longValue();
            idGenerator.updateAndGet(atual -> Math.max(atual, idSalvo + 1));
        }

        questoes.put(id.longValue(), questao);
        return questao;
    }

    /**
     * Busca uma questão pelo seu ID.
     */
    @Override
    public Optional<Questao> buscarPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(questoes.get(id));
    }

    /**
     * Lista as questões vinculadas ao questionário informado.
     */
    @Override
    public List<Questao> buscarPorIdQuestionario(Long idQuestionario) {
        if (idQuestionario == null) {
            return List.of();
        }

        return questoes.values()
                .stream()
                .filter(questao ->
                        idQuestionario.equals(
                                questao.getIdQuestionario().longValue()
                        )
                )
                .toList();
    }

    /**
     * Remove uma questão pelo seu ID.
     */
    @Override
    public void excluir(Long id) {
        if (id != null) {
            questoes.remove(id);
        }
    }
}