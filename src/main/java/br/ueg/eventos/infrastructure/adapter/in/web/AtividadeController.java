package br.ueg.eventos.infrastructure.adapter.in.web;

import br.ueg.eventos.application.port.in.atividade.*;
import br.ueg.eventos.application.port.out.LocalRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Atividade;
import br.ueg.eventos.domain.model.Local;
import br.ueg.eventos.domain.model.TipoFrequenciaEnum;
import br.ueg.eventos.domain.model.Trilha;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

public class AtividadeController {

    private final CadastrarAtividadePort cadastrarAtividadePort;
    private final AtualizarAtividadePort atualizarAtividadePort;
    private final ExcluirAtividadePort excluirAtividadePort;
    private final BuscarAtividadePorIdPort buscarAtividadePorIdPort;
    private final ListarAtividadesPorEventoPort listarAtividadesPorEventoPort;
    private final LocalRepositoryPort localRepository;

    public AtividadeController(
            CadastrarAtividadePort cadastrarAtividadePort,
            AtualizarAtividadePort atualizarAtividadePort,
            ExcluirAtividadePort excluirAtividadePort,
            BuscarAtividadePorIdPort buscarAtividadePorIdPort,
            ListarAtividadesPorEventoPort listarAtividadesPorEventoPort) {
        this(cadastrarAtividadePort, atualizarAtividadePort, excluirAtividadePort,
                buscarAtividadePorIdPort, listarAtividadesPorEventoPort, null);
    }

    public AtividadeController(
            CadastrarAtividadePort cadastrarAtividadePort,
            AtualizarAtividadePort atualizarAtividadePort,
            ExcluirAtividadePort excluirAtividadePort,
            BuscarAtividadePorIdPort buscarAtividadePorIdPort,
            ListarAtividadesPorEventoPort listarAtividadesPorEventoPort,
            LocalRepositoryPort localRepository) {

        this.cadastrarAtividadePort = cadastrarAtividadePort;
        this.atualizarAtividadePort = atualizarAtividadePort;
        this.excluirAtividadePort = excluirAtividadePort;
        this.buscarAtividadePorIdPort = buscarAtividadePorIdPort;
        this.listarAtividadesPorEventoPort = listarAtividadesPorEventoPort;
        this.localRepository = localRepository;
    }

    public void registerRoutes(Javalin app) {
        app.post("/atividades", this::cadastrar);
        app.put("/atividades/{id}", this::atualizar);
        app.delete("/atividades/{id}", this::excluir);
        app.get("/atividades/{id}", this::buscarPorId);
        app.get("/atividades/evento/{idEvento}",
                this::listarPorEvento);
    }

    /**
     * DTO de entrada para Local, desacoplado da entidade de domínio.
     */
    public static class LocalDto {
        public Long id;
        public String nome;

        public LocalDto() {
        }

        public LocalDto(String nome) {
            this.nome = nome;
        }

        public LocalDto(Long id, String nome) {
            this.id = id;
            this.nome = nome;
        }
    }

    /**
     * DTO de entrada para Trilha, desacoplado da entidade de domínio.
     */
    public static class TrilhaDto {
        public Long id;
        public String nome;

        public TrilhaDto() {
        }

        public TrilhaDto(String nome) {
            this.nome = nome;
        }

        public TrilhaDto(Long id, String nome) {
            this.id = id;
            this.nome = nome;
        }
    }

    /**
     * DTO de requisição para cadastro e atualização de atividades.
     */
    public static class AtividadeRequest {
        public String nome;
        public Long eventoId;
        public LocalDto local;
        public String nomeLocal;
        public String horaInicio;
        public String horaFim;
        public String data;
        public boolean controlaVagas;
        public int vagas;
        public String tipo;
        public String tipoFrequencia;
        public TrilhaDto trilha;

        public AtividadeRequest() {
        }
    }

    private Local extrairLocal(AtividadeRequest request) {
        String nome = null;
        Long id = null;

        if (request.local != null) {
            id = request.local.id;
            nome = request.local.nome;
        }

        if ((nome == null || nome.isBlank()) && request.nomeLocal != null && !request.nomeLocal.isBlank()) {
            nome = request.nomeLocal;
        }

        if (nome != null) {
            nome = nome.trim();
        }

        if (nome == null || nome.isEmpty()) {
            return null;
        }

        if (id != null && localRepository != null) {
            try {
                return localRepository.buscarPorId(id)
                        .orElse(new Local(id, nome));
            } catch (Exception ignored) {
                return new Local(id, nome);
            }
        }

        if (id != null) {
            return new Local(id, nome);
        }

        return new Local(nome);
    }

    private TipoFrequenciaEnum parseTipoFrequencia(String valor) {
        if (valor == null || valor.isBlank()) {
            return TipoFrequenciaEnum.CHECKIN;
        }
        try {
            return TipoFrequenciaEnum.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return TipoFrequenciaEnum.CHECKIN;
        }
    }

    private Trilha extrairTrilha(AtividadeRequest request) {
        if (request.trilha != null && request.trilha.nome != null && !request.trilha.nome.isBlank()) {
            if (request.trilha.id != null) {
                return new Trilha(request.trilha.id, request.trilha.nome.trim());
            }
            return new Trilha(request.trilha.nome.trim());
        }
        return null;
    }

    private Atividade converter(AtividadeRequest request, Long id) {
        if (request == null) {
            throw new RegraNegocioException("Os dados da atividade são obrigatórios.");
        }
        if (request.nome == null || request.nome.trim().isEmpty()) {
            throw new RegraNegocioException("O nome da atividade é obrigatório.");
        }
        if (request.eventoId == null || request.eventoId <= 0) {
            throw new RegraNegocioException("O ID do evento é obrigatório.");
        }
        if (request.data == null || request.data.isBlank()) {
            throw new RegraNegocioException("A data da atividade é obrigatória.");
        }
        if (request.horaInicio == null || request.horaInicio.isBlank()) {
            throw new RegraNegocioException("O horário de início da atividade é obrigatório.");
        }
        if (request.horaFim == null || request.horaFim.isBlank()) {
            throw new RegraNegocioException("O horário de término da atividade é obrigatório.");
        }

        LocalDate data;
        try {
            data = LocalDate.parse(request.data.trim());
        } catch (Exception e) {
            throw new RegraNegocioException("Data inválida. Use o formato AAAA-MM-DD.");
        }

        LocalTime horaInicio;
        try {
            String h = request.horaInicio.trim();
            if (h.length() == 5)
                h += ":00";
            horaInicio = LocalTime.parse(h);
        } catch (Exception e) {
            throw new RegraNegocioException("Horário de início inválido. Use o formato HH:MM.");
        }

        LocalTime horaFim;
        try {
            String h = request.horaFim.trim();
            if (h.length() == 5)
                h += ":00";
            horaFim = LocalTime.parse(h);
        } catch (Exception e) {
            throw new RegraNegocioException("Horário de término inválido. Use o formato HH:MM.");
        }

        if (horaInicio.isAfter(horaFim)) {
            throw new RegraNegocioException("O horário de início não pode ser posterior ao horário de término.");
        }

        Local local = extrairLocal(request);
        Trilha trilha = extrairTrilha(request);

        return new Atividade(
                id,
                request.nome.trim(),
                request.eventoId,
                local,
                horaInicio,
                horaFim,
                data,
                request.controlaVagas,
                request.vagas,
                request.tipo,
                parseTipoFrequencia(request.tipoFrequencia),
                trilha);
    }

    private void cadastrar(Context ctx) {
        try {
            AtividadeRequest request = ctx.bodyAsClass(AtividadeRequest.class);

            Atividade atividade = converter(request, null);

            Atividade cadastrada = cadastrarAtividadePort.executar(atividade);

            ctx.status(HttpStatus.CREATED).json(cadastrada);

        } catch (RegraNegocioException e) {
            throw e;
        } catch (java.time.format.DateTimeParseException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(Map.of("erro", "Data ou horário inválido."));
        } catch (Exception e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(Map.of("erro", "Erro ao processar dados da atividade: " + e.getMessage()));
        }
    }

    private void atualizar(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));

            AtividadeRequest request = ctx.bodyAsClass(AtividadeRequest.class);

            Atividade atividade = converter(request, id);

            Atividade atualizada = atualizarAtividadePort.executar(id, atividade);

            ctx.status(HttpStatus.OK).json(atualizada);

        } catch (RegraNegocioException e) {
            throw e;
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(Map.of("erro", "O id da atividade deve ser válido."));
        } catch (java.time.format.DateTimeParseException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(Map.of("erro", "Data ou horário inválido."));
        } catch (Exception e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(Map.of("erro", "Erro ao atualizar atividade: " + e.getMessage()));
        }
    }

    private void excluir(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));

            excluirAtividadePort.executar(id);

            ctx.status(HttpStatus.NO_CONTENT);

        } catch (RegraNegocioException e) {
            throw e;
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(Map.of("erro", "O id da atividade deve ser válido."));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(Map.of("erro", "Erro interno: " + e.getMessage()));
        }
    }

    private void buscarPorId(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));

            Atividade atividade = buscarAtividadePorIdPort.executar(id);

            ctx.status(HttpStatus.OK).json(atividade);

        } catch (RegraNegocioException e) {
            throw e;
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(Map.of("erro", "O id da atividade deve ser válido."));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(Map.of("erro", "Erro interno: " + e.getMessage()));
        }
    }

    private void listarPorEvento(Context ctx) {
        try {
            Long idEvento = Long.parseLong(ctx.pathParam("idEvento"));

            ctx.status(HttpStatus.OK)
                    .json(listarAtividadesPorEventoPort.executar(idEvento));

        } catch (RegraNegocioException e) {
            throw e;
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(Map.of("erro", "O id do evento deve ser válido."));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(Map.of("erro", "Erro interno: " + e.getMessage()));
        }
    }
}
