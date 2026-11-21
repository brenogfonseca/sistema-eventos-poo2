<?php
require_once __DIR__ . '/api_service.php';
exigir_login();
$idAtividade = (int)($_GET['id'] ?? $_POST['id_atividade'] ?? 0);
if ($idAtividade <= 0) { header('Location: eventos.php'); exit; }
$headers = headers_autenticados();
$locaisResp = chamar_api('GET', '/locais', null, $headers);
$locais = $locaisResp['erro'] ? [] : ($locaisResp['corpo'] ?? []);
$atividadeResp = chamar_api('GET', "/atividades/{$idAtividade}", null, $headers);
$atividade = $atividadeResp['erro'] ? null : ($atividadeResp['corpo'] ?? null);
$erro = '';
$idEvento = (int)($atividade['eventoId'] ?? 0);
$eventoResp = $idEvento > 0 ? chamar_api('GET', "/eventos/{$idEvento}", null, $headers) : ['erro' => true];
$evento = $eventoResp['erro'] ? null : ($eventoResp['corpo'] ?? null);
$permitido = $atividade && $evento && (eh_administrador() || in_array(id_usuario_logado(), $evento['organizadoresIds'] ?? []));
if (!$permitido) $erro = $atividadeResp['msg_erro'] ?? 'Somente o organizador pode editar esta atividade.';

if ($_SERVER['REQUEST_METHOD'] === 'POST' && $permitido) {
    $vagas = max(0, (int)($_POST['vagas'] ?? 0));
    $idLocal = (int)($_POST['local_id'] ?? 0);
    $localSelecionado = null;
    foreach ($locais as $localDisponivel) {
        if ((int)($localDisponivel['id'] ?? 0) === $idLocal) { $localSelecionado = $localDisponivel; break; }
    }
    $nomeTrilha = trim($_POST['trilha_nome'] ?? '');
    $dados = [
        'nome' => trim($_POST['nome'] ?? ''),
        'descricao' => trim($_POST['descricao'] ?? ''),
        'eventoId' => $idEvento,
        'local' => $localSelecionado ? ['id' => $idLocal, 'nome' => $localSelecionado['nome']] : null,
        'horaInicio' => ($_POST['hora_inicio'] ?? '') . ':00',
        'horaFim' => ($_POST['hora_fim'] ?? '') . ':00',
        'data' => $_POST['data'] ?? '',
        'tipo' => trim($_POST['tipo'] ?? ''),
        'vagas' => $vagas,
        'controlaVagas' => !empty($_POST['controla_vagas']),
        'tipoFrequencia' => $_POST['tipoFrequencia'] ?? 'CHECKIN',
        'trilha' => $nomeTrilha !== '' ? ['nome' => $nomeTrilha] : null,
    ];
    $resultado = chamar_api('PUT', "/atividades/{$idAtividade}", $dados, $headers);
    if ($resultado['erro']) $erro = $resultado['msg_erro'];
    else { header("Location: atividades.php?evento_id={$idEvento}&atualizada=1"); exit; }
}

if ($atividade && $_SERVER['REQUEST_METHOD'] !== 'POST') {
    $atividade = $atividade;
}
$data = $_POST['data'] ?? ($atividade['data'] ?? '');
$inicio = $_POST['hora_inicio'] ?? substr($atividade['horaInicio'] ?? '09:00', 0, 5);
$fim = $_POST['hora_fim'] ?? substr($atividade['horaFim'] ?? '10:00', 0, 5);
$localId = (int)($_POST['local_id'] ?? ($atividade['local']['id'] ?? 0));
$trilha = $_POST['trilha_nome'] ?? ($atividade['trilha']['nome'] ?? '');
ob_start();
?>
<div class="secao-cabecalho"><div><h1 class="secao-titulo">Editar atividade</h1><p class="secao-subtitulo"><?= htmlspecialchars($atividade['nome'] ?? '') ?></p></div><a class="btn btn-secundario" href="atividades.php?evento_id=<?= $idEvento ?>">Voltar</a></div>
<?php if ($erro): ?><div class="alerta alerta-erro"><?= htmlspecialchars($erro) ?></div><?php endif; ?>
<?php if ($permitido): ?><div class="card"><form method="POST"><input type="hidden" name="id_atividade" value="<?= $idAtividade ?>">
    <div class="form-grupo"><label>Nome</label><input name="nome" value="<?= htmlspecialchars($_POST['nome'] ?? $atividade['nome'] ?? '') ?>" required></div>
    <div class="form-grupo"><label>Descrição</label><textarea name="descricao" rows="3" maxlength="4000"><?= htmlspecialchars($_POST['descricao'] ?? $atividade['descricao'] ?? '') ?></textarea></div>
    <div class="form-linha-3"><div class="form-grupo"><label>Data</label><input type="date" name="data" value="<?= htmlspecialchars($data) ?>" required></div><div class="form-grupo"><label>Início</label><input type="time" name="hora_inicio" value="<?= htmlspecialchars($inicio) ?>" required></div><div class="form-grupo"><label>Fim</label><input type="time" name="hora_fim" value="<?= htmlspecialchars($fim) ?>" required></div></div>
    <div class="form-linha"><div class="form-grupo"><label>Categoria/tipo</label><input name="tipo" value="<?= htmlspecialchars($_POST['tipo'] ?? $atividade['tipo'] ?? '') ?>"></div><div class="form-grupo"><label>Vagas restantes</label><input type="number" min="0" name="vagas" value="<?= (int)($_POST['vagas'] ?? $atividade['vagas'] ?? 0) ?>"></div></div>
    <label><input type="checkbox" name="controla_vagas" value="1" <?= !empty($_POST['controla_vagas']) || (($_SERVER['REQUEST_METHOD'] !== 'POST') && !empty($atividade['controlaVagas'])) ? 'checked' : '' ?>> Limitar vagas</label>
    <div class="form-linha"><div class="form-grupo"><label>Trilha</label><input name="trilha_nome" value="<?= htmlspecialchars($trilha) ?>"></div><div class="form-grupo"><label>Local cadastrado</label><select name="local_id"><option value="0">A definir</option><?php foreach ($locais as $localDisponivel): ?><option value="<?= (int)$localDisponivel['id'] ?>" <?= (int)$localDisponivel['id'] === $localId ? 'selected' : '' ?>><?= htmlspecialchars($localDisponivel['nome'] ?? '') ?></option><?php endforeach; ?></select></div></div>
    <div class="form-grupo"><label>Critério de frequência</label><select name="tipoFrequencia"><?php foreach (['CHECKIN','ENTRADA_SAIDA','MANUAL'] as $opcao): ?><option value="<?= $opcao ?>" <?= ($_POST['tipoFrequencia'] ?? $atividade['tipoFrequencia'] ?? 'CHECKIN') === $opcao ? 'selected' : '' ?>><?= htmlspecialchars($opcao) ?></option><?php endforeach; ?></select></div>
    <button class="btn btn-primario">Salvar alterações</button>
</form></div><?php endif; ?>
<?php
$conteudo = ob_get_clean();
$titulo = 'Editar atividade';
$paginaAtiva = 'eventos';
include __DIR__ . '/layout.php';
