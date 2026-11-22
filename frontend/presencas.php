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
$erro = '';
$feedback = $_SESSION['presencas_feedback'] ?? null;
unset($_SESSION['presencas_feedback']);

if ($_SERVER['REQUEST_METHOD'] === 'POST' && $podeGerenciar) {
    $idInscricaoEvento = (int)($_POST['id_inscricao_evento'] ?? 0);
    $presente = ($_POST['presente'] ?? '') === '1';
    $resultado = chamar_api('POST', '/frequencias/manual', [
        'idInscricaoEvento' => $idInscricaoEvento,
        'idAtividade' => $idAtividade,
        'presente' => $presente,
    ], $headers);
    $_SESSION['presencas_feedback'] = $resultado['erro']
        ? ['sucesso' => false, 'mensagem' => $resultado['msg_erro']]
        : ['sucesso' => true, 'mensagem' => $presente ? 'Participante marcado como presente.' : 'Participante marcado como ausente.'];
    header("Location: presencas.php?atividade_id={$idAtividade}");
    exit;
}

$listaResp = $podeGerenciar
    ? chamar_api('GET', "/frequencias/atividade/{$idAtividade}/participantes", null, $headers)
    : ['erro' => true, 'msg_erro' => $atividadeResp['msg_erro'] ?? 'Somente o organizador pode corrigir presenças.'];
$participantes = $listaResp['erro'] ? [] : ($listaResp['corpo'] ?? []);
if (!$podeGerenciar) $erro = 'Somente o organizador do evento pode corrigir presenças.';
elseif ($listaResp['erro']) $erro = $listaResp['msg_erro'] ?? 'Não foi possível carregar as presenças.';

ob_start();
?>
<div class="secao-cabecalho"><div><h1 class="secao-titulo">Presenças da atividade</h1>
<p class="secao-subtitulo"><?= htmlspecialchars($atividade['nome'] ?? '') ?> · <?= htmlspecialchars($evento['titulo'] ?? '') ?></p></div>
<a class="btn btn-secundario" href="atividades.php?evento_id=<?= $idEvento ?>">Voltar</a></div>
<?php if ($feedback): ?><div class="alerta <?= $feedback['sucesso'] ? 'alerta-sucesso' : 'alerta-erro' ?>"><?= htmlspecialchars($feedback['mensagem']) ?></div><?php endif; ?>
<?php if ($erro): ?><div class="alerta alerta-erro"><?= htmlspecialchars($erro) ?></div><?php endif; ?>
<?php if ($podeGerenciar && !$erro): ?>
<div class="card"><p>Somente organizadores podem corrigir presenças. Cada correção fica registrada com responsável e horário; o histórico anterior não é apagado.</p>
<?php if (!$participantes): ?><p class="text-muted">Ainda não há participantes ativos inscritos nesta atividade.</p><?php else: ?>
<div class="tabela-responsiva"><table class="tabela"><thead><tr><th>Participante</th><th>CPF</th><th>Status atual</th><th>Correção</th></tr></thead><tbody>
<?php foreach ($participantes as $participante): $presenteAtual = !empty($participante['presente']); ?>
<tr><td><?= htmlspecialchars($participante['nome'] ?? '') ?></td><td><?= htmlspecialchars($participante['cpfMascarado'] ?? 'CPF não cadastrado') ?></td>
<td><?= $presenteAtual ? 'Presente' : 'Ausente / sem validação' ?></td><td>
<form method="POST"><input type="hidden" name="id_atividade" value="<?= $idAtividade ?>"><input type="hidden" name="id_inscricao_evento" value="<?= (int)$participante['idInscricaoEvento'] ?>"><input type="hidden" name="presente" value="<?= $presenteAtual ? '0' : '1' ?>">
<button class="btn <?= $presenteAtual ? 'btn-perigo' : 'btn-sucesso' ?>" type="submit"><?= $presenteAtual ? 'Marcar falta' : 'Marcar presente' ?></button></form>
</td></tr><?php endforeach; ?>
</tbody></table></div><?php endif; ?></div><?php endif; ?>
<?php
$conteudo = ob_get_clean();
$titulo = 'Gerenciar presenças';
$paginaAtiva = 'eventos';
include __DIR__ . '/layout.php';
