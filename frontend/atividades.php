<?php
require_once __DIR__ . '/api_service.php';
iniciar_sessao();

$idEvento = (int)($_GET['evento_id'] ?? 0);
if ($idEvento <= 0) { header('Location: eventos.php'); exit; }

$idUsuario = id_usuario_logado();
$erro = '';
$sucesso = '';
$resEvento = chamar_api('GET', "/eventos/{$idEvento}");
$evento = $resEvento['erro'] ? null : ($resEvento['corpo'] ?? null);
if (!$evento) { $erro = $resEvento['msg_erro'] ?? 'Evento não encontrado.'; }
$organizadores = $evento['organizadoresIds'] ?? [];
$podeGerenciar = eh_administrador() || ($idUsuario !== null && in_array($idUsuario, $organizadores));

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    exigir_login();
    $acao = $_POST['acao'] ?? '';
    if ($acao === 'inscrever_evento') {
        $resultado = chamar_api('POST', '/inscricoes/evento', ['idEvento' => $idEvento], headers_autenticados());
        if ($resultado['erro']) $erro = $resultado['msg_erro'] ?: 'Não foi possível realizar a inscrição no evento.';
        else $sucesso = 'Inscrição no evento realizada. Agora escolha as atividades que deseja acompanhar.';
    } elseif ($acao === 'salvar_atividades') {
        $idInscricaoEvento = (int)($_POST['id_inscricao_evento'] ?? 0);
        $idsAtividades = array_values(array_unique(array_map('intval', $_POST['ids_atividades'] ?? [])));
        $resultado = chamar_api('PUT', "/inscricoes/evento/{$idInscricaoEvento}/atividades",
            ['idsAtividades' => $idsAtividades], headers_autenticados());
        if ($resultado['erro']) $erro = $resultado['msg_erro'] ?: 'Não foi possível salvar sua agenda.';
        else $sucesso = 'Sua agenda de atividades foi atualizada.';
    } elseif ($acao === 'criar_atividade' && $podeGerenciar) {
        $vagas = max(0, (int)($_POST['vagas'] ?? 0));
        $dados = [
            'nome' => trim($_POST['nome'] ?? ''),
            'eventoId' => $idEvento,
            'local' => ['nome' => trim($_POST['local_nome'] ?? '')],
            'horaInicio' => ($_POST['hora_inicio'] ?? '') . ':00',
            'horaFim' => ($_POST['hora_fim'] ?? '') . ':00',
            'data' => $_POST['data'] ?? '',
            'tipo' => trim($_POST['tipo'] ?? 'Palestra'),
            'vagas' => $vagas,
            'controlaVagas' => $vagas > 0,
            'tipoFrequencia' => $_POST['tipoFrequencia'] ?? 'CHECKIN',
            'trilha' => null,
        ];
        $resultado = chamar_api('POST', '/atividades', $dados, headers_autenticados());
        if ($resultado['erro']) $erro = $resultado['msg_erro'] ?: 'Erro ao criar atividade.';
        else $sucesso = 'Atividade criada com a política de frequência selecionada.';
    }
}

$atividadesResp = chamar_api('GET', "/atividades/evento/{$idEvento}");
$atividades = $atividadesResp['erro'] ? [] : ($atividadesResp['corpo'] ?? []);
$inscricaoEvento = null;
$selecionadas = [];
if ($idUsuario !== null) {
    $inscricoesResp = chamar_api('GET', "/inscricoes/eventos/usuario/{$idUsuario}", null, headers_autenticados());
    foreach (($inscricoesResp['corpo'] ?? []) as $item) {
        if ((int)($item['idEvento'] ?? 0) === $idEvento) { $inscricaoEvento = $item; break; }
    }
    if ($inscricaoEvento && empty($inscricaoEvento['cancelada'])) {
        $escolhidasResp = chamar_api('GET', "/inscricoes/evento/{$inscricaoEvento['id']}/atividades", null, headers_autenticados());
        foreach (($escolhidasResp['corpo'] ?? []) as $item) {
            if (empty($item['cancelada'])) $selecionadas[(int)$item['idAtividade']] = true;
        }
    }
}
$membroAtivo = $inscricaoEvento && empty($inscricaoEvento['cancelada']);
$inscricoesAbertas = !empty($evento['inscricoesAbertas']);
$qrEvento = $membroAtivo
    ? chamar_api('GET', "/inscricoes/evento/{$inscricaoEvento['id']}/checkin-qr", null, headers_autenticados())
    : ['erro' => true, 'msg_erro' => ''];

ob_start();
?>
<div class="secao-cabecalho">
    <div>
        <a href="eventos.php" class="text-muted">← Voltar aos Eventos</a>
        <h1 class="secao-titulo mt-sm">Atividades de <?= htmlspecialchars($evento['titulo'] ?? "Evento #{$idEvento}") ?></h1>
        <p class="secao-subtitulo">Inscreva-se no evento e escolha as atividades da sua agenda.</p>
    </div>
    <?php if ($podeGerenciar): ?>
        <button type="button" onclick="document.getElementById('form-nova-atividade').classList.toggle('d-none')" class="btn btn-primario">+ Nova Atividade</button>
    <?php endif; ?>
</div>

<?php if ($sucesso): ?><div class="alerta alerta-sucesso">✅ <?= htmlspecialchars($sucesso) ?></div><?php endif; ?>
<?php if ($erro): ?><div class="alerta alerta-erro">⚠️ <?= htmlspecialchars($erro) ?></div><?php endif; ?>

<?php if (!$idUsuario): ?>
    <div class="card"><p>Entre na sua conta para se inscrever e montar sua agenda.</p><a class="btn btn-primario" href="login.php">Entrar</a></div>
<?php elseif (!$membroAtivo): ?>
    <div class="card mb-md">
        <p>Primeiro, faça sua inscrição no evento. Depois, as atividades disponíveis aparecerão para você escolher.</p>
        <?php if ($inscricoesAbertas): ?>
            <form method="POST"><input type="hidden" name="acao" value="inscrever_evento"><button class="btn btn-sucesso" type="submit">Inscrever-se no evento</button></form>
        <?php else: ?><p class="text-muted">As inscrições para este evento estão fechadas.</p><?php endif; ?>
    </div>
<?php else: ?>
    <div class="card mb-md">
        <div class="alerta alerta-sucesso">Você está inscrito no evento. Marque as atividades que deseja incluir na sua agenda.</div>
        <?php if (!$qrEvento['erro'] && !empty($qrEvento['corpo']['imagemBase64'])): ?>
            <h3>Seu QR Code único para este evento</h3>
            <img src="data:image/png;base64,<?= htmlspecialchars($qrEvento['corpo']['imagemBase64'], ENT_QUOTES, 'UTF-8') ?>" alt="QR Code da inscrição no evento" width="160" height="160">
            <p class="text-muted">Mostre o mesmo QR ao organizador em cada atividade escolhida. Ele vale até o fim do evento e também fica em <a href="minhas_inscricoes.php">Minhas inscrições</a>.</p>
        <?php elseif ($qrEvento['erro']): ?>
            <p class="text-muted">QR Code indisponível: <?= htmlspecialchars($qrEvento['msg_erro'] ?? '') ?></p>
        <?php endif; ?>
    </div>
<?php endif; ?>

<?php if ($podeGerenciar): ?>
<div id="form-nova-atividade" class="card mb-md d-none">
    <h2 class="card-titulo">Criar atividade</h2>
    <form method="POST" action="atividades.php?evento_id=<?= $idEvento ?>">
        <input type="hidden" name="acao" value="criar_atividade">
        <div class="form-linha">
            <div class="form-grupo"><label>Nome *</label><input name="nome" required></div>
            <div class="form-grupo"><label>Local</label><input name="local_nome"></div>
        </div>
        <div class="form-linha-3">
            <div class="form-grupo"><label>Data *</label><input type="date" name="data" required></div>
            <div class="form-grupo"><label>Início *</label><input type="time" name="hora_inicio" required></div>
            <div class="form-grupo"><label>Fim *</label><input type="time" name="hora_fim" required></div>
        </div>
        <div class="form-linha">
            <div class="form-grupo"><label>Tipo</label><input name="tipo" value="Palestra"></div>
            <div class="form-grupo"><label>Vagas (0 = ilimitado)</label><input type="number" name="vagas" min="0" value="0"></div>
            <div class="form-grupo"><label>Critério de presença *</label>
                <select name="tipoFrequencia" required>
                    <option value="CHECKIN">Check-in único por QR</option>
                    <option value="ENTRADA_SAIDA">Entrada e saída por QR</option>
                    <option value="MANUAL">Validação manual pelo organizador</option>
                </select>
            </div>
        </div>
        <button type="submit" class="btn btn-sucesso">Salvar atividade</button>
    </form>
</div>
<?php endif; ?>

<?php if ($atividadesResp['erro']): ?>
    <div class="alerta alerta-erro"><?= htmlspecialchars($atividadesResp['msg_erro']) ?></div>
<?php elseif (empty($atividades)): ?>
    <div class="card"><p class="text-muted">Ainda não há atividades cadastradas para este evento.</p></div>
<?php else: ?>
    <?php if ($membroAtivo): ?>
    <form method="POST" action="atividades.php?evento_id=<?= $idEvento ?>" class="card">
        <input type="hidden" name="acao" value="salvar_atividades">
        <input type="hidden" name="id_inscricao_evento" value="<?= (int)$inscricaoEvento['id'] ?>">
        <div class="tabela-responsiva"><table class="tabela">
            <thead><tr><th>Escolher</th><th>Atividade</th><th>Data</th><th>Horário</th><th>Presença</th><th>Vagas restantes</th></tr></thead>
            <tbody><?php foreach ($atividades as $atividade):
                $idAtividade = (int)($atividade['id'] ?? 0);
                $limite = !empty($atividade['controlaVagas']) && (int)($atividade['vagas'] ?? 0) <= 0;
                $tipoPresenca = $atividade['tipoFrequencia'] ?? 'CHECKIN';
            ?>
                <tr>
                    <td><input type="checkbox" name="ids_atividades[]" value="<?= $idAtividade ?>" <?= isset($selecionadas[$idAtividade]) ? 'checked' : '' ?> <?= $limite && !isset($selecionadas[$idAtividade]) ? 'disabled' : '' ?> aria-label="Selecionar atividade"></td>
                    <td><strong><?= htmlspecialchars($atividade['nome'] ?? 'Atividade') ?></strong></td>
                    <td><?= htmlspecialchars($atividade['data'] ?? '—') ?></td>
                    <td><?= htmlspecialchars($atividade['horaInicio'] ?? '—') ?>–<?= htmlspecialchars($atividade['horaFim'] ?? '—') ?></td>
                    <td><?= htmlspecialchars($tipoPresenca) ?></td>
                    <td><?= !empty($atividade['controlaVagas']) ? (int)($atividade['vagas'] ?? 0) : 'Ilimitadas' ?></td>
                </tr>
            <?php endforeach; ?></tbody>
        </table></div>
        <p class="text-muted mt-sm">O servidor rejeita atividades com horários sobrepostos e inscrições sem vagas.</p>
        <button class="btn btn-primario mt-sm" type="submit">Salvar minha agenda</button>
    </form>
    <?php else: ?>
    <div class="card"><div class="tabela-responsiva"><table class="tabela">
        <thead><tr><th>Atividade</th><th>Data</th><th>Horário</th><th>Presença</th><th>Vagas</th></tr></thead>
        <tbody><?php foreach ($atividades as $atividade): ?><tr>
            <td><?= htmlspecialchars($atividade['nome'] ?? '—') ?></td><td><?= htmlspecialchars($atividade['data'] ?? '—') ?></td>
            <td><?= htmlspecialchars($atividade['horaInicio'] ?? '—') ?>–<?= htmlspecialchars($atividade['horaFim'] ?? '—') ?></td>
            <td><?= htmlspecialchars($atividade['tipoFrequencia'] ?? 'CHECKIN') ?></td>
            <td><?= !empty($atividade['controlaVagas']) ? (int)($atividade['vagas'] ?? 0) : 'Ilimitadas' ?></td>
        </tr><?php endforeach; ?></tbody>
    </table></div></div>
    <?php endif; ?>
<?php endif; ?>

<?php
$conteudo = ob_get_clean();
$titulo = 'Atividades do Evento';
$paginaAtiva = 'eventos';
include __DIR__ . '/layout.php';
