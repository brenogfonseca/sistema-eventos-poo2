<?php
/**
 * eventos.php — Módulo de Eventos: Listagem pública e gerenciamento de eventos
 * ─────────────────────────────────────────────────────────────────────────────
 * FLUXO DIDÁTICO E SEGURANÇA:
 *   1. Busca a lista de eventos na API Java (GET /eventos). Cada evento traz
 *      a lista de organizadoresIds vinculados ao evento.
 *   2. Usuário logado:
 *      - Pode criar novo evento (+ Criar Novo Evento disponível para todos logados).
 *      - Tem acesso às atividades do evento ("Ver Atividades").
 *   3. Interface Condicional (Proteção Visual):
 *      - Para cada evento, o PHP verifica se o usuário logado é ADMINISTRADOR
 *        ou se o seu ID está na lista de organizadores do evento.
 *      - Se NÃO for administrador e NEM organizador, os botões de "Editar" e "Excluir"
 *        ficam OCULTOS, evitando tentativas indevidas.
 *   4. Ao acionar "Excluir", o formulário envia POST para esta página que chama:
 *      DELETE /eventos/{id} passando usuarioExecutorId e header X-Usuario-Id.
 * ─────────────────────────────────────────────────────────────────────────────
 */
require_once __DIR__ . '/api_service.php';
iniciar_sessao();

$usuarioAtual = usuario_logado();
$idUsuarioLogado = id_usuario_logado();
$ehAdmin = eh_administrador();

$mensagemSucesso = '';
$mensagemErro = '';

// Trata mensagens passadas por query string
if (isset($_GET['criado'])) {
    $mensagemSucesso = 'Evento criado com sucesso! Você é o organizador principal.';
} elseif (isset($_GET['atualizado'])) {
    $mensagemSucesso = 'Evento atualizado com sucesso!';
} elseif (isset($_GET['negado'])) {
    $mensagemErro = 'Acesso negado: Você não tem permissão para alterar este evento.';
}

// ── Processamento de exclusão de evento via POST ─────────────────────────────
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['acao']) && $_POST['acao'] === 'excluir') {
    exigir_login();
    $idEventoExcluir = (int)($_POST['id_evento'] ?? 0);

    if ($idEventoExcluir > 0) {
        // Envia usuarioExecutorId no corpo JSON e no header X-Usuario-Id
        $dados = [
            'usuarioExecutorId' => $idUsuarioLogado
        ];

        $respDelete = chamar_api(
            'DELETE',
            "/eventos/{$idEventoExcluir}",
            $dados,
            headers_autenticados()
        );

        if ($respDelete['erro']) {
            $mensagemErro = $respDelete['msg_erro'] ?: 'Não foi possível excluir o evento.';
        } else {
            $mensagemSucesso = "Evento #{$idEventoExcluir} excluído com sucesso!";
        }
    }
}

// 1. Busca a lista de eventos na API Java
$resultado = chamar_api('GET', '/eventos', null, headers_autenticados());

$eventos = [];
$erroApi = '';

if ($resultado['erro']) {
    $erroApi = $resultado['msg_erro'];
} else {
    $eventos = $resultado['corpo'] ?? [];
}

ob_start();
?>

<!-- Cabeçalho da seção -->
<div class="secao-cabecalho">
    <div>
        <h1 class="secao-titulo">📅 Eventos</h1>
        <p class="secao-subtitulo">Confira os eventos disponíveis e gerencie seus eventos</p>
    </div>

    <?php if ($usuarioAtual): ?>
        <!-- Qualquer usuário autenticado pode criar eventos e se tornar organizador -->
        <a href="criar_evento.php" class="btn btn-primario" id="btn-criar-evento">
            + Criar Novo Evento
        </a>
    <?php endif; ?>
</div>

<!-- Alertas de sucesso e erro -->
<?php if ($mensagemSucesso): ?>
    <div class="alerta alerta-sucesso">✅ <?= htmlspecialchars($mensagemSucesso) ?></div>
<?php endif; ?>

<?php if ($mensagemErro): ?>
    <div class="alerta alerta-erro">⚠️ <?= htmlspecialchars($mensagemErro) ?></div>
<?php endif; ?>

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
        <?php if ($usuarioAtual): ?>
            <a href="criar_evento.php" class="btn btn-primario mt-md">Criar o primeiro evento</a>
        <?php endif; ?>
    </div>
<?php endif; ?>

<!-- ── GRID DE CARDS DE EVENTOS ──────────────────────────────────────────────── -->
<div class="grid-eventos">
    <?php foreach ($eventos as $evento):
        $idEvento    = $evento['id']           ?? null;
        $titulo      = $evento['titulo']      ?? 'Sem título';
        $descricao   = $evento['descricao']   ?? '';
        $dataInicio  = isset($evento['inicio']) ? date('d/m/Y H:i', strtotime($evento['inicio'])) : '—';
        $dataFim     = isset($evento['fim'])    ? date('d/m/Y H:i', strtotime($evento['fim']))    : '—';
        $capacidade  = $evento['capacidade'] ?? '—';

        // Lista de organizadores associados a este evento
        $organizadores = $evento['organizadoresIds'] ?? [];

        // ── REGRA DE INTERFACE CONDICIONAL ───────────────────────────────────
        // Verifica se o usuário atual é o organizador/criador deste evento específico
        $ehOrganizadorDesteEvento = ($idUsuarioLogado !== null && in_array($idUsuarioLogado, $organizadores));

        // Usuário pode gerenciar (editar/excluir) se for Administrador OU Organizador do evento
        $podeGerenciar = $ehAdmin || $ehOrganizadorDesteEvento;
    ?>
        <div class="card-evento" id="evento-<?= (int)$idEvento ?>">
            <div style="display: flex; justify-content: space-between; align-items: flex-start; gap: 0.5rem;">
                <h3><?= htmlspecialchars($titulo) ?></h3>
                <?php if ($ehOrganizadorDesteEvento): ?>
                    <span class="badge badge-usuario" title="Você é o organizador deste evento" style="font-size: 0.72rem;">
                        👑 Seu Evento
                    </span>
                <?php endif; ?>
            </div>

            <div class="evento-meta">
                <span>📆 <?= htmlspecialchars($dataInicio) ?> → <?= htmlspecialchars($dataFim) ?></span>
                <span>👥 Capacidade: <?= htmlspecialchars((string)$capacidade) ?></span>
            </div>

            <?php if ($descricao): ?>
                <p class="evento-descricao">
                    <?= htmlspecialchars(mb_strimwidth($descricao, 0, 120, '...')) ?>
                </p>
            <?php endif; ?>

            <div class="card-evento-acoes" style="display: flex; gap: 0.5rem; flex-wrap: wrap; align-items: center; margin-top: 1rem;">
                <!-- Botão para ver atividades (sempre disponível) -->
                <a href="atividades.php?evento_id=<?= (int)$idEvento ?>"
                   class="btn btn-primario"
                   id="btn-ver-atividades-<?= (int)$idEvento ?>"
                   style="font-size: 0.85rem; padding: 0.4rem 0.8rem;">
                    Ver Atividades
                </a>

                <?php if ($podeGerenciar): ?>
                    <!--
                        BOTÕES CONDICIONAIS DE GERENCIAMENTO:
                        Exibidos APENAS se o usuário for ADMINISTRADOR ou ORGANIZADOR DESTE EVENTO.
                        Caso contrário, ficam ocultos visualmente e protegidos no backend.
                    -->
                    <a href="editar_evento.php?id=<?= (int)$idEvento ?>"
                       class="btn btn-secundario"
                       id="btn-editar-evento-<?= (int)$idEvento ?>"
                       style="font-size: 0.85rem; padding: 0.4rem 0.8rem;">
                        ✏️ Editar
                    </a>

                    <form method="POST" action="eventos.php"
                          id="form-excluir-evento-<?= (int)$idEvento ?>"
                          style="display: inline;"
                          onsubmit="return confirm('Tem certeza que deseja excluir o evento <?= addslashes($titulo) ?>?');">
                        <input type="hidden" name="acao" value="excluir">
                        <input type="hidden" name="id_evento" value="<?= (int)$idEvento ?>">
                        <button type="submit"
                                class="btn btn-perigo"
                                id="btn-excluir-evento-<?= (int)$idEvento ?>"
                                style="font-size: 0.85rem; padding: 0.4rem 0.8rem;">
                            🗑️ Excluir
                        </button>
                    </form>
                <?php endif; ?>
            </div>
        </div>
    <?php endforeach; ?>
</div>

<?php
$conteudo    = ob_get_clean();
$titulo      = 'Eventos';
$paginaAtiva = 'eventos';
include __DIR__ . '/layout.php';
