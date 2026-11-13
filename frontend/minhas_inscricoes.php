<?php
require_once __DIR__ . '/api_service.php';
exigir_login();

$idUsuario = id_usuario_logado();
iniciar_sessao();
if ($_SERVER['REQUEST_METHOD'] === 'POST' && ($_POST['acao'] ?? '') === 'cancelar_evento') {
    $idInscricaoEvento = (int)($_POST['id_inscricao_evento'] ?? 0);
    $cancelamento = chamar_api('DELETE', "/inscricoes/evento/{$idInscricaoEvento}", null, headers_autenticados());
    $_SESSION['inscricoes_feedback'] = $cancelamento['erro']
        ? ['sucesso' => false, 'mensagem' => $cancelamento['msg_erro']]
        : ['sucesso' => true, 'mensagem' => 'Inscrição no evento cancelada. As vagas das atividades foram liberadas.'];
    header('Location: minhas_inscricoes.php');
    exit;
}
$feedback = $_SESSION['inscricoes_feedback'] ?? null;
unset($_SESSION['inscricoes_feedback']);
$resultado = chamar_api('GET', "/inscricoes/eventos/usuario/{$idUsuario}", null, headers_autenticados());
$inscricoes = $resultado['erro'] ? [] : ($resultado['corpo'] ?? []);
$erro = $resultado['erro'] ? ($resultado['msg_erro'] ?? '') : '';

ob_start();
?>
<div class="secao-cabecalho">
    <div><h1 class="secao-titulo">Minhas inscrições</h1><p class="secao-subtitulo">Sua inscrição nos eventos, atividades escolhidas e QR Code de acesso.</p></div>
    <a href="eventos.php" class="btn btn-secundario">← Ver eventos</a>
</div>

<?php if ($erro): ?><div class="alerta alerta-erro">⚠️ <?= htmlspecialchars($erro) ?></div><?php endif; ?>
<?php if ($feedback): ?><div class="alerta <?= $feedback['sucesso'] ? 'alerta-sucesso' : 'alerta-erro' ?>"><?= htmlspecialchars($feedback['mensagem']) ?></div><?php endif; ?>
<?php if (!$erro && !$inscricoes): ?>
    <div class="card text-center" style="padding:3rem"><p>Você ainda não está inscrito em eventos.</p><a href="eventos.php" class="btn btn-primario">Explorar eventos</a></div>
<?php endif; ?>

<?php foreach ($inscricoes as $inscricao):
    $idInscricaoEvento = (int)($inscricao['id'] ?? 0);
    $idEvento = (int)($inscricao['idEvento'] ?? 0);
    $ativo = empty($inscricao['cancelada']);
    $eventoResp = chamar_api('GET', "/eventos/{$idEvento}");
    $evento = $eventoResp['erro'] ? [] : ($eventoResp['corpo'] ?? []);
    $atividadesResp = $ativo ? chamar_api('GET', "/inscricoes/evento/{$idInscricaoEvento}/atividades", null, headers_autenticados()) : ['erro' => false, 'corpo' => []];
    $qrResp = $ativo ? chamar_api('GET', "/inscricoes/evento/{$idInscricaoEvento}/checkin-qr", null, headers_autenticados()) : ['erro' => true, 'msg_erro' => 'Inscrição cancelada.'];
    $frequenciaResp = $ativo ? chamar_api('GET', "/inscricoes/evento/{$idInscricaoEvento}/frequencia-certificado", null, headers_autenticados()) : ['erro' => true];
    $frequencia = $frequenciaResp['erro'] ? null : ($frequenciaResp['corpo'] ?? null);
?>
<section class="card mb-md">
    <div class="secao-cabecalho">
        <div>
            <h2><?= htmlspecialchars($evento['titulo'] ?? "Evento #{$idEvento}") ?></h2>
            <span class="badge <?= $ativo ? 'badge-ativo' : 'badge-cancelado' ?>"><?= $ativo ? 'Inscrição ativa' : 'Cancelada' ?></span>
        </div>
        <?php if ($ativo): ?><a class="btn btn-secundario" href="atividades.php?evento_id=<?= $idEvento ?>">Editar agenda</a><?php endif; ?>
    </div>

    <?php if ($ativo): ?>
        <form method="POST" onsubmit="return confirm('Cancelar a inscrição no evento e todas as atividades escolhidas?');">
            <input type="hidden" name="acao" value="cancelar_evento">
            <input type="hidden" name="id_inscricao_evento" value="<?= $idInscricaoEvento ?>">
            <button type="submit" class="btn btn-perigo">Cancelar inscrição no evento</button>
        </form>
    <?php endif; ?>

    <h3>Atividades escolhidas</h3>
    <?php $escolhidas = $atividadesResp['erro'] ? [] : ($atividadesResp['corpo'] ?? []); ?>
    <?php if (!$ativo): ?>
        <p class="text-muted">A inscrição foi cancelada.</p>
    <?php elseif (!$escolhidas): ?>
        <p class="text-muted">Você ainda não escolheu atividades. <a href="atividades.php?evento_id=<?= $idEvento ?>">Monte sua agenda</a>.</p>
    <?php else: ?>
        <ul>
        <?php foreach ($escolhidas as $escolhida):
            if (!empty($escolhida['cancelada'])) continue;
            $idAtividade = (int)($escolhida['idAtividade'] ?? 0);
            $atividadeResp = chamar_api('GET', "/atividades/{$idAtividade}");
            $atividade = $atividadeResp['erro'] ? [] : ($atividadeResp['corpo'] ?? []);
        ?>
            <li><?= htmlspecialchars($atividade['nome'] ?? "Atividade #{$idAtividade}") ?> — <?= htmlspecialchars($atividade['data'] ?? '') ?> <?= htmlspecialchars($atividade['horaInicio'] ?? '') ?>–<?= htmlspecialchars($atividade['horaFim'] ?? '') ?></li>
        <?php endforeach; ?>
        </ul>
    <?php endif; ?>

    <?php if ($ativo && $frequencia && !empty($frequencia['certificadoHabilitado'])): ?>
        <p><strong>Frequência computada:</strong> <?= number_format((float)$frequencia['percentualFrequencia'], 2, ',', '.') ?>%
            (mínimo <?= (int)$frequencia['frequenciaMinima'] ?>%) —
            <?= !empty($frequencia['elegivel']) ? 'elegível ao certificado' : 'ainda não elegível' ?>
        </p>
    <?php endif; ?>

    <?php if ($ativo && !$qrResp['erro'] && !empty($qrResp['corpo']['imagemBase64'])): ?>
        <div class="mt-md">
            <h3>Seu QR Code único para este evento</h3>
            <img src="data:image/png;base64,<?= htmlspecialchars($qrResp['corpo']['imagemBase64'], ENT_QUOTES, 'UTF-8') ?>" alt="QR Code da inscrição no evento" width="180" height="180">
            <p class="text-muted">Mostre este mesmo QR ao organizador em cada atividade escolhida. Válido até <?= htmlspecialchars(date('d/m/Y H:i', strtotime($qrResp['corpo']['expiraEm'] ?? ''))) ?>.</p>
        </div>
    <?php elseif ($ativo && $qrResp['erro']): ?>
        <p class="text-muted">QR Code indisponível: <?= htmlspecialchars($qrResp['msg_erro'] ?? 'erro ao gerar o código') ?></p>
    <?php endif; ?>
</section>
<?php endforeach; ?>

<?php
$conteudo = ob_get_clean();
$titulo = 'Minhas Inscrições';
$paginaAtiva = 'inscricoes';
include __DIR__ . '/layout.php';
