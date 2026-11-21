<?php
require_once __DIR__ . '/api_service.php';
exigir_login();
if (!eh_administrador()) { http_response_code(403); exit('Apenas administradores podem gerenciar locais.'); }
$headers = headers_autenticados();
$erro = '';
$sucesso = '';
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $acao = $_POST['acao'] ?? '';
    if ($acao === 'criar') {
        $r = chamar_api('POST', '/locais', ['nome' => trim($_POST['nome'] ?? '')], $headers);
    } elseif ($acao === 'alterar') {
        $id = (int)($_POST['id'] ?? 0);
        $r = chamar_api('PUT', "/locais/{$id}", ['nome' => trim($_POST['nome'] ?? '')], $headers);
    } elseif ($acao === 'excluir') {
        $id = (int)($_POST['id'] ?? 0);
        $r = chamar_api('DELETE', "/locais/{$id}", null, $headers);
    } else {
        $r = ['erro' => true, 'msg_erro' => 'Ação inválida.'];
    }
    if ($r['erro']) $erro = $r['msg_erro']; else $sucesso = 'Alterações salvas.';
}
$r = chamar_api('GET', '/locais', null, $headers);
$locais = $r['erro'] ? [] : ($r['corpo'] ?? []);
if ($r['erro'] && !$erro) $erro = $r['msg_erro'];
ob_start();
?>
<div class="secao-cabecalho"><div><h1 class="secao-titulo">Gerenciar locais</h1><p class="secao-subtitulo">Cadastre espaços que podem ser associados às atividades.</p></div></div>
<?php if ($erro): ?><div class="alerta alerta-erro"><?= htmlspecialchars($erro) ?></div><?php endif; ?>
<?php if ($sucesso): ?><div class="alerta alerta-sucesso"><?= htmlspecialchars($sucesso) ?></div><?php endif; ?>
<div class="card mb-md"><h2>Novo local</h2><form method="POST"><input type="hidden" name="acao" value="criar"><div class="form-linha"><input name="nome" required maxlength="160" placeholder="Nome do local"><button class="btn btn-primario">Cadastrar</button></div></form></div>
<div class="card"><h2>Locais cadastrados</h2><?php if (!$locais): ?><p>Nenhum local cadastrado.</p><?php else: ?><div class="tabela-responsiva"><table class="tabela"><thead><tr><th>Nome</th><th>Ações</th></tr></thead><tbody>
<?php foreach ($locais as $local): ?><tr><td><form method="POST" class="inline-form"><input type="hidden" name="acao" value="alterar"><input type="hidden" name="id" value="<?= (int)$local['id'] ?>"><input name="nome" value="<?= htmlspecialchars($local['nome'] ?? '') ?>" required maxlength="160"><button class="btn btn-secundario">Salvar</button></form></td><td><form method="POST" onsubmit="return confirm('Excluir este local?');"><input type="hidden" name="acao" value="excluir"><input type="hidden" name="id" value="<?= (int)$local['id'] ?>"><button class="btn btn-perigo">Excluir</button></form></td></tr><?php endforeach; ?>
</tbody></table></div><?php endif; ?></div>
<?php
$conteudo = ob_get_clean();
$titulo = 'Locais';
$paginaAtiva = 'locais';
include __DIR__ . '/layout.php';
