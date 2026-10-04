package br.ueg.eventos.infrastructure.adapter.out.persistence;

import br.ueg.eventos.application.port.out.RespostaRepositoryPort;
import br.ueg.eventos.domain.model.Resposta;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Adaptador de saída responsável por armazenar respostas em memória.
 * Implementa a porta de repositório usada pela camada de aplicação.
 */
public class InMemoryRespostaRepository implements RespostaRepositoryPort {

    private final Map<Long, Resposta> respostas = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    /**
     * Salva uma resposta. Se ela ainda não tiver ID, gera um novo.
     */
    @Override
    public Resposta salvar(Resposta resposta) {
        if (resposta == null) {
            throw new IllegalArgumentException(
                    "A resposta não pode ser nula."
            );
        }

        Integer id = resposta.getId();

        if (id == null) {
            // A entidade usa Integer para o ID; o gerador mantém a sequência como Long.
            id = Math.toIntExact(idGenerator.getAndIncrement());

            // Reconstrói a entidade com o ID gerado.
            resposta = new Resposta(
                    id,
                    resposta.getIdQuestao(),
                    resposta.getIdUsuario(),
                    resposta.getValor()
            );
        } else {
            // Avança o gerador para evitar reutilizar um ID já informado.
            long idSalvo = id.longValue();
            idGenerator.updateAndGet(atual -> Math.max(atual, idSalvo + 1));
        }

        respostas.put(id.longValue(), resposta);
        return resposta;
    }

    /**
     * Busca uma resposta pelo seu ID.
     */
    @Override
    public Optional<Resposta> buscarPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(respostas.get(id));
    }

    /**
     * Lista as respostas vinculadas à questão informada.
     */
    @Override
    public List<Resposta> buscarPorIdQuestao(Long idQuestao) {
        if (idQuestao == null) {
            return List.of();
        }

        return respostas.values()
                .stream()
                .filter(resposta ->
                        idQuestao.equals(resposta.getIdQuestao().longValue())
                )
                .toList();
    }

    /**
     * Remove uma resposta pelo seu ID.
     */
    @Override
    public void excluir(Long id) {
        if (id != null) {
            respostas.remove(id);
        }
    }
}