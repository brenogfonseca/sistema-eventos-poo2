package br.ueg.eventos.infrastructure.adapter.in.web;

import br.ueg.eventos.application.port.in.evento.AlterarEventoPort;
import br.ueg.eventos.application.port.in.evento.AlterarEventoPort.ComandoAlterarEvento;
import br.ueg.eventos.application.port.in.evento.BuscarEventoPorIdPort;
import br.ueg.eventos.application.port.in.evento.BuscarVinculoOrganizadorPort;
import br.ueg.eventos.application.port.in.evento.CriarEventoPort;
import br.ueg.eventos.application.port.in.evento.CriarEventoPort.ComandoCriarEvento;
import br.ueg.eventos.application.port.in.evento.ExcluirEventoPort;
import br.ueg.eventos.application.port.in.evento.ExcluirEventoPort.ComandoExcluirEvento;
import br.ueg.eventos.application.port.in.evento.ListarEventosPort;
import br.ueg.eventos.application.port.out.UsuarioRepositoryPort;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Evento;
import br.ueg.eventos.domain.model.Usuario;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador de entrada Web (REST) para o gerenciamento de Eventos.
 * Fornece endpoints para criar, listar, buscar por ID, atualizar e excluir eventos,
 * integrando a segurança baseada no usuário executor e vínculos de organizador.
 */
public class EventoController {

    private static final String HEADER_EXECUTOR_ID = "X-Usuario-Id";

    private final CriarEventoPort criarEventoPort;
    private final ListarEventosPort listarEventosPort;
    private final AlterarEventoPort alterarEventoPort;
    private final ExcluirEventoPort excluirEventoPort;
    private final BuscarEventoPorIdPort buscarEventoPorIdPort;
    private final BuscarVinculoOrganizadorPort buscarVinculoOrganizadorPort;
    private final UsuarioRepositoryPort usuarioRepository;

    public EventoController(CriarEventoPort criarEventoPort,
                            ListarEventosPort listarEventosPort,
                            AlterarEventoPort alterarEventoPort,
                            ExcluirEventoPort excluirEventoPort,
                            BuscarEventoPorIdPort buscarEventoPorIdPort,
                            BuscarVinculoOrganizadorPort buscarVinculoOrganizadorPort,
                            UsuarioRepositoryPort usuarioRepository) {
        this.criarEventoPort = criarEventoPort;
        this.listarEventosPort = listarEventosPort;
        this.alterarEventoPort = alterarEventoPort;
        this.excluirEventoPort = excluirEventoPort;
        this.buscarEventoPorIdPort = buscarEventoPorIdPort;
        this.buscarVinculoOrganizadorPort = buscarVinculoOrganizadorPort;
        this.usuarioRepository = usuarioRepository;
    }

    public void registerRoutes(Javalin app) {
        app.post("/eventos", this::criarEvento);
        app.get("/eventos", this::listarEventos);
        app.get("/eventos/{id}", this::buscarEventoPorId);
        app.put("/eventos/{id}", this::alterarEvento);
        app.delete("/eventos/{id}", this::excluirEvento);
        app.get("/eventos/{id}/organizadores", this::listarOrganizadoresDoEvento);
    }

    // DTO de requisição para criação e alteração de eventos
    static class SalvarEventoRequest {
        public String titulo;
        public String descricao;
        public String inicio;
        public String fim;
        public int capacidade;
        public Integer usuarioExecutorId; // Permite envio no corpo como alternativa ao header
    }

    // DTO de resposta detalhada de Evento incluindo atributos do domínio
    public static class EventoDetalheResponse {
        public Long id;
        public String titulo;
        public String descricao;
        public String inicio;
        public String fim;
        public int capacidade;
        public boolean inscricoesAbertas;
        public boolean certificado;
        public int frequenciaMinima;
        public List<Integer> organizadoresIds; // Lista de IDs de organizadores

        public EventoDetalheResponse(Evento evento, List<Integer> organizadoresIds) {
            this.id = evento.getId();
            this.titulo = evento.getTitulo();
            this.descricao = evento.getDescricao();
            this.inicio = evento.getPeriodo() != null && evento.getPeriodo().getInicio() != null ? evento.getPeriodo().getInicio().toString() : null;
            this.fim = evento.getPeriodo() != null && evento.getPeriodo().getFim() != null ? evento.getPeriodo().getFim().toString() : null;
            this.capacidade = evento.getCapacidadeMaxima();
            this.inscricoesAbertas = evento.isInscricoesAbertas();
            this.certificado = evento.isCertificado();
            this.frequenciaMinima = evento.getFrequenciaMinima();
            this.organizadoresIds = organizadoresIds != null ? organizadoresIds : new ArrayList<>();
        }
    }

    public static class ErroResponse {
        public int status;
        public String erro;
        public String mensagem;

        public ErroResponse(int status, String erro, String mensagem) {
            this.status = status;
            this.erro = erro;
            this.mensagem = mensagem;
        }
    }

    private void listarEventos(Context ctx) {
        try {
            List<Evento> eventos = listarEventosPort.executar();
            List<EventoDetalheResponse> resposta = new ArrayList<>();

            for (Evento ev : eventos) {
                List<Integer> organizadores = new ArrayList<>();
                if (buscarVinculoOrganizadorPort != null && ev.getId() != null) {
                    buscarVinculoOrganizadorPort.listarPorEvento(ev.getId().intValue())
                            .forEach(v -> organizadores.add(v.getUsuarioId()));
                }
                resposta.add(new EventoDetalheResponse(ev, organizadores));
            }

            ctx.status(HttpStatus.OK).json(resposta);
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(new ErroResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.getCode(),
                    "Erro interno",
                    "Erro interno: " + e.getMessage()
            ));
        }
    }

    private void buscarEventoPorId(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));
            Evento evento = buscarEventoPorIdPort.executar(id)
                    .orElseThrow(() -> new RegraNegocioException("Evento não encontrado com o ID: " + id));

            List<Integer> organizadores = new ArrayList<>();
            if (buscarVinculoOrganizadorPort != null) {
                buscarVinculoOrganizadorPort.listarPorEvento(id.intValue())
                        .forEach(v -> organizadores.add(v.getUsuarioId()));
            }

            ctx.status(HttpStatus.OK).json(new EventoDetalheResponse(evento, organizadores));
        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.NOT_FOUND).result(e.getMessage());
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result("O id do evento deve ser numérico.");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno: " + e.getMessage());
        }
    }

    private void criarEvento(Context ctx) {
        try {
            SalvarEventoRequest request = ctx.bodyAsClass(SalvarEventoRequest.class);

            // Identifica o usuário criador via header ou corpo (opcional para criação, mas recomendado)
            Integer idCriador = extrairIdUsuario(ctx, request != null ? request.usuarioExecutorId : null, false);

            ComandoCriarEvento comando = new ComandoCriarEvento(
                    request.titulo,
                    request.descricao,
                    LocalDateTime.parse(request.inicio),
                    LocalDateTime.parse(request.fim),
                    request.capacidade,
                    idCriador);

            Evento eventoCriado = criarEventoPort.executar(comando);

            List<Integer> organizadores = new ArrayList<>();
            if (idCriador != null) {
                organizadores.add(idCriador);
            }

            ctx.status(HttpStatus.CREATED).json(new EventoDetalheResponse(eventoCriado, organizadores));

        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno: " + e.getMessage());
        }
    }

    private void alterarEvento(Context ctx) {
        try {
            Long idEvento = Long.parseLong(ctx.pathParam("id"));
            SalvarEventoRequest request = ctx.bodyAsClass(SalvarEventoRequest.class);

            // Extrai o ID do executor (obrigatório para alteração)
            Integer idExecutor = extrairIdUsuario(ctx, request != null ? request.usuarioExecutorId : null, true);

            // Carrega o usuário executor para verificação de permissões no domínio/serviço
            Usuario usuarioExecutor = carregarUsuario(idExecutor);

            ComandoAlterarEvento comando = new ComandoAlterarEvento(
                    idEvento,
                    request.titulo,
                    request.descricao,
                    LocalDateTime.parse(request.inicio),
                    LocalDateTime.parse(request.fim),
                    request.capacidade,
                    usuarioExecutor
            );

            Evento eventoAtualizado = alterarEventoPort.executar(comando);

            List<Integer> organizadores = new ArrayList<>();
            if (buscarVinculoOrganizadorPort != null) {
                buscarVinculoOrganizadorPort.listarPorEvento(idEvento.intValue())
                        .forEach(v -> organizadores.add(v.getUsuarioId()));
            }

            ctx.status(HttpStatus.OK).json(new EventoDetalheResponse(eventoAtualizado, organizadores));

        } catch (RegraNegocioException e) {
            // Se for negação de acesso (barreira de segurança), retorna 403 Forbidden ou 400 Bad Request
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("acesso negado")) {
                ctx.status(HttpStatus.FORBIDDEN).result(e.getMessage());
            } else {
                ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
            }
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result("O id do evento deve ser um número válido.");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno: " + e.getMessage());
        }
    }

    private void excluirEvento(Context ctx) {
        try {
            Long idEvento = Long.parseLong(ctx.pathParam("id"));

            // Tenta obter usuarioExecutorId pelo corpo (caso enviado em JSON) ou do cabeçalho
            Integer idCorpo = null;
            if (!ctx.body().isEmpty()) {
                try {
                    SalvarEventoRequest request = ctx.bodyAsClass(SalvarEventoRequest.class);
                    if (request != null) {
                        idCorpo = request.usuarioExecutorId;
                    }
                } catch (Exception ignored) {
                }
            }

            Integer idExecutor = extrairIdUsuario(ctx, idCorpo, true);
            Usuario usuarioExecutor = carregarUsuario(idExecutor);

            ComandoExcluirEvento comando = new ComandoExcluirEvento(idEvento, usuarioExecutor);
            excluirEventoPort.executar(comando);

            ctx.status(HttpStatus.NO_CONTENT);

        } catch (RegraNegocioException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("acesso negado")) {
                ctx.status(HttpStatus.FORBIDDEN).result(e.getMessage());
            } else {
                ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
            }
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result("O id do evento deve ser um número válido.");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno: " + e.getMessage());
        }
    }

    private void listarOrganizadoresDoEvento(Context ctx) {
        try {
            Long idEvento = Long.parseLong(ctx.pathParam("id"));
            List<Integer> ids = new ArrayList<>();
            if (buscarVinculoOrganizadorPort != null) {
                buscarVinculoOrganizadorPort.listarPorEvento(idEvento.intValue())
                        .forEach(v -> ids.add(v.getUsuarioId()));
            }
            ctx.status(HttpStatus.OK).json(ids);
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Erro interno: " + e.getMessage());
        }
    }

    // Método auxiliar para extrair o usuário executor do cabeçalho HTTP ou do corpo JSON
    private Integer extrairIdUsuario(Context ctx, Integer corpoUsuarioId, boolean obrigatorio) {
        // 1. Tenta extrair do header X-Usuario-Id
        String headerValue = ctx.header(HEADER_EXECUTOR_ID);
        if (headerValue != null && !headerValue.isBlank()) {
            try {
                return Integer.parseInt(headerValue.trim());
            } catch (NumberFormatException ignored) {
            }
        }

        // 2. Se não estiver no header, aceita se veio no corpo da requisição (usuarioExecutorId)
        if (corpoUsuarioId != null && corpoUsuarioId > 0) {
            return corpoUsuarioId;
        }

        if (obrigatorio) {
            throw new RegraNegocioException(
                    "Identificação do usuário executor é obrigatória (via header '" + HEADER_EXECUTOR_ID
                            + "' ou campo 'usuarioExecutorId').");
        }

        return null;
    }

    private Usuario carregarUsuario(Integer usuarioId) {
        if (usuarioRepository == null) {
            throw new RegraNegocioException("Repositório de usuários não configurado.");
        }
        return usuarioRepository.buscarPorId(usuarioId)
                .orElseThrow(() -> new RegraNegocioException("Usuário executor não encontrado com o ID: " + usuarioId));
    }
}
