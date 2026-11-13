<?php
require_once __DIR__ . '/api_service.php';
exigir_login();
iniciar_sessao();

$idUsuario = id_usuario_logado();
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $token = trim($_POST['token'] ?? '');
    $idEvento = (int)($_POST['id_evento'] ?? 0);
    $idAtividade = (int)($_POST['id_atividade'] ?? 0);
    $acao = trim($_POST['acao_presenca'] ?? '');
    if ($token === '' || $idAtividade <= 0 || $acao === '') {
        $_SESSION['checkin_qr_feedback'] = ['sucesso' => false, 'mensagem' => 'Escolha a atividade, a ação e leia um QR Code.'];
    } else {
        $resultado = chamar_api('POST', '/frequencias/checkin-qr',
            ['token' => $token, 'idAtividade' => $idAtividade, 'acao' => $acao], headers_autenticados());
        $_SESSION['checkin_qr_feedback'] = $resultado['erro']
            ? ['sucesso' => false, 'mensagem' => $resultado['msg_erro']]
            : ['sucesso' => true, 'mensagem' => 'Presença registrada para a atividade selecionada.'];
    }
    header('Location: checkin_qr.php?evento_id=' . $idEvento . '&atividade_id=' . $idAtividade . '&acao=' . rawurlencode($acao));
    exit;
}

$feedback = $_SESSION['checkin_qr_feedback'] ?? null;
unset($_SESSION['checkin_qr_feedback']);
$eventosResp = chamar_api('GET', '/eventos', null, headers_autenticados());
$eventos = $eventosResp['erro'] ? [] : ($eventosResp['corpo'] ?? []);
$eventosPermitidos = [];
foreach ($eventos as $evento) {
    if (eh_administrador() || in_array($idUsuario, $evento['organizadoresIds'] ?? [])) $eventosPermitidos[] = $evento;
}
$idEvento = (int)($_GET['evento_id'] ?? 0);
$eventoSelecionado = null;
foreach ($eventosPermitidos as $evento) if ((int)($evento['id'] ?? 0) === $idEvento) $eventoSelecionado = $evento;
if (!$eventoSelecionado && $eventosPermitidos) { $eventoSelecionado = $eventosPermitidos[0]; $idEvento = (int)$eventoSelecionado['id']; }
$atividadesResp = $idEvento ? chamar_api('GET', "/atividades/evento/{$idEvento}") : ['erro' => false, 'corpo' => []];
$atividades = $atividadesResp['erro'] ? [] : ($atividadesResp['corpo'] ?? []);
$idAtividade = (int)($_GET['atividade_id'] ?? 0);
$acaoSelecionada = $_GET['acao'] ?? '';

ob_start();
?>
<div class="secao-cabecalho"><div><h1 class="secao-titulo">Leitor de QR Code</h1><p class="secao-subtitulo">O organizador escolhe a atividade e valida a presença do participante com o QR único do evento.</p></div></div>

<?php if ($feedback): ?><div class="alerta <?= $feedback['sucesso'] ? 'alerta-sucesso' : 'alerta-erro' ?>"><?= htmlspecialchars($feedback['mensagem'], ENT_QUOTES, 'UTF-8') ?></div><?php endif; ?>

<?php if (!$eventosPermitidos): ?>
    <div class="card"><p>Você não está vinculado como organizador a nenhum evento.</p></div>
<?php else: ?>
<div class="card" style="max-width:760px">
    <h2>1. Selecione evento e atividade</h2>
    <form method="GET" action="checkin_qr.php">
        <div class="form-grupo"><label for="evento_id">Evento</label>
            <select name="evento_id" id="evento_id" onchange="this.form.submit()">
                <?php foreach ($eventosPermitidos as $evento): ?><option value="<?= (int)$evento['id'] ?>" <?= (int)$evento['id'] === $idEvento ? 'selected' : '' ?>><?= htmlspecialchars($evento['titulo'] ?? ('Evento #' . $evento['id'])) ?></option><?php endforeach; ?>
            </select>
        </div>
        <div class="form-grupo"><label for="atividade_id">Atividade</label>
            <select name="atividade_id" id="atividade_id" required>
                <?php foreach ($atividades as $atividade): ?><option value="<?= (int)$atividade['id'] ?>" data-politica="<?= htmlspecialchars($atividade['tipoFrequencia'] ?? 'CHECKIN') ?>" <?= (int)$atividade['id'] === $idAtividade ? 'selected' : '' ?>><?= htmlspecialchars($atividade['nome'] ?? ('Atividade #' . $atividade['id'])) ?> — <?= htmlspecialchars($atividade['tipoFrequencia'] ?? 'CHECKIN') ?></option><?php endforeach; ?>
            </select>
        </div>
        <noscript><button class="btn btn-secundario" type="submit">Carregar atividades</button></noscript>
    </form>

    <?php if (!$atividades): ?><p class="text-muted">Este evento ainda não possui atividades.</p><?php else: ?>
    <h2>2. Valide a presença</h2>
    <form method="POST" action="checkin_qr.php" id="form-checkin-qr">
        <input type="hidden" name="id_evento" value="<?= $idEvento ?>">
        <input type="hidden" name="id_atividade" id="id_atividade_post" value="<?= $idAtividade ?: (int)$atividades[0]['id'] ?>">
        <div class="form-grupo"><label for="acao_presenca">Ação</label>
            <select name="acao_presenca" id="acao_presenca" required>
                <option value="CHECKIN">Registrar check-in</option>
                <option value="ENTRADA">Registrar entrada</option>
                <option value="SAIDA">Registrar saída</option>
                <option value="PRESENTE">Validar como presente (manual)</option>
                <option value="AUSENTE">Validar como ausente (manual)</option>
            </select>
        </div>
        <button type="button" class="btn btn-primario" id="iniciar-camera">Abrir câmera</button>
        <p id="status-camera" class="text-muted" aria-live="polite"></p>
        <video id="video-qr" autoplay playsinline style="display:none;width:100%;max-height:420px;background:#111;border-radius:8px"></video>
        <div class="form-grupo mt-md"><label for="token">Token (preenchido pela câmera ou colado manualmente)</label>
            <textarea id="token" name="token" rows="3" required placeholder="Leia o QR Code do participante ou cole o token"></textarea>
        </div>
        <button type="submit" class="btn btn-sucesso">Confirmar presença nesta atividade</button>
    </form>
    <?php endif; ?>
</div>
<?php endif; ?>

<script>
(() => {
    const atividade = document.getElementById('atividade_id');
    const acao = document.getElementById('acao_presenca');
    const idPost = document.getElementById('id_atividade_post');
    if (!atividade || !acao || !idPost) return;
    const opcoes = [...acao.options];
    function ajustarAcoes() {
        const politica = atividade.selectedOptions[0]?.dataset.politica || 'CHECKIN';
        const permitidas = politica === 'CHECKIN' ? ['CHECKIN']
            : politica === 'ENTRADA_SAIDA' ? ['ENTRADA', 'SAIDA'] : ['PRESENTE', 'AUSENTE'];
        for (const option of opcoes) option.hidden = !permitidas.includes(option.value);
        if (!permitidas.includes(acao.value)) acao.value = permitidas[0];
        idPost.value = atividade.value;
    }
    atividade.addEventListener('change', ajustarAcoes);
    ajustarAcoes();

    const botao = document.getElementById('iniciar-camera');
    const status = document.getElementById('status-camera');
    const video = document.getElementById('video-qr');
    const campoToken = document.getElementById('token');
    if (!botao) return;
    let stream = null;
    let lendo = false;
    async function iniciarLeitura() {
        if (!('BarcodeDetector' in window) || !navigator.mediaDevices?.getUserMedia) {
            status.textContent = 'Este navegador não oferece leitura por câmera. Cole o token no campo abaixo.'; return;
        }
        try {
            const detector = new BarcodeDetector({ formats: ['qr_code'] });
            stream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: { ideal: 'environment' } }, audio: false });
            video.srcObject = stream; video.style.display = 'block'; await video.play(); lendo = true; botao.disabled = true;
            status.textContent = 'Aponte a câmera para o QR Code do participante.';
            const procurar = async () => {
                if (!lendo) return;
                try {
                    const codigos = await detector.detect(video);
                    if (codigos.length && codigos[0].rawValue) {
                        lendo = false; campoToken.value = codigos[0].rawValue; stream?.getTracks().forEach(track => track.stop());
                        document.getElementById('form-checkin-qr').requestSubmit(); return;
                    }
                } catch (erro) { status.textContent = 'Não foi possível ler a imagem. Aproxime o QR ou cole o token.'; }
                window.setTimeout(procurar, 250);
            };
            procurar();
        } catch (erro) { status.textContent = 'Não foi possível abrir a câmera. Verifique as permissões.'; botao.disabled = false; }
    }
    botao.addEventListener('click', iniciarLeitura);
    window.addEventListener('pagehide', () => { lendo = false; stream?.getTracks().forEach(track => track.stop()); });
})();
</script>
<?php
$conteudo = ob_get_clean();
$titulo = 'Leitor QR';
$paginaAtiva = 'checkin_qr';
include __DIR__ . '/layout.php';
