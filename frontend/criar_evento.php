<?php
/**
 * criar_evento.php — Módulo de Eventos: Formulário de criação
 * ─────────────────────────────────────────────────────────────────────────────
 * FLUXO DIDÁTICO:
 *   1. Apenas administradores podem acessar esta página (exigir_login() + check).
 *   2. O formulário coleta: título, descrição, data/hora início e fim, capacidade.
 *   3. Ao submeter via POST, este PHP:
 *      a. Captura os campos de $_POST.
 *      b. Formata as datas para o padrão ISO 8601 (ex: "2026-10-01T08:00:00"),
 *         que é o que LocalDateTime.parse() espera no Java.
 *      c. Monta o array $dados e chama chamar_api('POST', '/eventos', $dados).
 *      d. A função cURL serializa para JSON e envia ao Javalin.
 *   4. Em caso de sucesso (HTTP 201), redireciona para a lista de eventos.
 *
 * Endpoint utilizado:
 *   POST /eventos
 *   Corpo: { "titulo", "descricao", "inicio", "fim", "capacidade" }
 * ─────────────────────────────────────────────────────────────────────────────
 */
require_once __DIR__ . '/api_service.php';
exigir_login(); // Redireciona para login.php se não estiver logado

// Apenas administradores podem criar eventos
if (!eh_administrador()) {
    header('Location: eventos.php');
    exit;
}

$erro    = '';
$sucesso = '';

// ── Processa o formulário quando enviado via POST ─────────────────────────────
if ($_SERVER['REQUEST_METHOD'] === 'POST') {

    // 1. Captura os campos do formulário
    $titulo     = trim($_POST['titulo']     ?? '');
    $descricao  = trim($_POST['descricao']  ?? '');
    $dataInicio = trim($_POST['dataInicio'] ?? ''); // Ex: "2026-10-01"
    $horaInicio = trim($_POST['horaInicio'] ?? ''); // Ex: "08:00"
    $dataFim    = trim($_POST['dataFim']    ?? '');
    $horaFim    = trim($_POST['horaFim']    ?? '');
    $capacidade = (int)($_POST['capacidade'] ?? 0);

    // 2. Validação básica antes de chamar a API
    if (empty($titulo) || empty($dataInicio) || empty($dataFim)) {
        $erro = 'Título, data de início e data de fim são obrigatórios.';

    } elseif ($capacidade <= 0) {
        $erro = 'A capacidade deve ser um número maior que zero.';

    } else {
        // 3. Formata as datas para ISO 8601 (LocalDateTime do Java)
        //    O Java espera: "2026-10-01T08:00:00"
        $inicioISO = $dataInicio . 'T' . ($horaInicio ?: '00:00') . ':00';
        $fimISO    = $dataFim    . 'T' . ($horaFim    ?: '23:59') . ':00';

        // 4. Monta o array de dados — exatamente o que CriarEventoRequest.java espera
        $dados = [
            'titulo'     => $titulo,
            'descricao'  => $descricao,
            'inicio'     => $inicioISO,   // "2026-10-01T08:00:00"
            'fim'        => $fimISO,       // "2026-10-03T18:00:00"
            'capacidade' => $capacidade,
        ];

        // 5. Dispara a requisição: POST /eventos
        //    O PHP converte $dados para JSON e o cURL envia ao servidor Java
        $resultado = chamar_api('POST', '/eventos', $dados);

        if ($resultado['erro']) {
            $erro = $resultado['msg_erro'] ?: 'Erro ao criar evento. Verifique os dados.';
        } else {
            // Sucesso! Redireciona para a lista de eventos
            header('Location: eventos.php?criado=1');
            exit;
        }
    }
}

// ── Monta o HTML da página ────────────────────────────────────────────────────
ob_start();
?>

<div class="secao-cabecalho">
    <div>
        <h1 class="secao-titulo">➕ Criar Novo Evento</h1>
        <p class="secao-subtitulo">Preencha os dados do evento abaixo</p>
    </div>
    <a href="eventos.php" class="btn btn-secundario">← Voltar</a>
</div>

<div class="card" style="max-width: 680px;">

    <!-- Mensagem de erro -->
    <?php if ($erro): ?>
        <div class="alerta alerta-erro">⚠️ <?= htmlspecialchars($erro) ?></div>
    <?php endif; ?>

    <!-- ── FORMULÁRIO DE CRIAÇÃO DE EVENTO ───────────────────────────────── -->
    <!--
        Os campos abaixo correspondem ao que a API Java espera em
        EventoController.CriarEventoRequest:
        { titulo, descricao, inicio (ISO 8601), fim (ISO 8601), capacidade }
    -->
    <form method="POST" action="criar_evento.php" id="form-criar-evento">

        <div class="form-grupo">
            <label for="titulo">Título do Evento *</label>
            <input
                type="text"
                id="titulo"
                name="titulo"
                placeholder="Ex: Symposium de POO 2026"
                value="<?= htmlspecialchars($_POST['titulo'] ?? '') ?>"
                required
            >
        </div>

        <div class="form-grupo">
            <label for="descricao">Descrição</label>
            <textarea
                id="descricao"
                name="descricao"
                placeholder="Descreva o evento brevemente..."
            ><?= htmlspecialchars($_POST['descricao'] ?? '') ?></textarea>
        </div>

        <!-- Data e hora de início lado a lado -->
        <div class="form-linha">
            <div class="form-grupo">
                <label for="dataInicio">Data de Início *</label>
                <input
                    type="date"
                    id="dataInicio"
                    name="dataInicio"
                    value="<?= htmlspecialchars($_POST['dataInicio'] ?? '') ?>"
                    required
                >
            </div>
            <div class="form-grupo">
                <label for="horaInicio">Hora de Início</label>
                <input
                    type="time"
                    id="horaInicio"
                    name="horaInicio"
                    value="<?= htmlspecialchars($_POST['horaInicio'] ?? '08:00') ?>"
                >
            </div>
        </div>

        <!-- Data e hora de fim lado a lado -->
        <div class="form-linha">
            <div class="form-grupo">
                <label for="dataFim">Data de Fim *</label>
                <input
                    type="date"
                    id="dataFim"
                    name="dataFim"
                    value="<?= htmlspecialchars($_POST['dataFim'] ?? '') ?>"
                    required
                >
            </div>
            <div class="form-grupo">
                <label for="horaFim">Hora de Fim</label>
                <input
                    type="time"
                    id="horaFim"
                    name="horaFim"
                    value="<?= htmlspecialchars($_POST['horaFim'] ?? '18:00') ?>"
                >
            </div>
        </div>

        <div class="form-grupo">
            <label for="capacidade">Capacidade (nº de participantes) *</label>
            <input
                type="number"
                id="capacidade"
                name="capacidade"
                min="1"
                placeholder="Ex: 100"
                value="<?= htmlspecialchars($_POST['capacidade'] ?? '') ?>"
                required
            >
        </div>

        <div class="d-flex gap-sm">
            <button type="submit" class="btn btn-primario" id="btn-salvar-evento">
                ✅ Salvar Evento
            </button>
            <a href="eventos.php" class="btn btn-secundario">Cancelar</a>
        </div>

    </form>
</div>

<?php
$conteudo    = ob_get_clean();
$titulo      = 'Criar Evento';
$paginaAtiva = 'criar_evento';
include __DIR__ . '/layout.php';
