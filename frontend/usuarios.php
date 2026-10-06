<?php
/**
 * usuarios.php — Módulo de Usuários (Admin): Gerenciamento de usuários
 * ─────────────────────────────────────────────────────────────────────────────
 * FLUXO DIDÁTICO:
 *   1. Apenas ADMINISTRADOR pode acessar esta página.
 *   2. Busca a lista de todos os usuários: GET /usuarios
 *      — requer header X-Usuario-Id (via headers_autenticados())
 *   3. Exibe em tabela com opções de:
 *      a. Alterar perfil: PATCH /usuarios/{id}/perfil  (novoPerfil)
 *      b. Excluir:        DELETE /usuarios/{id}
 *
 * Endpoints utilizados:
 *   GET    /usuarios             (header: X-Usuario-Id)
 *   PATCH  /usuarios/{id}/perfil (body: { "novoPerfil": "..." })
 *   DELETE /usuarios/{id}        (header: X-Usuario-Id)
 * ─────────────────────────────────────────────────────────────────────────────
 */
require_once __DIR__ . '/api_service.php';
exigir_login();

// Apenas administradores
if (!eh_administrador()) {
    header('Location: eventos.php');
    exit;
}

$erro    = '';
$sucesso = '';

// ── Processa ações do formulário ──────────────────────────────────────────────
if ($_SERVER['REQUEST_METHOD'] === 'POST') {

    $acao      = trim($_POST['acao']       ?? '');
    $idAlvo    = (int)($_POST['id_usuario'] ?? 0);
    $novoPerfil= trim($_POST['novo_perfil'] ?? '');

    switch ($acao) {

        // ── Alterar perfil: PATCH /usuarios/{id}/perfil ───────────────────
        case 'alterar_perfil':
            if ($idAlvo > 0 && !empty($novoPerfil)) {
                // Corpo: { "novoPerfil": "ADMINISTRADOR" }
                // Conforme AlterarPerfilRequest em UsuarioController.java
                $dados     = ['novoPerfil' => $novoPerfil];
                $resultado = chamar_api(
                    'PATCH',
                    "/usuarios/{$idAlvo}/perfil",
                    $dados,
                    headers_autenticados() // Passa X-Usuario-Id no header
                );
                $sucesso = $resultado['erro']
                    ? ''
                    : "Perfil do usuário #{$idAlvo} alterado para {$novoPerfil}.";
                $erro    = $resultado['erro'] ? $resultado['msg_erro'] : '';
            }
            break;

        // ── Excluir usuário: DELETE /usuarios/{id} ────────────────────────
        case 'excluir':
            if ($idAlvo > 0) {
                // DELETE não precisa de corpo; apenas o ID na URL e X-Usuario-Id
                $resultado = chamar_api(
                    'DELETE',
                    "/usuarios/{$idAlvo}",
                    null,
                    headers_autenticados()
                );
                $sucesso = $resultado['erro']
                    ? ''
                    : "Usuário #{$idAlvo} excluído com sucesso.";
                $erro    = $resultado['erro'] ? $resultado['msg_erro'] : '';
            }
            break;
    }
}

// ── Busca lista de usuários na API ────────────────────────────────────────────
// GET /usuarios — requer que o executor seja ADMINISTRADOR (via X-Usuario-Id)
$resultado    = chamar_api('GET', '/usuarios', null, headers_autenticados());
$usuarios     = [];
$erroListagem = '';

if ($resultado['erro']) {
    $erroListagem = $resultado['msg_erro'];
} else {
    $usuarios = $resultado['corpo'] ?? [];
}

// ── Monta o HTML ──────────────────────────────────────────────────────────────
ob_start();
?>

<div class="secao-cabecalho">
    <div>
        <h1 class="secao-titulo">👥 Gerenciar Usuários</h1>
        <p class="secao-subtitulo">Administre os perfis e contas do sistema</p>
    </div>
    <span class="text-muted">Total: <?= count($usuarios) ?> usuário(s)</span>
</div>

<!-- Alertas -->
<?php if ($sucesso): ?>
    <div class="alerta alerta-sucesso">✅ <?= htmlspecialchars($sucesso) ?></div>
<?php endif; ?>
<?php if ($erro): ?>
    <div class="alerta alerta-erro">⚠️ <?= htmlspecialchars($erro) ?></div>
<?php endif; ?>
<?php if ($erroListagem): ?>
    <div class="alerta alerta-erro">⚠️ <?= htmlspecialchars($erroListagem) ?></div>
<?php endif; ?>

<!-- Tabela de usuários -->
<?php if (!empty($usuarios)): ?>
<div class="card">
    <div class="tabela-responsiva">
        <table class="tabela" id="tabela-usuarios">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Nome</th>
                    <th>E-mail</th>
                    <th>Perfil Atual</th>
                    <th>Alterar Perfil</th>
                    <th>Excluir</th>
                </tr>
            </thead>
            <tbody>
                <?php foreach ($usuarios as $u):
                    $uid    = $u['id']     ?? '—';
                    $nome   = $u['nome']   ?? '—';
                    $email  = $u['email']  ?? '—';
                    $perfil = $u['perfil'] ?? '—';
                    $ehEu   = ($uid == id_usuario_logado()); // Não pode se autoexcluir
                ?>
                    <tr id="usuario-<?= (int)$uid ?>">
                        <td>#<?= htmlspecialchars((string)$uid) ?></td>
                        <td><strong><?= htmlspecialchars($nome) ?></strong></td>
                        <td><?= htmlspecialchars($email) ?></td>
                        <td>
                            <?php
                            $badgeClass = match($perfil) {
                                'ADMINISTRADOR' => 'badge-admin',
                                'USUARIO'       => 'badge-usuario',
                                default         => 'badge-visitante',
                            };
                            ?>
                            <span class="badge <?= $badgeClass ?>">
                                <?= htmlspecialchars($perfil) ?>
                            </span>
                        </td>
                        <td>
                            <!--
                                Formulário: PATCH /usuarios/{id}/perfil
                                Corpo JSON: { "novoPerfil": "ADMINISTRADOR" }
                            -->
                            <form method="POST" action="usuarios.php"
                                  id="form-perfil-<?= (int)$uid ?>"
                                  style="display:flex; gap:0.4rem; align-items:center;">
                                <input type="hidden" name="acao" value="alterar_perfil">
                                <input type="hidden" name="id_usuario" value="<?= (int)$uid ?>">
                                <select name="novo_perfil"
                                        id="select-perfil-<?= (int)$uid ?>"
                                        style="font-size:0.82rem; padding:0.25rem 0.5rem; border:1px solid #ccc; border-radius:4px;">
                                    <option value="VISITANTE"     <?= $perfil === 'VISITANTE'     ? 'selected' : '' ?>>Visitante</option>
                                    <option value="USUARIO"       <?= $perfil === 'USUARIO'       ? 'selected' : '' ?>>Usuário</option>
                                    <option value="ADMINISTRADOR" <?= $perfil === 'ADMINISTRADOR' ? 'selected' : '' ?>>Admin</option>
                                </select>
                                <button type="submit"
                                        class="btn btn-primario"
                                        id="btn-salvar-perfil-<?= (int)$uid ?>"
                                        style="font-size:0.78rem; padding:0.25rem 0.6rem;">
                                    Salvar
                                </button>
                            </form>
                        </td>
                        <td>
                            <?php if (!$ehEu): ?>
                                <!--
                                    Formulário: DELETE /usuarios/{id}
                                    Usa X-Usuario-Id no header para identificar o admin.
                                -->
                                <form method="POST" action="usuarios.php"
                                      id="form-excluir-<?= (int)$uid ?>"
                                      onsubmit="return confirm('Excluir <?= addslashes($nome) ?>?');">
                                    <input type="hidden" name="acao" value="excluir">
                                    <input type="hidden" name="id_usuario" value="<?= (int)$uid ?>">
                                    <button type="submit"
                                            class="btn btn-perigo"
                                            id="btn-excluir-<?= (int)$uid ?>"
                                            style="font-size:0.78rem; padding:0.25rem 0.6rem;">
                                        Excluir
                                    </button>
                                </form>
                            <?php else: ?>
                                <span class="text-muted" style="font-size:0.82rem;">(você)</span>
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
$titulo      = 'Usuários';
$paginaAtiva = 'usuarios';
include __DIR__ . '/layout.php';
