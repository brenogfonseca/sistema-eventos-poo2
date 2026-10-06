<?php
/**
 * minhas_inscricoes.php — Módulo de Inscrições: Agenda do participante
 * ─────────────────────────────────────────────────────────────────────────────
 * FLUXO DIDÁTICO:
 *   1. Verifica se o usuário está logado (exigir_login()).
 *   2. Busca todas as inscrições do usuário:
 *      GET /inscricoes/usuario/{idUsuario}
 *   3. Exibe em uma tabela com status (ATIVO / CANCELADO) e ações.
 *   4. Botão "Cancelar" envia PATCH /inscricoes/{id}/cancelar.
 *   5. Botão "Reativar" envia PATCH /inscricoes/{id}/reativar.
 *
 * Endpoints utilizados:
 *   GET   /inscricoes/usuario/{idUsuario}
 *   PATCH /inscricoes/{id}/cancelar
 *   PATCH /inscricoes/{id}/reativar
 * ─────────────────────────────────────────────────────────────────────────────
 */
require_once __DIR__ . '/api_service.php';
exigir_login(); // Redireciona para login se não logado

$idUsuario = id_usuario_logado();
$erro      = '';
$sucesso   = '';

// ── Processa ação de cancelar ou reativar inscrição ───────────────────────────
if ($_SERVER['REQUEST_METHOD'] === 'POST') {

    $idInscricao = (int)($_POST['id_inscricao'] ?? 0);
    $acao        = trim($_POST['acao'] ?? '');

    if ($idInscricao > 0 && in_array($acao, ['cancelar', 'reativar'])) {

        // PATCH /inscricoes/{id}/cancelar  ou  PATCH /inscricoes/{id}/reativar
        // Esses endpoints não têm corpo; apenas o ID na URL e o método PATCH.
        $resultado = chamar_api(
            'PATCH',
            "/inscricoes/{$idInscricao}/{$acao}"
        );

        if ($resultado['erro']) {
            $erro = $resultado['msg_erro'] ?: "Erro ao {$acao} a inscrição.";
        } else {
            $sucesso = $acao === 'cancelar'
                ? 'Inscrição cancelada com sucesso.'
                : 'Inscrição reativada com sucesso! ✅';
        }
    }
}

// ── Busca as inscrições do usuário logado na API ──────────────────────────────
// GET /inscricoes/usuario/{idUsuario}
$resultado    = chamar_api('GET', "/inscricoes/usuario/{$idUsuario}");
$inscricoes   = [];
$erroListagem = '';

if ($resultado['erro']) {
    $erroListagem = $resultado['msg_erro'];
} else {
    $inscricoes = $resultado['corpo'] ?? [];
}

// ── Monta o HTML ──────────────────────────────────────────────────────────────
ob_start();
?>

<div class="secao-cabecalho">
    <div>
        <h1 class="secao-titulo">📋 Minhas Inscrições</h1>
        <p class="secao-subtitulo">Sua agenda de atividades</p>
    </div>
    <a href="eventos.php" class="btn btn-secundario">← Ver Eventos</a>
</div>

<!-- Alertas de retorno -->
<?php if ($sucesso): ?>
    <div class="alerta alerta-sucesso">✅ <?= htmlspecialchars($sucesso) ?></div>
<?php endif; ?>
<?php if ($erro): ?>
    <div class="alerta alerta-erro">⚠️ <?= htmlspecialchars($erro) ?></div>
<?php endif; ?>
<?php if ($erroListagem): ?>
    <div class="alerta alerta-erro">⚠️ <?= htmlspecialchars($erroListagem) ?></div>
<?php endif; ?>

<!-- Lista de inscrições -->
<?php if (empty($inscricoes) && !$erroListagem): ?>
    <div class="card text-center" style="padding: 3rem;">
        <p style="font-size: 2rem;">📭</p>
        <p class="text-muted">Você ainda não tem inscrições.</p>
        <a href="eventos.php" class="btn btn-primario mt-md">Explorar eventos</a>
    </div>

<?php else: ?>
    <div class="card">
        <div class="tabela-responsiva">
            <table class="tabela" id="tabela-inscricoes">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Atividade (ID)</th>
                        <th>Data Inscrição</th>
                        <th>Status</th>
                        <th>Ações</th>
                    </tr>
                </thead>
                <tbody>
                    <?php foreach ($inscricoes as $inscricao):
                        $idInsc      = $inscricao['id']          ?? '—';
                        $idAtiv      = $inscricao['idAtividade'] ?? '—';
                        $dataInsc    = $inscricao['dataInscricao'] ?? '—';
                        $status      = $inscricao['status']      ?? 'ATIVO';
                        $ativo       = $status === 'ATIVO';

                        // Formata a data de inscrição
                        if ($dataInsc !== '—') {
                            $dataFormatada = date('d/m/Y H:i', strtotime($dataInsc));
                        } else {
                            $dataFormatada = '—';
                        }
                    ?>
                        <tr id="inscricao-<?= (int)$idInsc ?>">
                            <td>#<?= htmlspecialchars((string)$idInsc) ?></td>
                            <td>
                                <a href="atividades.php?evento_id=<?= htmlspecialchars((string)($inscricao['idEvento'] ?? '')) ?>">
                                    Atividade #<?= htmlspecialchars((string)$idAtiv) ?>
                                </a>
                            </td>
                            <td><?= htmlspecialchars($dataFormatada) ?></td>
                            <td>
                                <?php if ($ativo): ?>
                                    <span class="badge badge-ativo">Ativo</span>
                                <?php else: ?>
                                    <span class="badge badge-cancelado">Cancelado</span>
                                <?php endif; ?>
                            </td>
                            <td>
                                <!--
                                    Formulários com PATCH /inscricoes/{id}/cancelar
                                    ou PATCH /inscricoes/{id}/reativar.
                                    O PHP acima detecta 'acao' e chama chamar_api().
                                -->
                                <?php if ($ativo): ?>
                                    <form method="POST" action="minhas_inscricoes.php"
                                          id="form-cancelar-<?= (int)$idInsc ?>"
                                          style="display:inline;"
                                          onsubmit="return confirm('Cancelar esta inscrição?');">
                                        <input type="hidden" name="id_inscricao" value="<?= (int)$idInsc ?>">
                                        <input type="hidden" name="acao" value="cancelar">
                                        <button type="submit"
                                                class="btn btn-perigo"
                                                id="btn-cancelar-<?= (int)$idInsc ?>"
                                                style="font-size:0.82rem; padding:0.3rem 0.8rem;">
                                            Cancelar
                                        </button>
                                    </form>
                                <?php else: ?>
                                    <form method="POST" action="minhas_inscricoes.php"
                                          id="form-reativar-<?= (int)$idInsc ?>"
                                          style="display:inline;">
                                        <input type="hidden" name="id_inscricao" value="<?= (int)$idInsc ?>">
                                        <input type="hidden" name="acao" value="reativar">
                                        <button type="submit"
                                                class="btn btn-sucesso"
                                                id="btn-reativar-<?= (int)$idInsc ?>"
                                                style="font-size:0.82rem; padding:0.3rem 0.8rem;">
                                            Reativar
                                        </button>
                                    </form>
                                <?php endif; ?>
                            </td>
                        </tr>
                    <?php endforeach; ?>
                </tbody>
            </table>
        </div>
    </div>
<?php endif; ?>

<?php
$conteudo    = ob_get_clean();
$titulo      = 'Minhas Inscrições';
$paginaAtiva = 'inscricoes';
include __DIR__ . '/layout.php';
