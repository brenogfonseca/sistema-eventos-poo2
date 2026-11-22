package br.ueg.eventos.infrastructure.adapter.in.web;

import br.ueg.eventos.application.port.in.atividade.*;
import br.ueg.eventos.domain.exception.AcessoNegadoException;
import br.ueg.eventos.domain.exception.RegraNegocioException;
import br.ueg.eventos.domain.model.Atividade;
import br.ueg.eventos.domain.model.Local;
import br.ueg.eventos.domain.model.TipoFrequenciaEnum;
import br.ueg.eventos.domain.model.MetodoIdentificacaoCheckinEnum;
import br.ueg.eventos.domain.model.Trilha;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AtividadeController {

    private final CadastrarAtividadePort cadastrarAtividadePort;
    private final AtualizarAtividadePort atualizarAtividadePort;
    private final ExcluirAtividadePort excluirAtividadePort;
    private final BuscarAtividadePorIdPort buscarAtividadePorIdPort;
    private final ListarAtividadesPorEventoPort listarAtividadesPorEventoPort;
    private final EventoAuthorization autorizacao;
    private final ConsultarConflitosLocalAtividadePort consultarConflitosPort;

    public AtividadeController(
            CadastrarAtividadePort cadastrarAtividadePort,
            AtualizarAtividadePort atualizarAtividadePort,
            ExcluirAtividadePort excluirAtividadePort,
            BuscarAtividadePorIdPort buscarAtividadePorIdPort,
            ListarAtividadesPorEventoPort listarAtividadesPorEventoPort,
            EventoAuthorization autorizacao,
            ConsultarConflitosLocalAtividadePort consultarConflitosPort) {

        this.cadastrarAtividadePort = cadastrarAtividadePort;
        this.atualizarAtividadePort = atualizarAtividadePort;
        this.excluirAtividadePort = excluirAtividadePort;
        this.buscarAtividadePorIdPort = buscarAtividadePorIdPort;
        this.listarAtividadesPorEventoPort = listarAtividadesPorEventoPort;
        this.autorizacao = autorizacao;
        this.consultarConflitosPort = consultarConflitosPort;
    }

    public void registerRoutes(Javalin app) {
        app.post("/atividades", this::cadastrar);
        app.put("/atividades/{id}", this::atualizar);
        app.delete("/atividades/{id}", this::excluir);
        app.get("/atividades/{id}", this::buscarPorId);
        app.get("/atividades/evento/{idEvento}",
                this::listarPorEvento);
    }

    static class AtividadeRequest {
        public String nome;
        public String descricao;
        public Long eventoId;
        public Local local;
        public String horaInicio;
        public String horaFim;
        public String data;
        public boolean controlaVagas;
        public int vagas;
        public String tipo;
        public TipoFrequenciaEnum tipoFrequencia;
        public MetodoIdentificacaoCheckinEnum metodoIdentificacaoCheckin;
        public Trilha trilha;
    }

    private Atividade converter(AtividadeRequest request, Long id) {
        Atividade atividade = new Atividade(
                id,
                request.nome,
                request.eventoId,
                request.local,
                LocalTime.parse(request.horaInicio),
                LocalTime.parse(request.horaFim),
                LocalDate.parse(request.data),
                request.controlaVagas,
                request.vagas,
                request.tipo,
                request.tipoFrequencia,
                request.trilha,
                request.metodoIdentificacaoCheckin == null
                        ? MetodoIdentificacaoCheckinEnum.QR
                        : request.metodoIdentificacaoCheckin
        );
        atividade.alterarDescricao(request.descricao);
        return atividade;
    }

    private void cadastrar(Context ctx) {
        try {
            AtividadeRequest request =
                    ctx.bodyAsClass(AtividadeRequest.class);

            Atividade atividade = converter(request, null);

            Atividade cadastrada =
                    cadastrarAtividadePort.executar(atividade, extrairUsuarioId(ctx));

            ctx.status(HttpStatus.CREATED).json(respostaAtividade(cadastrada));

        } catch (AcessoNegadoException e) {
            ctx.status(HttpStatus.FORBIDDEN).result(e.getMessage());
        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (java.time.format.DateTimeParseException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .result("Data ou horário inválido.");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .result("Erro interno: " + e.getMessage());
        }
    }

    private void atualizar(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));

            AtividadeRequest request =
                    ctx.bodyAsClass(AtividadeRequest.class);

            Atividade atividade = converter(request, id);

            Atividade atualizada =
                    atualizarAtividadePort.executar(id, atividade, extrairUsuarioId(ctx));

            ctx.status(HttpStatus.OK).json(respostaAtividade(atualizada));

        } catch (AcessoNegadoException e) {
            ctx.status(HttpStatus.FORBIDDEN).result(e.getMessage());
        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .result("O id da atividade deve ser válido.");
        } catch (java.time.format.DateTimeParseException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .result("Data ou horário inválido.");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .result("Erro interno: " + e.getMessage());
        }
    }

    private Map<String, Object> respostaAtividade(Atividade atividade) {
        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("id", atividade.getId());
        resposta.put("nome", atividade.getNome());
        resposta.put("descricao", atividade.getDescricao());
        resposta.put("eventoId", atividade.getEventoId());
        resposta.put("local", atividade.getLocal());
        resposta.put("horaInicio", atividade.getHoraInicio());
        resposta.put("horaFim", atividade.getHoraFim());
        resposta.put("data", atividade.getData());
        resposta.put("controlaVagas", atividade.isControlaVagas());
        resposta.put("vagas", atividade.getVagas());
        resposta.put("tipo", atividade.getTipo());
        resposta.put("tipoFrequencia", atividade.getTipoFrequencia());
        resposta.put("metodoIdentificacaoCheckin", atividade.getMetodoIdentificacaoCheckin());
        resposta.put("trilha", atividade.getTrilha());
        resposta.put("avisos", consultarConflitosPort.executar(atividade));
        return resposta;
    }

    private Integer extrairUsuarioId(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.header("X-Usuario-Id"));
            if (id <= 0) throw new NumberFormatException();
            return id;
        } catch (Exception e) {
            throw new AcessoNegadoException("Informe o organizador autenticado no cabeçalho X-Usuario-Id.");
        }
    }

    private void excluir(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));

            autorizacao.exigirGerenciadorDaAtividade(ctx, id);

            excluirAtividadePort.executar(id);

            ctx.status(HttpStatus.NO_CONTENT);

        } catch (AcessoNegadoException e) {
            ctx.status(HttpStatus.FORBIDDEN).result(e.getMessage());
        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .result("O id da atividade deve ser válido.");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .result("Erro interno: " + e.getMessage());
        }
    }

    private void buscarPorId(Context ctx) {
        try {
            Long id = Long.parseLong(ctx.pathParam("id"));

            Atividade atividade =
                    buscarAtividadePorIdPort.executar(id);
            autorizacao.exigirLeituraDaProgramacao(ctx, atividade.getEventoId());

            ctx.status(HttpStatus.OK).json(atividade);

        } catch (AcessoNegadoException e) {
            ctx.status(HttpStatus.FORBIDDEN).result(e.getMessage());
        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .result("O id da atividade deve ser válido.");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .result("Erro interno: " + e.getMessage());
        }
    }

    private void listarPorEvento(Context ctx) {
        try {
            Long idEvento =
                    Long.parseLong(ctx.pathParam("idEvento"));

            autorizacao.exigirLeituraDaProgramacao(ctx, idEvento);

            ctx.status(HttpStatus.OK)
                    .json(listarAtividadesPorEventoPort.executar(idEvento));

        } catch (AcessoNegadoException e) {
            ctx.status(HttpStatus.FORBIDDEN).result(e.getMessage());
        } catch (RegraNegocioException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .result("O id do evento deve ser válido.");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .result("Erro interno: " + e.getMessage());
        }
    }
}
