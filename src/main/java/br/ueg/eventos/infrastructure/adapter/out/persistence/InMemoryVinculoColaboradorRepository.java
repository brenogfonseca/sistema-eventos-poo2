package br.ueg.eventos.infrastructure.adapter.out.persistence;

import br.ueg.eventos.application.port.out.VinculoColaboradorRepositoryPort;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class InMemoryVinculoColaboradorRepository implements VinculoColaboradorRepositoryPort {
    private record Vinculo(Integer usuarioId, Long atividadeId) { }
    private final Set<Vinculo> vinculos = ConcurrentHashMap.newKeySet();

    @Override public void vincular(Integer usuarioId, Long atividadeId) { vinculos.add(new Vinculo(usuarioId, atividadeId)); }
    @Override public void remover(Integer usuarioId, Long atividadeId) { vinculos.remove(new Vinculo(usuarioId, atividadeId)); }
    @Override public boolean estaVinculado(Integer usuarioId, Long atividadeId) { return vinculos.contains(new Vinculo(usuarioId, atividadeId)); }
    @Override public List<Integer> listarUsuariosPorAtividade(Long atividadeId) {
        return vinculos.stream().filter(v -> v.atividadeId().equals(atividadeId)).map(Vinculo::usuarioId).toList();
    }
    @Override public List<Long> listarAtividadesPorUsuario(Integer usuarioId) {
        return vinculos.stream().filter(v -> v.usuarioId().equals(usuarioId)).map(Vinculo::atividadeId).collect(Collectors.toList());
    }
}
