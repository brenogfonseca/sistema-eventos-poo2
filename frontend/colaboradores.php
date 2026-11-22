<?php
require_once __DIR__ . '/api_service.php';
exigir_login();
iniciar_sessao();

$idAtividade = (int)($_GET['atividade_id'] ?? $_POST['id_atividade'] ?? 0);
if ($idAtividade <= 0) { header('Location: eventos.php'); exit; }
$headers = headers_autenticados();
$atividadeResp = chamar_api('GET', "/atividades/{$idAtividade}", null, $headers);
$atividade = $atividadeResp['erro'] ? null : ($atividadeResp['corpo'] ?? null);
$idEvento = (int)($atividade['eventoId'] ?? 0);
$eventoResp = $idEvento > 0 ? chamar_api('GET', "/eventos/{$idEvento}", null, $headers) : ['erro' => true];
$evento = $eventoResp['erro'] ? null : ($eventoResp['corpo'] ?? null);
$podeGerenciar = $atividade && $evento && (eh_administrador()
    || in_array(id_usuario_logado(), $evento['organizadoresIds'] ?? []));
$feedback = $_SESSION['colaboradores_feedback'] ?? null;
unset($_SESSION['colaboradores_feedback']);
$erro = '';

if ($_SERVER['REQUEST_METHOD'] === 'POST' && $podeGerenciar) {
    $idColaborador = (int)($_POST['id_usuario'] ?? 0);
    if (($_POST['acao'] ?? '') === 'vincular') {
        $resultado = chamar_api('POST', "/atividades/{$idAtividade}/colaboradores",
            ['email' => trim($_POST['email'] ?? '')], $headers);
    } else {
        $resultado = chamar_api('DELETE', "/atividades/{$idAtividade}/colaboradores/{$idColaborador}", null, $headers);
    }
    $_SESSION['colaboradores_feedback'] = $resultado['erro']
        ? ['sucesso' => false, 'mensagem' => $resultado['msg_erro']]
        : ['sucesso' => true, 'mensagem' => ($_POST['acao'] ?? '') === 'vincular'
            ? 'Colaborador vinculado somente a esta atividade.' : 'Acesso do colaborador removido.'];
    header("Location: colaboradores.php?atividade_id={$idAtividade}");
    exit;
}

$listaResp = $podeGerenciar
    ? chamar_api('GET', "/atividades/{$idAtividade}/colaboradores", null, $headers)
    : ['erro' => true, 'msg_erro' => $atividadeResp['msg_erro'] ?? 'Somente o organizador pode gerenciar colaboradores.'];
$colaboradores = $listaResp['erro'] ? [] : ($listaResp['corpo'] ?? []);
if (!$podeGerenciar) $erro = 'Somente o organizador do evento pode gerenciar colaboradores.';
elseif ($listaResp['erro']) $erro = $listaResp['msg_erro'] ?? 'Não foi possível carregar colaboradores.';

ob_start();
?>
<div class="secao-cabecalho"><div><h1 class="secao-titulo">Colaboradores da atividade</h1>
<p class="secao-subtitulo"><?= htmlspecialchars($atividade['nome'] ?? '') ?> · <?= htmlspecialchars($evento['titulo'] ?? '') ?></p></div>
<a class="btn btn-secundario" href="atividades.php?evento_id=<?= $idEvento ?>">Voltar</a></div>
<?php if ($feedback): ?><div class="alerta <?= $feedback['sucesso'] ? 'alerta-sucesso' : 'alerta-erro' ?>"><?= htmlspecialchars($feedback['mensagem']) ?></div><?php endif; ?>
<?php if ($erro): ?><div class="alerta alerta-erro"><?= htmlspecialchars($erro) ?></div><?php endif; ?>
<?php if ($podeGerenciar && !$erro): ?>
<div class="card"><h2>Adicionar pessoa da equipe</h2><p>Informe o e-mail de uma conta já cadastrada. Ela poderá validar presença nesta atividade, sem permissão para marcar falta ou corrigir status.</p>
<form method="POST"><input type="hidden" name="acao" value="vincular"><input type="hidden" name="id_atividade" value="<?= $idAtividade ?>">
<div class="form-linha"><div class="form-grupo"><label for="email">E-mail do colaborador</label><input type="email" id="email" name="email" required></div><button class="btn btn-primario" type="submit">Vincular</button></div></form>
<h2>Equipe vinculada</h2>
<?php if (!$colaboradores): ?><p class="text-muted">Ainda não há colaboradores vinculados a esta atividade.</p><?php else: ?>
<div class="tabela-responsiva"><table class="tabela"><thead><tr><th>Nome</th><th>E-mail</th><th>Acesso</th></tr></thead><tbody>
<?php foreach ($colaboradores as $colaborador): ?><tr><td><?= htmlspecialchars($colaborador['nome'] ?? '') ?></td><td><?= htmlspecialchars($colaborador['email'] ?? '') ?></td><td>
<form method="POST"><input type="hidden" name="acao" value="remover"><input type="hidden" name="id_atividade" value="<?= $idAtividade ?>"><input type="hidden" name="id_usuario" value="<?= (int)$colaborador['idUsuario'] ?>"><button class="btn btn-perigo" type="submit">Remover</button></form>
</td></tr><?php endforeach; ?></tbody></table></div><?php endif; ?></div><?php endif; ?>
<?php
$conteudo = ob_get_clean();
$titulo = 'Gerenciar colaboradores';
$paginaAtiva = 'eventos';
include __DIR__ . '/layout.php';
