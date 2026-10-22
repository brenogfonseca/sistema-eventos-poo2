<?php
require_once __DIR__ . '/api_service.php';
exigir_login();

$idUsuario = id_usuario_logado();
iniciar_sessao();

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $token = trim($_POST['token'] ?? '');

    if ($token === '') {
        $_SESSION['checkin_qr_feedback'] = [
            'sucesso' => false,
            'mensagem' => 'Leia um QR Code ou cole o token para registrar o check-in.',
        ];
    } else {
        $resultado = chamar_api(
            'POST',
            '/frequencias/checkin-qr',
            ['token' => $token],
            headers_autenticados()
        );

        $_SESSION['checkin_qr_feedback'] = $resultado['erro']
            ? ['sucesso' => false, 'mensagem' => $resultado['msg_erro']]
            : ['sucesso' => true, 'mensagem' => 'Check-in registrado com sucesso.'];
    }

    header('Location: checkin_qr.php');
    exit;
}

$feedback = $_SESSION['checkin_qr_feedback'] ?? null;
unset($_SESSION['checkin_qr_feedback']);

ob_start();
?>
<div class="secao-cabecalho">
    <div>
        <h1 class="secao-titulo">Leitor de QR Code</h1>
        <p class="secao-subtitulo">Leia o QR de uma inscrição para registrar a presença.</p>
    </div>
</div>

<?php if ($feedback): ?>
    <div class="alerta <?= $feedback['sucesso'] ? 'alerta-sucesso' : 'alerta-erro' ?>">
        <?= htmlspecialchars($feedback['mensagem'], ENT_QUOTES, 'UTF-8') ?>
    </div>
<?php endif; ?>

<div class="card" style="max-width:720px;">
    <p>O check-in só pode ser registrado por um administrador ou organizador do evento.</p>
    <button type="button" class="btn btn-primario" id="iniciar-camera">
        Abrir câmera
    </button>
    <p id="status-camera" class="text-muted" aria-live="polite"></p>
    <video id="video-qr" autoplay playsinline style="display:none; width:100%; max-height:420px; background:#111; border-radius:8px;"></video>

    <form method="POST" action="checkin_qr.php" id="form-checkin-qr" class="mt-md">
        <div class="form-grupo">
            <label for="token">Token do QR Code</label>
            <textarea id="token" name="token" rows="3" required
                      placeholder="O token será preenchido após a leitura. Também é possível colá-lo aqui."></textarea>
        </div>
        <button type="submit" class="btn btn-sucesso">Registrar check-in</button>
    </form>
</div>

<script>
(() => {
    const botao = document.getElementById('iniciar-camera');
    const status = document.getElementById('status-camera');
    const video = document.getElementById('video-qr');
    const campoToken = document.getElementById('token');
    let stream = null;
    let lendo = false;

    async function iniciarLeitura() {
        if (!('BarcodeDetector' in window) || !navigator.mediaDevices?.getUserMedia) {
            status.textContent = 'Este navegador não oferece leitura de QR pela câmera. Cole o token no campo abaixo.';
            return;
        }

        try {
            const detector = new BarcodeDetector({ formats: ['qr_code'] });
            stream = await navigator.mediaDevices.getUserMedia({
                video: { facingMode: { ideal: 'environment' } },
                audio: false
            });
            video.srcObject = stream;
            video.style.display = 'block';
            await video.play();
            lendo = true;
            status.textContent = 'Aponte a câmera para o QR Code.';
            botao.disabled = true;

            const procurar = async () => {
                if (!lendo) return;
                try {
                    const codigos = await detector.detect(video);
                    if (codigos.length > 0 && codigos[0].rawValue) {
                        lendo = false;
                        campoToken.value = codigos[0].rawValue;
                        status.textContent = 'QR lido. Enviando para validação…';
                        stream?.getTracks().forEach(track => track.stop());
                        document.getElementById('form-checkin-qr').requestSubmit();
                        return;
                    }
                } catch (erro) {
                    status.textContent = 'Não foi possível ler a imagem. Tente aproximar o QR ou cole o token manualmente.';
                }
                window.setTimeout(procurar, 250);
            };
            procurar();
        } catch (erro) {
            status.textContent = 'Não foi possível abrir a câmera. Verifique a permissão ou cole o token manualmente.';
            botao.disabled = false;
        }
    }

    botao.addEventListener('click', iniciarLeitura);
    window.addEventListener('pagehide', () => {
        lendo = false;
        stream?.getTracks().forEach(track => track.stop());
    });
})();
</script>

<?php
$conteudo = ob_get_clean();
$titulo = 'Leitor QR';
$paginaAtiva = 'checkin_qr';
include __DIR__ . '/layout.php';
