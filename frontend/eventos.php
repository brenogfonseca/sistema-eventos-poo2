<?php
/**
 * eventos.php — Módulo de Eventos: Listagem pública de eventos
 * ─────────────────────────────────────────────────────────────────────────────
 * FLUXO DIDÁTICO:
 *   1. Ao carregar a página, este PHP chama chamar_api('GET', '/eventos').
 *   2. A função cURL busca a lista de eventos na API Java.
 *   3. O PHP itera sobre o array retornado e renderiza um card para cada evento.
 *   4. Usuários logados veem botão "Ver Atividades" (acesso ao módulo interno).
 *   5. Administradores veem também o botão "Excluir" — mas este front-end
 *      didático não implementa exclusão de eventos (apenas criação e listagem,
 *      conforme o EventoController.java que só tem GET e POST).
 *
 * Endpoint utilizado:
 *   GET /eventos
 *   Retorna: array de objetos Evento com campos do domínio.
 * ─────────────────────────────────────────────────────────────────────────────
 */
require_once __DIR__ . '/api_service.php';
iniciar_sessao();

// 1. Busca a lista de eventos na API Java
//    GET /eventos — endpoint público, não precisa de header X-Usuario-Id
$resultado = chamar_api('GET', '/eventos');

$eventos = [];
$erroApi = '';

if ($resultado['erro']) {
    // Houve erro de conexão ou resposta 4xx/5xx
    $erroApi = $resultado['msg_erro'];
} else {
    // Sucesso: $resultado['corpo'] é um array de eventos (decodificado do JSON)
    $eventos = $resultado['corpo'] ?? [];
}

// ── Monta o conteúdo da página ────────────────────────────────────────────────
ob_start();
?>

<!-- Cabeçalho da seção -->
<div class="secao-cabecalho">
    <div>
        <h1 class="secao-titulo">📅 Eventos</h1>
        <p class="secao-subtitulo">Confira os eventos disponíveis</p>
    </div>

    <?php if (eh_administrador()): ?>
        <!-- Apenas administradores podem criar eventos -->
        <a href="criar_evento.php" class="btn btn-primario" id="btn-criar-evento">
            + Criar Novo Evento
        </a>
    <?php endif; ?>
</div>

<!-- Mensagem de erro de API -->
<?php if ($erroApi): ?>
    <div class="alerta alerta-erro">
        ⚠️ <?= htmlspecialchars($erroApi) ?>
        <br><small>Verifique se o servidor Java está rodando em http://localhost:8080</small>
    </div>
<?php endif; ?>

<!-- Mensagem se não há eventos -->
<?php if (empty($eventos) && !$erroApi): ?>
    <div class="card text-center" style="padding: 3rem;">
        <p style="font-size: 2rem;">📭</p>
        <p class="text-muted">Nenhum evento cadastrado ainda.</p>
        <?php if (eh_administrador()): ?>
            <a href="criar_evento.php" class="btn btn-primario mt-md">Criar o primeiro evento</a>
        <?php endif; ?>
    </div>
<?php endif; ?>

<!-- ── GRID DE CARDS DE EVENTOS ──────────────────────────────────────────────── -->
<div class="grid-eventos">
    <?php foreach ($eventos as $evento):
        // Formata as datas para exibição legível
        // A API retorna no formato ISO 8601: "2026-10-01T08:00:00"
        $dataInicio  = isset($evento['inicio']) ? date('d/m/Y H:i', strtotime($evento['inicio'])) : '—';
        $dataFim     = isset($evento['fim'])    ? date('d/m/Y H:i', strtotime($evento['fim']))    : '—';
        $capacidade  = $evento['capacidade'] ?? '—';
        $titulo      = $evento['titulo']      ?? 'Sem título';
        $descricao   = $evento['descricao']   ?? '';
        $idEvento    = $evento['id']           ?? null;
    ?>
        <div class="card-evento" id="evento-<?= (int)$idEvento ?>">
            <h3><?= htmlspecialchars($titulo) ?></h3>

            <div class="evento-meta">
                <span>📆 <?= htmlspecialchars($dataInicio) ?> → <?= htmlspecialchars($dataFim) ?></span>
                <span>👥 Capacidade: <?= htmlspecialchars((string)$capacidade) ?></span>
            </div>

            <?php if ($descricao): ?>
                <p class="evento-descricao">
                    <?= htmlspecialchars(mb_strimwidth($descricao, 0, 120, '...')) ?>
                </p>
            <?php endif; ?>

            <div class="card-evento-acoes">
                <!-- Botão para ver atividades do evento -->
                <a href="atividades.php?evento_id=<?= (int)$idEvento ?>"
                   class="btn btn-primario"
                   id="btn-ver-atividades-<?= (int)$idEvento ?>">
                    Ver Atividades
                </a>
            </div>
        </div>
    <?php endforeach; ?>
</div>

<?php
$conteudo    = ob_get_clean();
$titulo      = 'Eventos';
$paginaAtiva = 'eventos';
include __DIR__ . '/layout.php';
