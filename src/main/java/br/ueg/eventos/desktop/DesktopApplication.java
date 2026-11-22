package br.ueg.eventos.desktop;

import br.ueg.eventos.desktop.ApiClient.ApiException;
import br.ueg.eventos.desktop.ApiClient.Session;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Standalone JavaFX client. Every operation uses the same REST API and PostgreSQL database as the web client. */
public final class DesktopApplication extends Application {
    private final ApiClient api = new ApiClient();
    private final ObjectMapper json = new ObjectMapper();
    private Stage stage;
    private Session session;
    private List<EventRow> events = new ArrayList<>();
    private List<RegistrationRow> registrations = new ArrayList<>();
    private final List<ListView<EventRow>> eventLists = new ArrayList<>();
    private final List<ComboBox<EventRow>> eventSelectors = new ArrayList<>();
    private final List<ComboBox<RegistrationRow>> registrationSelectors = new ArrayList<>();
    private final List<ComboBox<LocalRow>> localSelectors = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        showLogin();
    }

    private void showLogin() {
        TabPane auth = new TabPane();
        Tab login = new Tab("Entrar", loginForm());
        Tab cadastro = new Tab("Criar conta", cadastroForm());
        login.setClosable(false);
        cadastro.setClosable(false);
        auth.getTabs().setAll(login, cadastro);
        stage.setTitle("Gestão de Eventos — Desktop");
        stage.setMinWidth(620);
        stage.setMinHeight(480);
        stage.setScene(new Scene(auth, 700, 520));
        stage.show();
    }

    private Node loginForm() {
        TextField email = new TextField();
        email.setPromptText("E-mail");
        PasswordField senha = new PasswordField();
        senha.setPromptText("Senha");
        Label status = new Label("Entre com a conta cadastrada no sistema.");
        Button entrar = new Button("Entrar");
        entrar.setDefaultButton(true);
        entrar.setOnAction(e -> executar(status, () -> {
            JsonNode retorno = api.post("/auth/login", Map.of("email", email.getText().trim(), "senha", senha.getText()), null);
            session = new Session(retorno.path("id").asInt(), retorno.path("nome").asText(),
                    retorno.path("email").asText(), retorno.path("perfil").asText(),
                    retorno.path("token").asText(), retorno.path("expiraEm").asText());
            showMain();
        }));
        VBox box = new VBox(12, new Label("Acesse sua conta"), email, senha, entrar, status);
        box.setPadding(new Insets(28));
        box.setMaxWidth(450);
        return centralizar(box);
    }

    private Node cadastroForm() {
        TextField nome = new TextField();
        nome.setPromptText("Nome completo");
        TextField email = new TextField();
        email.setPromptText("E-mail");
        TextField cpf = new TextField();
        cpf.setPromptText("CPF (opcional)");
        PasswordField senha = new PasswordField();
        senha.setPromptText("Senha forte");
        Label status = new Label("Use ao menos 8 caracteres, com maiúscula, minúscula, número e símbolo.");
        Button cadastrar = new Button("Criar conta");
        cadastrar.setOnAction(e -> executar(status, () -> {
            Map<String, Object> dados = new HashMap<>();
            dados.put("nome", nome.getText().trim());
            dados.put("email", email.getText().trim());
            dados.put("senha", senha.getText());
            if (!cpf.getText().isBlank()) dados.put("cpf", cpf.getText().replaceAll("\\D", ""));
            api.post("/usuarios", dados, null);
            status.setText("Conta criada. Entre na aba Entrar.");
            senha.clear();
        }));
        VBox box = new VBox(12, nome, email, cpf, senha, cadastrar, status);
        box.setPadding(new Insets(28));
        box.setMaxWidth(450);
        return centralizar(box);
    }

    private void showMain() {
        eventLists.clear();
        eventSelectors.clear();
        registrationSelectors.clear();
        localSelectors.clear();
        TabPane tabs = new TabPane();
        tabs.getTabs().setAll(
                tab("Eventos e programação", eventosTab()),
                tab("Minhas inscrições e QR", inscricoesTab()),
                tab("Leitor e frequência", leitorTab()),
                tab("Corrigir presenças", presencasDesktopTab()),
                tab("Avaliações", avaliacoesTab()),
                tab("Relatórios", relatoriosTab()),
                tab("Criar evento ou atividade", gestaoTab())
        );
        Label identidade = new Label("Conectado: " + session.nome() + " (" + session.perfil() + ")");
        Button sair = new Button("Sair");
        sair.setOnAction(e -> { session = null; showLogin(); });
        HBox top = new HBox(12, identidade, new Region(), sair);
        HBox.setHgrow(top.getChildren().get(1), Priority.ALWAYS);
        top.setPadding(new Insets(10));
        BorderPane root = new BorderPane(tabs);
        root.setTop(top);
        stage.setTitle("Gestão de Eventos — " + session.nome());
        stage.setScene(new Scene(root, 1120, 760));
        refreshData();
    }

    private Tab tab(String titulo, Node conteudo) {
        Tab tab = new Tab(titulo, conteudo);
        tab.setClosable(false);
        return tab;
    }

    private Node eventosTab() {
        ListView<EventRow> lista = new ListView<>();
        eventLists.add(lista);
        lista.setPrefWidth(400);
        VBox detalhes = new VBox(10);
        Label titulo = new Label("Selecione um evento para ver a programação.");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label descricao = new Label();
        descricao.setWrapText(true);
        VBox atividadesBox = new VBox(8);
        ScrollPane atividadesScroll = new ScrollPane(atividadesBox);
        atividadesScroll.setFitToWidth(true);
        Label status = new Label();
        Button atualizar = new Button("Atualizar eventos");
        Button inscrever = new Button("Inscrever-se no evento");
        Button carregar = new Button("Carregar atividades");
        Button salvar = new Button("Salvar minha agenda");
        HBox acoes = new HBox(8, atualizar, inscrever, carregar, salvar);
        detalhes.getChildren().addAll(titulo, descricao, acoes, status, atividadesScroll);
        VBox.setVgrow(atividadesScroll, Priority.ALWAYS);
        EventRow[] selecionado = {null};
        Map<Long, CheckBox> checks = new HashMap<>();

        atualizar.setOnAction(e -> executar(status, () -> preencherEventos(lista)));
        lista.setCellFactory(view -> new ListCell<>() {
            @Override protected void updateItem(EventRow item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.label());
            }
        });
        lista.getSelectionModel().selectedItemProperty().addListener((obs, anterior, atual) -> {
            if (atual == null) return;
            selecionado[0] = atual;
            titulo.setText(atual.data.path("titulo").asText("Evento"));
            descricao.setText(atual.data.path("descricao").asText(""));
            atividadesBox.getChildren().clear();
            checks.clear();
            status.setText("Clique em Carregar atividades. Horários conflitantes são rejeitados pela API.");
        });
        inscrever.setOnAction(e -> {
            EventRow event = selecionado[0];
            if (event == null) { status.setText("Selecione um evento."); return; }
            executar(status, () -> {
                api.post("/inscricoes/evento", Map.of("idEvento", event.id()), session);
                carregarInscricoes();
                status.setText("Inscrição no evento concluída. Agora carregue e selecione as atividades.");
            });
        });
        carregar.setOnAction(e -> {
            EventRow event = selecionado[0];
            if (event == null) { status.setText("Selecione um evento."); return; }
            executar(status, () -> carregarAtividades(event, atividadesBox, checks, status));
        });
        salvar.setOnAction(e -> {
            EventRow event = selecionado[0];
            if (event == null) { status.setText("Selecione um evento."); return; }
            RegistrationRow registration = registrationForEvent(event.id());
            if (registration == null || registration.cancelada()) {
                status.setText("Faça primeiro sua inscrição no evento.");
                return;
            }
            executar(status, () -> {
                List<Long> escolhidas = checks.entrySet().stream().filter(entry -> entry.getValue().isSelected())
                        .map(Map.Entry::getKey).toList();
                api.put("/inscricoes/evento/" + registration.id() + "/atividades",
                        Map.of("idsAtividades", escolhidas), session);
                status.setText("Agenda salva. A API valida conflitos de horário e capacidade.");
                carregarInscricoes();
            });
        });

        HBox content = new HBox(12, lista, detalhes);
        content.setPadding(new Insets(14));
        HBox.setHgrow(detalhes, Priority.ALWAYS);
        return content;
    }

    private void carregarAtividades(EventRow event, VBox box, Map<Long, CheckBox> checks, Label status) throws Exception {
        JsonNode resposta = api.get("/atividades/evento/" + event.id(), session);
        box.getChildren().clear();
        checks.clear();
        RegistrationRow registration = registrationForEvent(event.id());
        Map<Long, Boolean> escolhidas = new HashMap<>();
        if (registration != null && !registration.cancelada()) {
            JsonNode atual = api.get("/inscricoes/evento/" + registration.id() + "/atividades", session);
            atual.forEach(item -> { if (!item.path("cancelada").asBoolean()) escolhidas.put(item.path("idAtividade").asLong(), true); });
        }
        resposta.forEach(atividade -> {
            long id = atividade.path("id").asLong();
            String hora = atividade.path("data").asText("") + "  " + atividade.path("horaInicio").asText("")
                    + "–" + atividade.path("horaFim").asText("");
            String local = atividade.path("local").path("nome").asText(atividade.path("localNome").asText(""));
            CheckBox check = new CheckBox(atividade.path("nome").asText("Atividade") + " — " + hora
                    + (local.isBlank() ? "" : " — " + local));
            check.setSelected(escolhidas.containsKey(id));
            check.setDisable(registration == null || registration.cancelada()
                    || (atividade.path("controlaVagas").asBoolean() && atividade.path("vagas").asInt() <= 0 && !check.isSelected()));
            checks.put(id, check);
            Label desc = new Label(atividade.path("descricao").asText(""));
            desc.setWrapText(true);
            VBox row = new VBox(2, check, desc);
            row.setPadding(new Insets(7));
            row.setStyle("-fx-border-color: #dddddd; -fx-border-radius: 4;");
            box.getChildren().add(row);
        });
        status.setText(resposta.size() + " atividade(s). Escolha as opções e salve sua agenda.");
    }

    private Node inscricoesTab() {
        ComboBox<RegistrationRow> lista = new ComboBox<>();
        registrationSelectors.add(lista);
        lista.setPrefWidth(560);
        ImageView qr = new ImageView();
        qr.setFitWidth(230);
        qr.setFitHeight(230);
        qr.setPreserveRatio(true);
        Label detalhes = new Label();
        detalhes.setWrapText(true);
        Label status = new Label();
        Button atualizar = new Button("Atualizar inscrições");
        Button gerarQr = new Button("Exibir QR do evento");
        Button cancelar = new Button("Cancelar inscrição");
        Button certificado = new Button("Baixar certificado PDF");
        atualizar.setOnAction(e -> executar(status, () -> carregarInscricoes(lista)));
        gerarQr.setOnAction(e -> {
            RegistrationRow item = lista.getValue();
            if (item == null) { status.setText("Selecione uma inscrição."); return; }
            if (item.cancelada()) { status.setText("A inscrição está cancelada."); return; }
            executar(status, () -> {
                JsonNode codigo = api.get("/inscricoes/evento/" + item.id() + "/checkin-qr", session);
                byte[] png = java.util.Base64.getDecoder().decode(codigo.path("imagemBase64").asText());
                qr.setImage(new Image(new ByteArrayInputStream(png)));
                JsonNode frequencia = api.get("/inscricoes/evento/" + item.id() + "/frequencia-certificado", session);
                double horasPresentes = frequencia.path("minutosPresentes").asDouble() / 60.0;
                double horasTotais = frequencia.path("minutosTotais").asDouble() / 60.0;
                String criterio = frequencia.path("criterioFrequencia").asText("PERCENTUAL");
                String minimo = "PERCENTUAL".equals(criterio)
                        ? frequencia.path("frequenciaMinima").asText("75") + "%"
                        : String.format("%.2f h", frequencia.path("frequenciaMinimaMinutos").asDouble() / 60.0);
                double cargaCertificado = frequencia.path("cargaHorariaCertificadoMinutos").asDouble() / 60.0;
                String modoCarga = "FIXA".equals(frequencia.path("modoCargaHoraria").asText("DINAMICA"))
                        ? "fixa" : "dinâmica";
                detalhes.setText("Evento #" + item.idEvento()
                        + " · Presença: " + String.format("%.2f h / %.2f h", horasPresentes, horasTotais)
                        + " (" + frequencia.path("percentualFrequencia").asText("0") + "%)"
                        + " · mínimo: " + minimo
                        + " · certificado: " + String.format("%.2f h", cargaCertificado) + " (" + modoCarga + ")"
                        + " · " + (frequencia.path("elegivel").asBoolean()
                                ? "elegível ao certificado" : "não elegível ainda"));
                status.setText("Este QR identifica sua inscrição no evento e pode ser usado nas atividades selecionadas.");
            });
        });
        cancelar.setOnAction(e -> {
            RegistrationRow item = lista.getValue();
            if (item == null || item.cancelada()) { status.setText("Selecione uma inscrição ativa."); return; }
            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION,
                    "Cancelar a inscrição no evento e liberar as vagas das atividades?", ButtonType.CANCEL, ButtonType.OK);
            confirmacao.showAndWait().filter(b -> b == ButtonType.OK).ifPresent(b ->
                    executar(status, () -> {
                        api.delete("/inscricoes/evento/" + item.id(), session);
                        qr.setImage(null);
                        carregarInscricoes(lista);
                        status.setText("Inscrição cancelada.");
                    }));
        });
        certificado.setOnAction(e -> {
            RegistrationRow item = lista.getValue();
            if (item == null) { status.setText("Selecione uma inscrição."); return; }
            FileChooser chooser = new FileChooser();
            chooser.setInitialFileName("certificado-evento-" + item.idEvento() + ".pdf");
            File destino = chooser.showSaveDialog(stage);
            if (destino == null) return;
            executar(status, () -> {
                Files.write(destino.toPath(), api.getBytes(
                        "/inscricoes/evento/" + item.id() + "/certificado.pdf", session));
                status.setText("Certificado salvo em " + destino.getAbsolutePath());
            });
        });
        HBox botoes = new HBox(8, atualizar, gerarQr, cancelar, certificado);
        VBox content = new VBox(12, lista, botoes, status, qr, detalhes);
        content.setPadding(new Insets(16));
        content.setAlignment(javafx.geometry.Pos.TOP_CENTER);
        return content;
    }

    private Node leitorTab() {
        ComboBox<CheckinActivityRow> atividade = new ComboBox<>();
        TextArea token = new TextArea();
        token.setPromptText("Cole o token lido do QR Code ou carregue uma imagem PNG/JPG do código.");
        token.setPrefRowCount(3);
        TextField termoBusca = new TextField();
        termoBusca.setPromptText("Nome ou CPF");
        ComboBox<CheckinParticipantRow> participante = new ComboBox<>();
        ComboBox<String> acao = new ComboBox<>();
        Label status = new Label();
        Button carregarAtividades = new Button("Carregar minhas atividades autorizadas");
        Button carregarImagem = new Button("Ler QR de uma imagem");
        Button buscar = new Button("Buscar participante");
        Button enviar = new Button("Registrar presença");
        VBox blocoQr = new VBox(8, carregarImagem, token);
        VBox blocoBusca = new VBox(8, termoBusca, buscar, participante);
        blocoBusca.setVisible(false);
        blocoBusca.setManaged(false);
        carregarAtividades.setOnAction(e -> executar(status, () -> {
            JsonNode resposta = api.get("/frequencias/atividades-validaveis", session);
            List<CheckinActivityRow> itens = new ArrayList<>();
            resposta.forEach(item -> itens.add(new CheckinActivityRow(item)));
            atividade.setItems(FXCollections.observableArrayList(itens));
            if (!itens.isEmpty()) atividade.getSelectionModel().selectFirst();
            status.setText(itens.isEmpty() ? "Não há atividades autorizadas para validar."
                    : itens.size() + " atividade(s) autorizada(s) carregada(s).");
        }));

        atividade.valueProperty().addListener((obs, anterior, atual) -> {
            if (atual == null) return;
            List<String> permitidas = switch (atual.politica()) {
                case "ENTRADA_SAIDA" -> List.of("ENTRADA", "SAIDA");
                case "MANUAL" -> List.of("PRESENTE");
                default -> List.of("CHECKIN");
            };
            acao.setItems(FXCollections.observableArrayList(permitidas));
            acao.getSelectionModel().selectFirst();
            boolean usaBusca = "NOME_CPF".equals(atual.metodo());
            blocoBusca.setVisible(usaBusca);
            blocoBusca.setManaged(usaBusca);
            blocoQr.setVisible(!usaBusca);
            blocoQr.setManaged(!usaBusca);
            participante.getItems().clear();
            status.setText("Identificação: " + (usaBusca ? "nome/CPF" : "QR Code")
                    + " · frequência: " + atual.politica());
        });

        buscar.setOnAction(e -> {
            CheckinActivityRow selecionada = atividade.getValue();
            if (selecionada == null || termoBusca.getText().isBlank()) {
                status.setText("Selecione uma atividade e informe nome ou CPF."); return;
            }
            executar(status, () -> {
                String q = URLEncoder.encode(termoBusca.getText().trim(), StandardCharsets.UTF_8);
                JsonNode resposta = api.get("/atividades/" + selecionada.idAtividade()
                        + "/participantes-checkin?q=" + q, session);
                List<CheckinParticipantRow> itens = new ArrayList<>();
                resposta.forEach(item -> itens.add(new CheckinParticipantRow(item)));
                participante.setItems(FXCollections.observableArrayList(itens));
                if (!itens.isEmpty()) participante.getSelectionModel().selectFirst();
                status.setText(itens.isEmpty() ? "Nenhum participante encontrado." : itens.size() + " participante(s) encontrado(s).");
            });
        });

        carregarImagem.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Abrir imagem do QR Code");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg", "*.bmp"));
            File arquivo = chooser.showOpenDialog(stage);
            if (arquivo == null) return;
            executar(status, () -> {
                var imagem = ImageIO.read(arquivo);
                if (imagem == null) throw new IllegalArgumentException("O arquivo escolhido não é uma imagem compatível.");
                String valor = new MultiFormatReader().decode(
                        new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(imagem)))).getText();
                token.setText(valor);
                status.setText("QR Code lido. Confira atividade e ação antes de confirmar.");
            });
        });

        enviar.setOnAction(e -> {
            CheckinActivityRow selecionada = atividade.getValue();
            if (selecionada == null) { status.setText("Selecione uma atividade."); return; }
            executar(status, () -> {
                if ("NOME_CPF".equals(selecionada.metodo())) {
                    CheckinParticipantRow escolhido = participante.getValue();
                    if (escolhido == null) { status.setText("Busque e selecione um participante."); return; }
                    api.post("/frequencias/checkin-participante", Map.of(
                            "idInscricaoEvento", escolhido.idInscricaoEvento(),
                            "idAtividade", selecionada.idAtividade(), "acao", acao.getValue()), session);
                    termoBusca.clear();
                    participante.getItems().clear();
                } else {
                    if (token.getText().isBlank()) { status.setText("Leia o QR Code do participante."); return; }
                    api.post("/frequencias/checkin-qr", Map.of("token", token.getText().trim(),
                            "idAtividade", selecionada.idAtividade(), "acao", acao.getValue()), session);
                    token.clear();
                }
                status.setText("Presença registrada e auditada. Colaboradores não podem corrigir ou marcar falta.");
            });
        });
        VBox content = new VBox(12, carregarAtividades, new Label("Atividade autorizada"), atividade,
                new Label("Ação conforme o critério de frequência"), acao, blocoQr, blocoBusca, enviar, status);
        content.setPadding(new Insets(18));
        return content;
    }

    private Node presencasDesktopTab() {
        ComboBox<EventRow> evento = new ComboBox<>();
        eventSelectors.add(evento);
        ComboBox<ActivityRow> atividade = new ComboBox<>();
        ComboBox<AttendanceRow> participante = new ComboBox<>();
        Label status = new Label("Esta tela é exclusiva para organizadores; a API também valida essa permissão.");
        Button carregarAtividades = new Button("Carregar atividades");
        Button carregarLista = new Button("Carregar participantes");
        Button alternar = new Button("Marcar presença/falta");
        carregarAtividades.setOnAction(e -> {
            EventRow selecionado = evento.getValue();
            if (selecionado == null) { status.setText("Selecione um evento."); return; }
            executar(status, () -> {
                JsonNode resposta = api.get("/atividades/evento/" + selecionado.id(), session);
                List<ActivityRow> itens = new ArrayList<>();
                resposta.forEach(item -> itens.add(new ActivityRow(item)));
                atividade.setItems(FXCollections.observableArrayList(itens));
                if (!itens.isEmpty()) atividade.getSelectionModel().selectFirst();
                status.setText(itens.size() + " atividade(s) carregadas.");
            });
        });
        carregarLista.setOnAction(e -> {
            ActivityRow selecionada = atividade.getValue();
            if (selecionada == null) { status.setText("Selecione uma atividade."); return; }
            executar(status, () -> {
                JsonNode resposta = api.get("/frequencias/atividade/" + selecionada.id() + "/participantes", session);
                List<AttendanceRow> itens = new ArrayList<>();
                resposta.forEach(item -> itens.add(new AttendanceRow(item)));
                participante.setItems(FXCollections.observableArrayList(itens));
                if (!itens.isEmpty()) participante.getSelectionModel().selectFirst();
                status.setText(itens.size() + " participante(s). Cada alteração gera uma correção auditada.");
            });
        });
        alternar.setOnAction(e -> {
            ActivityRow selecionada = atividade.getValue();
            AttendanceRow pessoa = participante.getValue();
            if (selecionada == null || pessoa == null) { status.setText("Selecione atividade e participante."); return; }
            executar(status, () -> {
                api.post("/frequencias/manual", Map.of("idInscricaoEvento", pessoa.idInscricaoEvento(),
                        "idAtividade", selecionada.id(), "presente", !pessoa.presente()), session);
                carregarLista.fire();
                status.setText(pessoa.presente() ? "Participante marcado como ausente." : "Participante marcado como presente.");
            });
        });
        VBox content = new VBox(12, evento, carregarAtividades, atividade, carregarLista, participante,
                alternar, status);
        content.setPadding(new Insets(18));
        return content;
    }

    private Node avaliacoesTab() {
        ComboBox<EventRow> evento = new ComboBox<>();
        eventSelectors.add(evento);
        ComboBox<ActivityRow> atividade = new ComboBox<>();
        TextField titulo = new TextField();
        titulo.setPromptText("Título do questionário");
        CheckBox anonimo = new CheckBox("Ocultar respondentes nos relatórios");
        CheckBox publicado = new CheckBox("Publicar para participantes com frequência validada");
        TextField enunciado = new TextField();
        enunciado.setPromptText("Enunciado da questão");
        ComboBox<String> tipoQuestao = new ComboBox<>(FXCollections.observableArrayList(
                "TEXTUAL", "ESCOLHA_UNICA", "ESCALA_NUMERICA"));
        tipoQuestao.getSelectionModel().selectFirst();
        TextArea opcoes = new TextArea();
        opcoes.setPromptText("Opções de escolha única, uma por linha");
        opcoes.setPrefRowCount(3);
        TextField escalaMinima = new TextField("1");
        TextField escalaMaxima = new TextField("5");
        VBox questoes = new VBox(12);
        VBox gestao = new VBox(9);
        Label status = new Label("Participantes respondem depois que o organizador publica as questões.");
        Button carregarAtividades = new Button("Carregar atividades");
        Button abrir = new Button("Abrir avaliação");
        Button criar = new Button("Criar questionário");
        Button salvar = new Button("Salvar título e publicação");
        Button adicionarQuestao = new Button("Adicionar questão");

        carregarAtividades.setOnAction(e -> {
            EventRow selecionado = evento.getValue();
            if (selecionado == null) { status.setText("Selecione um evento."); return; }
            executar(status, () -> {
                JsonNode resposta = api.get("/atividades/evento/" + selecionado.id(), session);
                List<ActivityRow> itens = new ArrayList<>();
                resposta.forEach(item -> itens.add(new ActivityRow(item)));
                atividade.setItems(FXCollections.observableArrayList(itens));
                if (!itens.isEmpty()) atividade.getSelectionModel().selectFirst();
                status.setText(itens.size() + " atividade(s) carregadas.");
            });
        });

        criar.setOnAction(e -> {
            ActivityRow selecionada = atividade.getValue();
            if (selecionada == null || titulo.getText().isBlank()) {
                status.setText("Selecione uma atividade e informe o título."); return;
            }
            if (!gerenciaEvento(evento.getValue())) { status.setText("Somente organizadores podem criar questionários."); return; }
            executar(status, () -> {
                api.post("/questionarios", Map.of("idAtividade", selecionada.id(),
                        "titulo", titulo.getText().trim(), "anonimo", anonimo.isSelected(), "publicado", false), session);
                abrirAvaliacao(selecionada, titulo, anonimo, publicado, gestao, questoes, status,
                        enunciado, tipoQuestao, opcoes, escalaMinima, escalaMaxima);
            });
        });

        abrir.setOnAction(e -> {
            ActivityRow selecionada = atividade.getValue();
            if (selecionada == null) { status.setText("Selecione uma atividade."); return; }
            executar(status, () -> abrirAvaliacao(selecionada, titulo, anonimo, publicado,
                    gestao, questoes, status, enunciado, tipoQuestao, opcoes, escalaMinima, escalaMaxima));
        });

        salvar.setOnAction(e -> {
            ActivityRow selecionada = atividade.getValue();
            if (selecionada == null || !gerenciaEvento(evento.getValue())) { status.setText("Ação restrita ao organizador."); return; }
            executar(status, () -> {
                JsonNode questionario = api.get("/questionarios/atividade/" + selecionada.id(), session);
                api.put("/questionarios/" + questionario.path("id").asLong(), Map.of(
                        "titulo", titulo.getText().trim(), "anonimo", anonimo.isSelected(),
                        "publicado", publicado.isSelected()), session);
                status.setText("Questionário atualizado.");
            });
        });

        adicionarQuestao.setOnAction(e -> {
            ActivityRow selecionada = atividade.getValue();
            if (selecionada == null || !gerenciaEvento(evento.getValue())) { status.setText("Ação restrita ao organizador."); return; }
            executar(status, () -> {
                JsonNode questionario = api.get("/questionarios/atividade/" + selecionada.id(), session);
                String tipo = tipoQuestao.getValue();
                Map<String, Object> dados = new HashMap<>();
                dados.put("idQuestionario", questionario.path("id").asLong());
                dados.put("enunciado", enunciado.getText().trim());
                dados.put("tipo", tipo);
                dados.put("opcoes", "ESCOLHA_UNICA".equals(tipo)
                        ? opcoes.getText().lines().map(String::trim).filter(s -> !s.isBlank()).toList() : List.of());
                dados.put("escalaMinima", "ESCALA_NUMERICA".equals(tipo) ? Integer.parseInt(escalaMinima.getText()) : null);
                dados.put("escalaMaxima", "ESCALA_NUMERICA".equals(tipo) ? Integer.parseInt(escalaMaxima.getText()) : null);
                api.post("/questoes", dados, session);
                enunciado.clear();
                abrirAvaliacao(selecionada, titulo, anonimo, publicado, gestao, questoes, status,
                        enunciado, tipoQuestao, opcoes, escalaMinima, escalaMaxima);
                status.setText("Questão adicionada.");
            });
        });

        tipoQuestao.valueProperty().addListener((obs, anterior, atual) -> {
            opcoes.setDisable(!"ESCOLHA_UNICA".equals(atual));
            escalaMinima.setDisable(!"ESCALA_NUMERICA".equals(atual));
            escalaMaxima.setDisable(!"ESCALA_NUMERICA".equals(atual));
        });
        opcoes.setDisable(true);
        escalaMinima.setDisable(true);
        escalaMaxima.setDisable(true);
        gestao.getChildren().addAll(new Label("Gerenciamento do questionário"), titulo, anonimo,
                publicado, salvar, new Separator(), enunciado, tipoQuestao, opcoes,
                new HBox(8, escalaMinima, escalaMaxima), adicionarQuestao);
        VBox content = new VBox(12, new Label("Evento"), evento, carregarAtividades,
                new Label("Atividade"), atividade, new HBox(8, abrir, criar), status, gestao, questoes);
        content.setPadding(new Insets(16));
        return new ScrollPane(content);
    }

    private void abrirAvaliacao(ActivityRow atividade, TextField titulo, CheckBox anonimo,
                                CheckBox publicado, VBox gestao, VBox questoes, Label status,
                                TextField enunciado, ComboBox<String> tipoQuestao, TextArea opcoes,
                                TextField escalaMinima, TextField escalaMaxima) throws Exception {
        JsonNode questionario = api.get("/questionarios/atividade/" + atividade.id(), session);
        titulo.setText(questionario.path("titulo").asText(""));
        anonimo.setSelected(questionario.path("anonimo").asBoolean());
        publicado.setSelected(questionario.path("publicado").asBoolean());
        boolean podeEditar = gerenciaEventoPorAtividade(atividade);
        gestao.setVisible(podeEditar);
        gestao.setManaged(podeEditar);
        questoes.getChildren().clear();
        JsonNode itens = api.get("/questoes/questionario/" + questionario.path("id").asLong(), session);
        for (JsonNode questao : itens) {
            VBox card = new VBox(8);
            card.setPadding(new Insets(10));
            card.setStyle("-fx-border-color: #dddddd; -fx-border-radius: 4;");
            card.getChildren().add(new Label(questao.path("enunciado").asText()));
            if (podeEditar) {
                card.getChildren().add(new Label("Tipo: " + questao.path("tipo").asText()));
            } else if (questionario.path("publicado").asBoolean()) {
                Control campo;
                String tipo = questao.path("tipo").asText();
                if ("ESCOLHA_UNICA".equals(tipo)) {
                    ComboBox<String> escolhas = new ComboBox<>();
                    questao.path("opcoes").forEach(op -> escolhas.getItems().add(op.asText()));
                    campo = escolhas;
                } else if ("ESCALA_NUMERICA".equals(tipo)) {
                    campo = new Spinner<>(questao.path("escalaMinima").asInt(1),
                            questao.path("escalaMaxima").asInt(5), questao.path("escalaMinima").asInt(1));
                } else {
                    TextArea texto = new TextArea();
                    texto.setPrefRowCount(3);
                    campo = texto;
                }
                Button enviar = new Button("Enviar resposta");
                enviar.setOnAction(e -> executar(status, () -> {
                    String valor = campo instanceof ComboBox<?> combo ? String.valueOf(combo.getValue())
                            : campo instanceof Spinner<?> spinner ? String.valueOf(spinner.getValue())
                            : ((TextArea) campo).getText().trim();
                    if (valor.equals("null") || valor.isBlank()) throw new IllegalArgumentException("Preencha a resposta.");
                    api.post("/respostas", Map.of("idQuestao", questao.path("id").asLong(), "valor", valor), session);
                    status.setText("Resposta registrada.");
                    enviar.setDisable(true);
                }));
                card.getChildren().addAll(campo, enviar);
            }
            questoes.getChildren().add(card);
        }
        status.setText("Questionário carregado. A API valida inscrição, presença e publicação antes de aceitar cada resposta.");
    }

    private boolean gerenciaEvento(EventRow evento) {
        if (evento == null) return false;
        if ("ADMINISTRADOR".equals(session.perfil())) return true;
        for (JsonNode organizador : evento.data().path("organizadoresIds")) {
            if (organizador.asInt() == session.id()) return true;
        }
        return false;
    }

    private boolean gerenciaEventoPorAtividade(ActivityRow atividade) {
        return gerenciaEvento(events.stream()
                .filter(evento -> evento.id() == atividade.data().path("eventoId").asLong())
                .findFirst().orElse(null));
    }

    private Node relatoriosTab() {
        ComboBox<EventRow> evento = new ComboBox<>();
        eventSelectors.add(evento);
        ComboBox<String> tipo = new ComboBox<>(FXCollections.observableArrayList("Inscrições", "Frequências"));
        tipo.getSelectionModel().selectFirst();
        Label status = new Label("A exportação exige perfil de administrador ou vínculo de organização do evento.");
        Button baixar = new Button("Exportar CSV");
        baixar.setOnAction(e -> {
            EventRow selecionado = evento.getValue();
            if (selecionado == null) { status.setText("Selecione um evento."); return; }
            FileChooser chooser = new FileChooser();
            chooser.setInitialFileName((tipo.getValue().startsWith("Ins") ? "inscricoes" : "frequencias")
                    + "-evento-" + selecionado.id() + ".csv");
            File destino = chooser.showSaveDialog(stage);
            if (destino == null) return;
            executar(status, () -> {
                String caminho = tipo.getValue().startsWith("Ins") ? "inscricoes" : "frequencias";
                byte[] conteudo = api.getBytes("/relatorios/eventos/" + selecionado.id() + "/" + caminho + ".csv", session);
                Files.write(destino.toPath(), conteudo);
                status.setText("Relatório salvo em " + destino.getAbsolutePath());
            });
        });
        VBox content = new VBox(12, evento, tipo, baixar, status);
        content.setPadding(new Insets(18));
        return content;
    }

    private Node gestaoTab() {
        TabPane tabPane = new TabPane(tab("Novo evento", criarEventoForm()),
                tab("Editar evento", editarEventoForm()),
                tab("Nova atividade", criarAtividadeForm()),
                tab("Editar atividade", editarAtividadeForm()),
                tab("Pessoas da programação", pessoasTab()),
                tab("Colaboradores", colaboradoresDesktopTab()));
        if ("ADMINISTRADOR".equals(session.perfil())) tabPane.getTabs().add(tab("Locais", locaisTab()));
        return tabPane;
    }

    private Node editarEventoForm() {
        ComboBox<EventRow> evento = new ComboBox<>();
        eventSelectors.add(evento);
        TextField titulo = new TextField();
        TextArea descricao = new TextArea();
        TextField inicio = new TextField(); inicio.setPromptText("Início: AAAA-MM-DDTHH:mm:ss");
        TextField fim = new TextField(); fim.setPromptText("Fim: AAAA-MM-DDTHH:mm:ss");
        TextField capacidade = new TextField();
        ComboBox<String> modalidade = new ComboBox<>(FXCollections.observableArrayList("PRESENCIAL", "ONLINE", "HIBRIDO"));
        ComboBox<String> statusEvento = new ComboBox<>(FXCollections.observableArrayList("RASCUNHO", "PUBLICADO", "CANCELADO", "ENCERRADO"));
        ComboBox<String> minimo = new ComboBox<>(FXCollections.observableArrayList("50", "75", "100"));
        ComboBox<String> criterioFrequencia = new ComboBox<>(FXCollections.observableArrayList("PERCENTUAL", "HORAS"));
        TextField horasMinimas = new TextField("0"); horasMinimas.setPromptText("Mínimo de presença em horas");
        ComboBox<String> modoCarga = new ComboBox<>(FXCollections.observableArrayList("DINAMICA", "FIXA"));
        TextField cargaFixa = new TextField("0"); cargaFixa.setPromptText("Carga fixa do certificado em horas");
        TextField fuso = new TextField();
        TextField prazo = new TextField(); prazo.setPromptText("Prazo de cancelamento ou vazio");
        CheckBox certificado = new CheckBox("Emitir certificado");
        CheckBox inscricoes = new CheckBox("Inscrições abertas");
        Label status = new Label();
        Button carregar = new Button("Carregar dados");
        Button salvar = new Button("Salvar evento");
        Button excluir = new Button("Excluir evento");
        carregar.setOnAction(e -> {
            EventRow selecionado = evento.getValue();
            if (selecionado == null) { status.setText("Selecione um evento."); return; }
            JsonNode dados = selecionado.data();
            titulo.setText(dados.path("titulo").asText());
            descricao.setText(dados.path("descricao").asText(""));
            inicio.setText(dados.path("inicio").asText());
            fim.setText(dados.path("fim").asText());
            capacidade.setText(Integer.toString(dados.path("capacidade").asInt()));
            modalidade.setValue(dados.path("modalidade").asText("PRESENCIAL"));
            statusEvento.setValue(dados.path("status").asText("RASCUNHO"));
            minimo.setValue(Integer.toString(dados.path("frequenciaMinima").asInt(75)));
            criterioFrequencia.setValue(dados.path("criterioFrequenciaCertificado").asText("PERCENTUAL"));
            horasMinimas.setText(Double.toString(dados.path("frequenciaMinimaMinutos").asInt() / 60.0));
            modoCarga.setValue(dados.path("modoCargaHorariaCertificado").asText("DINAMICA"));
            cargaFixa.setText(Double.toString(dados.path("cargaHorariaFixaMinutos").asInt() / 60.0));
            fuso.setText(dados.path("fusoHorario").asText("America/Sao_Paulo"));
            prazo.setText(dados.path("prazoCancelamento").asText(""));
            certificado.setSelected(dados.path("certificado").asBoolean());
            inscricoes.setSelected(dados.path("inscricoesAbertas").asBoolean());
            status.setText("Confira os valores e salve. A API valida vínculo de organização e período da programação.");
        });
        salvar.setOnAction(e -> {
            EventRow selecionado = evento.getValue();
            if (selecionado == null) { status.setText("Selecione um evento."); return; }
            executar(status, () -> {
                Map<String, Object> dados = new HashMap<>();
                dados.put("titulo", titulo.getText().trim());
                dados.put("descricao", descricao.getText().trim());
                dados.put("inicio", inicio.getText().trim());
                dados.put("fim", fim.getText().trim());
                dados.put("capacidade", Integer.parseInt(capacidade.getText()));
                dados.put("certificado", certificado.isSelected());
                dados.put("frequenciaMinima", Integer.parseInt(minimo.getValue()));
                dados.put("criterioFrequenciaCertificado", criterioFrequencia.getValue());
                dados.put("frequenciaMinimaMinutos", (int) Math.round(Double.parseDouble(horasMinimas.getText()) * 60));
                dados.put("modoCargaHorariaCertificado", modoCarga.getValue());
                dados.put("cargaHorariaFixaMinutos", (int) Math.round(Double.parseDouble(cargaFixa.getText()) * 60));
                dados.put("inscricoesAbertas", inscricoes.isSelected());
                dados.put("modalidade", modalidade.getValue());
                dados.put("status", statusEvento.getValue());
                dados.put("fusoHorario", fuso.getText().trim());
                dados.put("prazoCancelamento", prazo.getText().isBlank() ? null : prazo.getText().trim());
                api.put("/eventos/" + selecionado.id(), dados, session);
                preencherEventos(null);
                status.setText("Evento atualizado.");
            });
        });
        excluir.setOnAction(e -> {
            EventRow selecionado = evento.getValue();
            if (selecionado == null) { status.setText("Selecione um evento."); return; }
            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION,
                    "Excluir este evento? A operação é lógica e preserva o histórico.",
                    ButtonType.CANCEL, ButtonType.OK);
            confirmacao.showAndWait().filter(b -> b == ButtonType.OK).ifPresent(b -> executar(status, () -> {
                api.delete("/eventos/" + selecionado.id(), session);
                preencherEventos(null);
                status.setText("Evento excluído.");
            }));
        });
        VBox content = new VBox(9, evento, carregar, titulo, descricao, inicio, fim,
                capacidade, modalidade, statusEvento, certificado, minimo, criterioFrequencia,
                horasMinimas, modoCarga, cargaFixa, inscricoes,
                fuso, prazo, new HBox(8, salvar, excluir), status);
        content.setPadding(new Insets(16));
        return new ScrollPane(content);
    }

    private Node editarAtividadeForm() {
        ComboBox<EventRow> evento = new ComboBox<>();
        eventSelectors.add(evento);
        ComboBox<ActivityRow> atividade = new ComboBox<>();
        ComboBox<LocalRow> local = new ComboBox<>();
        localSelectors.add(local);
        TextField nome = new TextField();
        TextArea descricao = new TextArea(); descricao.setPrefRowCount(3);
        TextField data = new TextField();
        TextField horaInicio = new TextField();
        TextField horaFim = new TextField();
        TextField tipo = new TextField();
        TextField trilha = new TextField();
        TextField vagas = new TextField();
        CheckBox controlaVagas = new CheckBox("Limitar vagas");
        ComboBox<String> criterio = new ComboBox<>(FXCollections.observableArrayList("CHECKIN", "ENTRADA_SAIDA", "MANUAL"));
        ComboBox<String> metodoCheckin = new ComboBox<>(FXCollections.observableArrayList("QR", "NOME_CPF"));
        Label status = new Label();
        Button carregar = new Button("Carregar atividades");
        Button salvar = new Button("Salvar atividade");
        Button excluir = new Button("Excluir atividade");
        carregar.setOnAction(e -> {
            EventRow selecionado = evento.getValue();
            if (selecionado == null) { status.setText("Selecione um evento."); return; }
            executar(status, () -> {
                JsonNode resposta = api.get("/atividades/evento/" + selecionado.id(), session);
                List<ActivityRow> itens = new ArrayList<>();
                resposta.forEach(item -> itens.add(new ActivityRow(item)));
                atividade.setItems(FXCollections.observableArrayList(itens));
                if (!itens.isEmpty()) atividade.getSelectionModel().selectFirst();
                status.setText(itens.size() + " atividade(s) carregadas.");
            });
        });
        atividade.valueProperty().addListener((obs, anterior, atual) -> {
            if (atual == null) return;
            JsonNode dados = atual.data();
            nome.setText(dados.path("nome").asText());
            descricao.setText(dados.path("descricao").asText(""));
            data.setText(dados.path("data").asText());
            horaInicio.setText(dados.path("horaInicio").asText());
            horaFim.setText(dados.path("horaFim").asText());
            tipo.setText(dados.path("tipo").asText(""));
            trilha.setText(dados.path("trilha").path("nome").asText(""));
            vagas.setText(Integer.toString(dados.path("vagas").asInt()));
            controlaVagas.setSelected(dados.path("controlaVagas").asBoolean());
            criterio.setValue(dados.path("tipoFrequencia").asText("CHECKIN"));
            metodoCheckin.setValue(dados.path("metodoIdentificacaoCheckin").asText("QR"));
            long localId = dados.path("local").path("id").asLong(0);
            local.getItems().stream().filter(item -> item.id() == localId).findFirst().ifPresent(local::setValue);
        });
        salvar.setOnAction(e -> {
            EventRow eventoEscolhido = evento.getValue();
            ActivityRow atividadeEscolhida = atividade.getValue();
            if (eventoEscolhido == null || atividadeEscolhida == null) { status.setText("Selecione evento e atividade."); return; }
            executar(status, () -> {
                Map<String, Object> dados = new HashMap<>();
                dados.put("nome", nome.getText().trim());
                dados.put("descricao", descricao.getText().trim());
                dados.put("eventoId", eventoEscolhido.id());
                dados.put("data", data.getText().trim());
                dados.put("horaInicio", horaInicio.getText().trim());
                dados.put("horaFim", horaFim.getText().trim());
                dados.put("tipo", tipo.getText().trim());
                dados.put("vagas", Integer.parseInt(vagas.getText()));
                dados.put("controlaVagas", controlaVagas.isSelected());
                dados.put("tipoFrequencia", criterio.getValue());
                dados.put("metodoIdentificacaoCheckin", metodoCheckin.getValue());
                LocalRow localAtual = local.getValue();
                dados.put("local", localAtual == null ? null : Map.of("id", localAtual.id(), "nome", localAtual.nome()));
                dados.put("trilha", trilha.getText().isBlank() ? null : Map.of("nome", trilha.getText().trim()));
                JsonNode atualizada = api.put("/atividades/" + atividadeEscolhida.id(), dados, session);
                status.setText(mensagemAtividade(atualizada, "Atividade atualizada."));
                carregar.fire();
            });
        });
        excluir.setOnAction(e -> {
            ActivityRow selecionada = atividade.getValue();
            if (selecionada == null) { status.setText("Selecione uma atividade."); return; }
            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION,
                    "Excluir esta atividade? O servidor impede a exclusão quando há histórico de inscrição.",
                    ButtonType.CANCEL, ButtonType.OK);
            confirmacao.showAndWait().filter(b -> b == ButtonType.OK).ifPresent(b -> executar(status, () -> {
                api.delete("/atividades/" + selecionada.id(), session);
                status.setText("Atividade excluída.");
                carregar.fire();
            }));
        });
        VBox content = new VBox(9, evento, carregar, atividade, nome, descricao, data,
                horaInicio, horaFim, tipo, trilha, local, vagas, controlaVagas, criterio, metodoCheckin,
                new HBox(8, salvar, excluir), status);
        content.setPadding(new Insets(16));
        return new ScrollPane(content);
    }

    private Node pessoasTab() {
        ComboBox<EventRow> evento = new ComboBox<>();
        eventSelectors.add(evento);
        ComboBox<ActivityRow> atividade = new ComboBox<>();
        ListView<JsonNode> pessoasEvento = new ListView<>();
        pessoasEvento.setPrefHeight(180);
        pessoasEvento.setCellFactory(lista -> new ListCell<>() {
            @Override
            protected void updateItem(JsonNode vinculo, boolean vazio) {
                super.updateItem(vinculo, vazio);
                if (vazio || vinculo == null) {
                    setText(null);
                } else {
                    setText(vinculo.path("pessoa").path("nome").asText("Pessoa")
                            + " · " + vinculo.path("papel").asText(""));
                }
            }
        });
        TextField nome = new TextField(); nome.setPromptText("Nome da pessoa");
        TextField organizacao = new TextField(); organizacao.setPromptText("Organização");
        TextField foto = new TextField(); foto.setPromptText("URL da foto (opcional)");
        TextArea biografia = new TextArea(); biografia.setPromptText("Biografia"); biografia.setPrefRowCount(3);
        ComboBox<String> papel = new ComboBox<>(FXCollections.observableArrayList("PALESTRANTE", "APRESENTADOR", "RESPONSAVEL"));
        papel.getSelectionModel().selectFirst();
        Label status = new Label();
        Button carregar = new Button("Carregar atividades");
        Button carregarPessoas = new Button("Carregar pessoas vinculadas");
        Button baixarCertificado = new Button("Baixar certificado da pessoa selecionada");
        Button vincular = new Button("Vincular à atividade");
        carregar.setOnAction(e -> {
            EventRow selecionado = evento.getValue();
            if (selecionado == null) { status.setText("Selecione um evento."); return; }
            executar(status, () -> {
                JsonNode resposta = api.get("/atividades/evento/" + selecionado.id(), session);
                List<ActivityRow> itens = new ArrayList<>();
                resposta.forEach(item -> itens.add(new ActivityRow(item)));
                atividade.setItems(FXCollections.observableArrayList(itens));
                if (!itens.isEmpty()) atividade.getSelectionModel().selectFirst();
                status.setText(itens.size() + " atividade(s) carregadas.");
            });
        });
        carregarPessoas.setOnAction(e -> {
            EventRow selecionado = evento.getValue();
            if (selecionado == null) { status.setText("Selecione um evento."); return; }
            executar(status, () -> {
                JsonNode resposta = api.get("/eventos/" + selecionado.id() + "/pessoas-programacao", session);
                List<JsonNode> itens = new ArrayList<>();
                resposta.forEach(itens::add);
                pessoasEvento.setItems(FXCollections.observableArrayList(itens));
                status.setText(itens.size() + " vínculo(s) da programação carregado(s).");
            });
        });
        baixarCertificado.setOnAction(e -> {
            EventRow eventoSelecionado = evento.getValue();
            JsonNode vinculo = pessoasEvento.getSelectionModel().getSelectedItem();
            if (eventoSelecionado == null || vinculo == null) {
                status.setText("Selecione um evento e uma pessoa da programação."); return;
            }
            String papelCertificado = vinculo.path("papel").asText("");
            long idPessoa = vinculo.path("pessoa").path("id").asLong(0);
            if (idPessoa <= 0 || !("PALESTRANTE".equals(papelCertificado)
                    || "APRESENTADOR".equals(papelCertificado))) {
                status.setText("O certificado só está disponível para palestrantes e apresentadores."); return;
            }
            executar(status, () -> {
                byte[] pdf = api.getBytes("/eventos/" + eventoSelecionado.id()
                        + "/pessoas-programacao/" + idPessoa + "/certificado.pdf?papel=" + papelCertificado, session);
                FileChooser chooser = new FileChooser();
                chooser.setTitle("Salvar certificado da programação");
                chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivo PDF", "*.pdf"));
                chooser.setInitialFileName("certificado-evento-" + eventoSelecionado.id()
                        + "-pessoa-" + idPessoa + ".pdf");
                File destino = chooser.showSaveDialog(stage);
                if (destino != null) {
                    Files.write(destino.toPath(), pdf);
                    status.setText("Certificado salvo em " + destino.getAbsolutePath());
                }
            });
        });
        vincular.setOnAction(e -> {
            ActivityRow selecionada = atividade.getValue();
            if (selecionada == null || nome.getText().isBlank()) { status.setText("Selecione uma atividade e informe o nome."); return; }
            executar(status, () -> {
                Map<String, Object> dados = new HashMap<>();
                dados.put("nome", nome.getText().trim());
                dados.put("organizacao", organizacao.getText().trim());
                dados.put("biografia", biografia.getText().trim());
                dados.put("fotoUrl", foto.getText().trim());
                dados.put("papel", papel.getValue());
                api.post("/atividades/" + selecionada.id() + "/pessoas", dados, session);
                status.setText("Pessoa vinculada à programação.");
                nome.clear(); organizacao.clear(); biografia.clear(); foto.clear();
            });
        });
        VBox content = new VBox(10, new Label("Evento"), evento, carregar,
                carregarPessoas, pessoasEvento, baixarCertificado,
                new Label("Atividade"), atividade, nome, organizacao, papel, foto,
                biografia, vincular, status);
        content.setPadding(new Insets(16));
        return new ScrollPane(content);
    }

    private Node colaboradoresDesktopTab() {
        ComboBox<EventRow> evento = new ComboBox<>();
        eventSelectors.add(evento);
        ComboBox<ActivityRow> atividade = new ComboBox<>();
        TextField email = new TextField();
        email.setPromptText("E-mail de uma conta cadastrada");
        ListView<CollaboratorRow> colaboradores = new ListView<>();
        colaboradores.setPrefHeight(220);
        Label status = new Label("O vínculo vale apenas para a atividade selecionada.");
        Button carregarAtividades = new Button("Carregar atividades");
        Button carregarEquipe = new Button("Carregar equipe");
        Button vincular = new Button("Vincular colaborador");
        Button remover = new Button("Remover colaborador");

        carregarAtividades.setOnAction(e -> {
            EventRow selecionado = evento.getValue();
            if (selecionado == null) { status.setText("Selecione um evento."); return; }
            executar(status, () -> {
                JsonNode resposta = api.get("/atividades/evento/" + selecionado.id(), session);
                List<ActivityRow> itens = new ArrayList<>();
                resposta.forEach(item -> itens.add(new ActivityRow(item)));
                atividade.setItems(FXCollections.observableArrayList(itens));
                if (!itens.isEmpty()) atividade.getSelectionModel().selectFirst();
                status.setText(itens.size() + " atividade(s) carregadas.");
            });
        });
        carregarEquipe.setOnAction(e -> {
            ActivityRow selecionada = atividade.getValue();
            if (selecionada == null) { status.setText("Selecione uma atividade."); return; }
            executar(status, () -> {
                JsonNode resposta = api.get("/atividades/" + selecionada.id() + "/colaboradores", session);
                List<CollaboratorRow> itens = new ArrayList<>();
                resposta.forEach(item -> itens.add(new CollaboratorRow(item)));
                colaboradores.setItems(FXCollections.observableArrayList(itens));
                status.setText(itens.size() + " colaborador(es) vinculados.");
            });
        });
        vincular.setOnAction(e -> {
            ActivityRow selecionada = atividade.getValue();
            if (selecionada == null || email.getText().isBlank()) {
                status.setText("Selecione uma atividade e informe o e-mail."); return;
            }
            executar(status, () -> {
                api.post("/atividades/" + selecionada.id() + "/colaboradores",
                        Map.of("email", email.getText().trim()), session);
                email.clear();
                carregarEquipe.fire();
                status.setText("Colaborador vinculado à atividade.");
            });
        });
        remover.setOnAction(e -> {
            ActivityRow selecionada = atividade.getValue();
            CollaboratorRow colaborador = colaboradores.getSelectionModel().getSelectedItem();
            if (selecionada == null || colaborador == null) {
                status.setText("Selecione a atividade e o colaborador."); return;
            }
            executar(status, () -> {
                api.delete("/atividades/" + selecionada.id() + "/colaboradores/"
                        + colaborador.idUsuario(), session);
                carregarEquipe.fire();
                status.setText("Acesso removido desta atividade.");
            });
        });
        VBox content = new VBox(10, evento, carregarAtividades, atividade, email,
                new HBox(8, vincular, carregarEquipe, remover), colaboradores, status);
        content.setPadding(new Insets(16));
        return new ScrollPane(content);
    }

    private Node locaisTab() {
        ListView<LocalRow> lista = new ListView<>();
        TextField nome = new TextField(); nome.setPromptText("Nome do local");
        Label status = new Label();
        Button atualizar = new Button("Atualizar lista");
        Button criar = new Button("Cadastrar");
        Button alterar = new Button("Salvar alteração");
        Button excluir = new Button("Excluir");
        atualizar.setOnAction(e -> executar(status, () -> preencherListaLocais(lista)));
        lista.getSelectionModel().selectedItemProperty().addListener((obs, anterior, atual) -> {
            if (atual != null) nome.setText(atual.nome());
        });
        criar.setOnAction(e -> executar(status, () -> {
            api.post("/locais", Map.of("nome", nome.getText().trim()), session);
            nome.clear(); preencherLocais(); preencherListaLocais(lista); status.setText("Local cadastrado.");
        }));
        alterar.setOnAction(e -> {
            LocalRow selecionado = lista.getSelectionModel().getSelectedItem();
            if (selecionado == null) { status.setText("Selecione um local."); return; }
            executar(status, () -> {
                api.put("/locais/" + selecionado.id(), Map.of("nome", nome.getText().trim()), session);
                preencherLocais(); preencherListaLocais(lista); status.setText("Local atualizado.");
            });
        });
        excluir.setOnAction(e -> {
            LocalRow selecionado = lista.getSelectionModel().getSelectedItem();
            if (selecionado == null) { status.setText("Selecione um local."); return; }
            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION,
                    "Excluir o local? Atividades existentes manterão o nome histórico.", ButtonType.CANCEL, ButtonType.OK);
            confirmacao.showAndWait().filter(b -> b == ButtonType.OK).ifPresent(b -> executar(status, () -> {
                api.delete("/locais/" + selecionado.id(), session);
                preencherLocais(); preencherListaLocais(lista); nome.clear(); status.setText("Local excluído.");
            }));
        });
        executar(status, () -> preencherListaLocais(lista));
        VBox content = new VBox(10, new Label("Locais usados pela programação"), lista, nome,
                new HBox(8, atualizar, criar, alterar, excluir), status);
        content.setPadding(new Insets(16));
        return content;
    }

    private void preencherListaLocais(ListView<LocalRow> destino) throws Exception {
        JsonNode resultado = api.get("/locais", session);
        List<LocalRow> itens = new ArrayList<>();
        resultado.forEach(item -> itens.add(new LocalRow(item.path("id").asLong(), item.path("nome").asText())));
        destino.setItems(FXCollections.observableArrayList(itens));
    }

    private Node criarEventoForm() {
        TextField titulo = new TextField(); titulo.setPromptText("Título");
        TextArea descricao = new TextArea(); descricao.setPromptText("Descrição");
        TextField inicio = new TextField("2026-10-01T08:00:00"); inicio.setPromptText("Início: AAAA-MM-DDTHH:mm:ss");
        TextField fim = new TextField("2026-10-01T18:00:00"); fim.setPromptText("Fim: AAAA-MM-DDTHH:mm:ss");
        TextField capacidade = new TextField("100");
        ComboBox<String> modalidade = new ComboBox<>(FXCollections.observableArrayList("PRESENCIAL", "ONLINE", "HIBRIDO"));
        modalidade.getSelectionModel().selectFirst();
        ComboBox<String> statusEvento = new ComboBox<>(FXCollections.observableArrayList("RASCUNHO", "PUBLICADO"));
        statusEvento.getSelectionModel().select("RASCUNHO");
        ComboBox<String> minimo = new ComboBox<>(FXCollections.observableArrayList("50", "75", "100"));
        minimo.getSelectionModel().select("75");
        ComboBox<String> criterioFrequencia = new ComboBox<>(FXCollections.observableArrayList("PERCENTUAL", "HORAS"));
        criterioFrequencia.getSelectionModel().select("PERCENTUAL");
        TextField horasMinimas = new TextField("0"); horasMinimas.setPromptText("Mínimo de presença em horas");
        ComboBox<String> modoCarga = new ComboBox<>(FXCollections.observableArrayList("DINAMICA", "FIXA"));
        modoCarga.getSelectionModel().select("DINAMICA");
        TextField cargaFixa = new TextField("0"); cargaFixa.setPromptText("Carga fixa do certificado em horas");
        TextField prazoCancelamento = new TextField();
        prazoCancelamento.setPromptText("Prazo para cancelar: AAAA-MM-DDTHH:mm (opcional)");
        CheckBox certificado = new CheckBox("Emitir certificado conforme frequência mínima");
        CheckBox inscricoes = new CheckBox("Abrir inscrições");
        Label status = new Label();
        Button salvar = new Button("Criar evento");
        salvar.setOnAction(e -> executar(status, () -> {
            Map<String, Object> dados = new HashMap<>();
            dados.put("titulo", titulo.getText().trim());
            dados.put("descricao", descricao.getText().trim());
            dados.put("inicio", inicio.getText().trim());
            dados.put("fim", fim.getText().trim());
            dados.put("capacidade", Integer.parseInt(capacidade.getText()));
            dados.put("certificado", certificado.isSelected());
            dados.put("frequenciaMinima", Integer.parseInt(minimo.getValue()));
            dados.put("criterioFrequenciaCertificado", criterioFrequencia.getValue());
            dados.put("frequenciaMinimaMinutos", (int) Math.round(Double.parseDouble(horasMinimas.getText()) * 60));
            dados.put("modoCargaHorariaCertificado", modoCarga.getValue());
            dados.put("cargaHorariaFixaMinutos", (int) Math.round(Double.parseDouble(cargaFixa.getText()) * 60));
            dados.put("inscricoesAbertas", inscricoes.isSelected());
            dados.put("modalidade", modalidade.getValue());
            dados.put("status", statusEvento.getValue());
            dados.put("fusoHorario", "America/Sao_Paulo");
            dados.put("prazoCancelamento", prazoCancelamento.getText().isBlank() ? null : prazoCancelamento.getText().trim());
            api.post("/eventos", dados, session);
            preencherEventos(null);
            status.setText("Evento criado e você foi vinculado como organizador.");
        }));
        VBox content = new VBox(9, titulo, descricao, inicio, fim, capacidade,
                modalidade, statusEvento, certificado, minimo, criterioFrequencia, horasMinimas,
                modoCarga, cargaFixa, inscricoes, prazoCancelamento, salvar, status);
        content.setPadding(new Insets(16));
        return new ScrollPane(content);
    }

    private Node criarAtividadeForm() {
        ComboBox<EventRow> evento = new ComboBox<>(FXCollections.observableArrayList(events));
        eventSelectors.add(evento);
        TextField nome = new TextField(); nome.setPromptText("Nome da atividade");
        TextArea descricao = new TextArea(); descricao.setPromptText("Descrição");
        TextField data = new TextField(); data.setPromptText("Data: AAAA-MM-DD");
        TextField horaInicio = new TextField(); horaInicio.setPromptText("Início: HH:mm");
        TextField horaFim = new TextField(); horaFim.setPromptText("Fim: HH:mm");
        ComboBox<LocalRow> local = new ComboBox<>();
        local.setPromptText("Selecione um local cadastrado (opcional)");
        localSelectors.add(local);
        TextField trilha = new TextField(); trilha.setPromptText("Trilha");
        TextField categoria = new TextField("Palestra"); categoria.setPromptText("Categoria");
        TextField vagas = new TextField("0"); vagas.setPromptText("0 = ilimitadas");
        ComboBox<String> criterio = new ComboBox<>(FXCollections.observableArrayList("CHECKIN", "ENTRADA_SAIDA", "MANUAL"));
        criterio.getSelectionModel().selectFirst();
        ComboBox<String> metodoCheckin = new ComboBox<>(FXCollections.observableArrayList("QR", "NOME_CPF"));
        metodoCheckin.getSelectionModel().selectFirst();
        Label status = new Label();
        Button salvar = new Button("Criar atividade");
        salvar.setOnAction(e -> {
            EventRow escolhido = evento.getValue();
            if (escolhido == null) { status.setText("Selecione um evento que você organiza."); return; }
            executar(status, () -> {
                int numeroVagas = Integer.parseInt(vagas.getText());
                Map<String, Object> dados = new HashMap<>();
                dados.put("nome", nome.getText().trim());
                dados.put("descricao", descricao.getText().trim());
                dados.put("eventoId", escolhido.id());
                dados.put("data", data.getText().trim());
                dados.put("horaInicio", horaInicio.getText().trim() + ":00");
                dados.put("horaFim", horaFim.getText().trim() + ":00");
                dados.put("tipo", categoria.getText().trim());
                dados.put("controlaVagas", numeroVagas > 0);
                dados.put("vagas", Math.max(0, numeroVagas));
                dados.put("tipoFrequencia", criterio.getValue());
                dados.put("metodoIdentificacaoCheckin", metodoCheckin.getValue());
                LocalRow localSelecionado = local.getValue();
                dados.put("local", localSelecionado == null ? null
                        : Map.of("id", localSelecionado.id(), "nome", localSelecionado.nome()));
                dados.put("trilha", trilha.getText().isBlank() ? null : Map.of("nome", trilha.getText().trim()));
                JsonNode criada = api.post("/atividades", dados, session);
                status.setText(mensagemAtividade(criada, "Atividade criada."));
            });
        });
        VBox content = new VBox(9, evento, nome, descricao, data, horaInicio, horaFim, categoria,
                local, trilha, vagas, criterio, metodoCheckin, salvar, status);
        content.setPadding(new Insets(16));
        return new ScrollPane(content);
    }

    private void refreshData() {
        try {
            preencherEventos(null);
            preencherLocais();
            carregarInscricoes();
        } catch (Exception ignored) {
            // Each screen displays its own API errors when refreshed.
        }
    }

    private void preencherEventos(ListView<EventRow> destino) throws Exception {
        JsonNode resultado = api.get("/eventos", session);
        List<EventRow> itens = new ArrayList<>();
        resultado.forEach(item -> itens.add(new EventRow(item)));
        events = itens;
        if (destino != null && !eventLists.contains(destino)) eventLists.add(destino);
        eventLists.forEach(lista -> lista.setItems(FXCollections.observableArrayList(itens)));
        eventSelectors.forEach(seletor -> seletor.setItems(FXCollections.observableArrayList(itens)));
    }

    private void preencherLocais() throws Exception {
        JsonNode resultado = api.get("/locais", session);
        List<LocalRow> itens = new ArrayList<>();
        resultado.forEach(item -> itens.add(new LocalRow(item.path("id").asLong(), item.path("nome").asText())));
        localSelectors.forEach(seletor -> seletor.setItems(FXCollections.observableArrayList(itens)));
    }

    private void carregarInscricoes() throws Exception {
        carregarInscricoes(null);
    }

    private void carregarInscricoes(ComboBox<RegistrationRow> destino) throws Exception {
        JsonNode resultado = api.get("/inscricoes/eventos/usuario/" + session.id(), session);
        List<RegistrationRow> itens = new ArrayList<>();
        resultado.forEach(item -> itens.add(new RegistrationRow(item)));
        registrations = itens;
        if (destino != null && !registrationSelectors.contains(destino)) registrationSelectors.add(destino);
        registrationSelectors.forEach(seletor -> seletor.setItems(FXCollections.observableArrayList(itens)));
    }

    private RegistrationRow registrationForEvent(long eventId) {
        return registrations.stream().filter(item -> item.idEvento() == eventId).findFirst().orElse(null);
    }

    private void executar(Label status, CheckedAction action) {
        try {
            action.run();
        } catch (ApiException e) {
            status.setText("API (" + e.status() + "): " + e.getMessage());
        } catch (Exception e) {
            status.setText(e.getMessage() == null ? "Falha ao executar a operação." : e.getMessage());
        }
    }

    private String mensagemAtividade(JsonNode resposta, String sucesso) {
        JsonNode avisos = resposta.path("avisos");
        if (!avisos.isArray() || avisos.size() == 0) return sucesso;
        List<String> textos = new ArrayList<>();
        avisos.forEach(aviso -> textos.add(aviso.asText()));
        return sucesso + " Aviso: " + String.join(" ", textos);
    }

    private Node centralizar(Node node) {
        StackPane pane = new StackPane(node);
        pane.setPadding(new Insets(18));
        return pane;
    }

    private interface CheckedAction { void run() throws Exception; }
    private record EventRow(JsonNode data) {
        long id() { return data.path("id").asLong(); }
        String label() { return data.path("titulo").asText("Evento") + " · " + data.path("inicio").asText(""); }
        @Override public String toString() { return label(); }
    }
    private record ActivityRow(JsonNode data) {
        long id() { return data.path("id").asLong(); }
        @Override public String toString() {
            return data.path("nome").asText("Atividade") + " · " + data.path("tipoFrequencia").asText("CHECKIN");
        }
    }
    private record CheckinActivityRow(JsonNode data) {
        long idAtividade() { return data.path("idAtividade").asLong(); }
        String politica() { return data.path("tipoFrequencia").asText("CHECKIN"); }
        String metodo() { return data.path("metodoIdentificacaoCheckin").asText("QR"); }
        @Override public String toString() {
            return data.path("tituloEvento").asText("Evento") + " · "
                    + data.path("nomeAtividade").asText("Atividade") + " · "
                    + data.path("data").asText("") + " "
                    + data.path("horaInicio").asText("") + "–" + data.path("horaFim").asText("");
        }
    }
    private record CheckinParticipantRow(JsonNode data) {
        long idInscricaoEvento() { return data.path("idInscricaoEvento").asLong(); }
        @Override public String toString() {
            return data.path("nome").asText("Participante") + " · "
                    + data.path("cpfMascarado").asText("CPF não cadastrado");
        }
    }
    private record AttendanceRow(JsonNode data) {
        long idInscricaoEvento() { return data.path("idInscricaoEvento").asLong(); }
        boolean presente() { return data.path("presente").asBoolean(); }
        @Override public String toString() {
            return data.path("nome").asText("Participante") + " · "
                    + data.path("cpfMascarado").asText("CPF não cadastrado") + " · "
                    + (presente() ? "Presente" : "Ausente / sem validação");
        }
    }
    private record CollaboratorRow(JsonNode data) {
        long idUsuario() { return data.path("idUsuario").asLong(); }
        @Override public String toString() {
            return data.path("nome").asText("Colaborador") + " · " + data.path("email").asText("");
        }
    }
    private record RegistrationRow(JsonNode data) {
        long id() { return data.path("id").asLong(); }
        long idEvento() { return data.path("idEvento").asLong(); }
        boolean cancelada() { return data.path("cancelada").asBoolean(); }
        @Override public String toString() {
            return "Evento #" + idEvento() + " · inscrição #" + id() + (cancelada() ? " (cancelada)" : "");
        }
    }
    private record LocalRow(long id, String nome) {
        @Override public String toString() { return nome; }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
