<?php
/**
 * editar_evento.php — Módulo de Eventos: Formulário de edição/atualização
 * ─────────────────────────────────────────────────────────────────────────────
 * FLUXO DIDÁTICO E SEGURANÇA:
 *   1. Exige usuário autenticado (exigir_login()).
 *   2. Carrega os dados do evento da API: GET /eventos/{id}.
 *   3. Lógica de barreira visual / permissão:
 *      Apenas o Administrador ou o próprio Organizador/Criador do evento podem acessar.
 *   4. Ao submeter via POST, este PHP envia:
 *      PUT /eventos/{id} com headers_autenticados() e usuarioExecutorId.
 *   5. Se a API recusar (HTTP 403 / 400), a mensagem é exibida em alerta.
 *   6. Em caso de sucesso, redireciona para a listagem com mensagem de sucesso.
 * ─────────────────────────────────────────────────────────────────────────────
 */
require_once __DIR__ . '/api_service.php';
exigir_login();

$idEvento = (int)($_GET['id'] ?? $_POST['id_evento'] ?? 0);
if ($idEvento <= 0) {
    header('Location: eventos.php');
    exit;
}

$idUsuarioLogado = id_usuario_logado();
$ehAdmin = eh_administrador();

$erro    = '';
$sucesso = '';

// 1. Busca os dados atuais do evento na API Java
$respEvento = chamar_api('GET', "/eventos/{$idEvento}", null, headers_autenticados());
if ($respEvento['erro']) {
    $erro = 'Evento não encontrado: ' . $respEvento['msg_erro'];
    $eventoAtual = null;
} else {
    $eventoAtual = $respEvento['corpo'];
}

// 2. Validação de segurança no front-end: checa se é Admin ou se o ID logado está entre os organizadores
if ($eventoAtual) {
    $organizadores = $eventoAtual['organizadoresIds'] ?? [];
    $ehOrganizador = in_array($idUsuarioLogado, $organizadores);

    // Se não for admin e nem organizador do evento, bloqueia o acesso à tela de edição
    if (!$ehAdmin && !$ehOrganizador) {
        header('Location: eventos.php?negado=1');
        exit;
    }
}

// 3. Processamento do formulário de atualização (PUT)
if ($_SERVER['REQUEST_METHOD'] === 'POST' && $eventoAtual) {
    $titulo     = trim($_POST['titulo']     ?? '');
    $descricao  = trim($_POST['descricao']  ?? '');
    $dataInicio = trim($_POST['dataInicio'] ?? '');
    $horaInicio = trim($_POST['horaInicio'] ?? '');
    $dataFim    = trim($_POST['dataFim']    ?? '');
    $horaFim    = trim($_POST['horaFim']    ?? '');
    $capacidade = (int)($_POST['capacidade'] ?? 0);

    if (empty($titulo) || empty($dataInicio) || empty($dataFim)) {
        $erro = 'Título, data de início e data de fim são obrigatórios.';
    } elseif ($capacidade <= 0) {
        $erro = 'A capacidade deve ser um número maior que zero.';
    } else {
        // Converte para ISO 8601 (LocalDateTime)
        $inicioISO = $dataInicio . 'T' . ($horaInicio ?: '00:00') . ':00';
        $fimISO    = $dataFim    . 'T' . ($horaFim    ?: '23:59') . ':00';

        // Prepara os dados incluindo o usuarioExecutorId no corpo
        $dados = [
            'titulo'            => $titulo,
            'descricao'         => $descricao,
            'inicio'            => $inicioISO,
            'fim'               => $fimISO,
            'capacidade'        => $capacidade,
            'usuarioExecutorId' => $idUsuarioLogado, // Enviado no corpo JSON para a API Java
        ];

        // Dispara PUT /eventos/{id} com header X-Usuario-Id
        $resultado = chamar_api('PUT', "/eventos/{$idEvento}", $dados, headers_autenticados());

        if ($resultado['erro']) {
            $erro = $resultado['msg_erro'] ?: 'Erro ao atualizar evento.';
        } else {
            header('Location: eventos.php?atualizado=1');
            exit;
        }
    }
}

// Prepara valores padrão para o formulário
$valTitulo = $_POST['titulo'] ?? $eventoAtual['titulo'] ?? '';
$valDescricao = $_POST['descricao'] ?? $eventoAtual['descricao'] ?? '';
$valCapacidade = $_POST['capacidade'] ?? $eventoAtual['capacidade'] ?? '';

$rawInicio = $eventoAtual['inicio'] ?? '';
$rawFim = $eventoAtual['fim'] ?? '';

$valDataInicio = $_POST['dataInicio'] ?? ($rawInicio ? date('Y-m-d', strtotime($rawInicio)) : '');
$valHoraInicio = $_POST['horaInicio'] ?? ($rawInicio ? date('H:i', strtotime($rawInicio)) : '08:00');
$valDataFim    = $_POST['dataFim']    ?? ($rawFim ? date('Y-m-d', strtotime($rawFim)) : '');
$valHoraFim    = $_POST['horaFim']    ?? ($rawFim ? date('H:i', strtotime($rawFim)) : '18:00');

ob_start();
?>

<div class="secao-cabecalho">
    <div>
        <h1 class="secao-titulo">✏️ Editar Evento</h1>
        <p class="secao-subtitulo">Atualize os dados do evento #<?= (int)$idEvento ?></p>
    </div>
    <a href="eventos.php" class="btn btn-secundario">← Voltar</a>
</div>

<div class="card" style="max-width: 680px;">

    <?php if ($erro): ?>
        <div class="alerta alerta-erro">⚠️ <?= htmlspecialchars($erro) ?></div>
    <?php endif; ?>

    <?php if ($eventoAtual): ?>
    <form method="POST" action="editar_evento.php?id=<?= (int)$idEvento ?>" id="form-editar-evento">
        <input type="hidden" name="id_evento" value="<?= (int)$idEvento ?>">

        <div class="form-grupo">
            <label for="titulo">Título do Evento *</label>
            <input
                type="text"
                id="titulo"
                name="titulo"
                value="<?= htmlspecialchars($valTitulo) ?>"
                required
            >
        </div>

        <div class="form-grupo">
            <label for="descricao">Descrição</label>
            <textarea
                id="descricao"
                name="descricao"
                rows="3"
            ><?= htmlspecialchars($valDescricao) ?></textarea>
        </div>

        <!-- Início -->
        <div class="form-linha">
            <div class="form-grupo">
                <label for="dataInicio">Data de Início *</label>
                <input
                    type="date"
                    id="dataInicio"
                    name="dataInicio"
                    value="<?= htmlspecialchars($valDataInicio) ?>"
                    required
                >
            </div>
            <div class="form-grupo">
                <label for="horaInicio">Hora de Início</label>
                <input
                    type="time"
                    id="horaInicio"
                    name="horaInicio"
                    value="<?= htmlspecialchars($valHoraInicio) ?>"
                >
            </div>
        </div>

        <!-- Fim -->
        <div class="form-linha">
            <div class="form-grupo">
                <label for="dataFim">Data de Fim *</label>
                <input
                    type="date"
                    id="dataFim"
                    name="dataFim"
                    value="<?= htmlspecialchars($valDataFim) ?>"
                    required
                >
            </div>
            <div class="form-grupo">
                <label for="horaFim">Hora de Fim</label>
                <input
                    type="time"
                    id="horaFim"
                    name="horaFim"
                    value="<?= htmlspecialchars($valHoraFim) ?>"
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
                value="<?= htmlspecialchars((string)$valCapacidade) ?>"
                required
            >
        </div>

        <div class="d-flex gap-sm">
            <button type="submit" class="btn btn-primario" id="btn-atualizar-evento">
                💾 Salvar Alterações
            </button>
            <a href="eventos.php" class="btn btn-secundario">Cancelar</a>
        </div>
    </form>
    <?php endif; ?>
</div>

<?php
$conteudo    = ob_get_clean();
$titulo      = 'Editar Evento';
$paginaAtiva = 'eventos';
include __DIR__ . '/layout.php';
