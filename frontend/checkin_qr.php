<?php
require_once __DIR__ . '/api_service.php';
exigir_login();
iniciar_sessao();

$idUsuario = id_usuario_logado();
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $token = trim($_POST['token'] ?? '');
    $idEvento = (int)($_POST['id_evento'] ?? 0);
    $idAtividade = (int)($_POST['id_atividade'] ?? 0);
    $idInscricaoEvento = (int)($_POST['id_inscricao_evento'] ?? 0);
    $acao = trim($_POST['acao_presenca'] ?? '');
    if ($idAtividade <= 0 || $acao === '') {
        $_SESSION['checkin_qr_feedback'] = ['sucesso' => false, 'mensagem' => 'Escolha a atividade e a ação de presença.'];
    } elseif ($idInscricaoEvento > 0) {
        $resultado = chamar_api('POST', '/frequencias/checkin-participante',
            ['idInscricaoEvento' => $idInscricaoEvento, 'idAtividade' => $idAtividade, 'acao' => $acao], headers_autenticados());
        $_SESSION['checkin_qr_feedback'] = $resultado['erro']
            ? ['sucesso' => false, 'mensagem' => $resultado['msg_erro']]
            : ['sucesso' => true, 'mensagem' => 'Presença registrada para a atividade selecionada.'];
    } elseif ($token === '') {
        $_SESSION['checkin_qr_feedback'] = ['sucesso' => false, 'mensagem' => 'Leia o QR Code do participante.'];
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
$validasResp = chamar_api('GET', '/frequencias/atividades-validaveis', null, headers_autenticados());
$atividadesValidaveis = $validasResp['erro'] ? [] : ($validasResp['corpo'] ?? []);
$eventosPermitidos = [];
foreach ($atividadesValidaveis as $item) {
    $eventoId = (int)($item['idEvento'] ?? 0);
    if ($eventoId > 0) $eventosPermitidos[$eventoId] = ['id' => $eventoId, 'titulo' => $item['tituloEvento'] ?? ('Evento #' . $eventoId)];
}
$eventosPermitidos = array_values($eventosPermitidos);
$idEvento = (int)($_GET['evento_id'] ?? 0);
$eventoSelecionado = null;
foreach ($eventosPermitidos as $evento) if ((int)($evento['id'] ?? 0) === $idEvento) $eventoSelecionado = $evento;
if (!$eventoSelecionado && $eventosPermitidos) { $eventoSelecionado = $eventosPermitidos[0]; $idEvento = (int)$eventoSelecionado['id']; }
$atividades = array_values(array_filter($atividadesValidaveis,
    static fn($item) => (int)($item['idEvento'] ?? 0) === $idEvento));
$idAtividade = (int)($_GET['atividade_id'] ?? 0);
$atividadeSelecionada = null;
foreach ($atividades as $item) if ((int)($item['idAtividade'] ?? 0) === $idAtividade) $atividadeSelecionada = $item;
if (!$atividadeSelecionada && $atividades) { $atividadeSelecionada = $atividades[0]; $idAtividade = (int)$atividadeSelecionada['idAtividade']; }
$termoBusca = trim($_GET['q'] ?? '');
$participantes = [];
if ($atividadeSelecionada && ($atividadeSelecionada['metodoIdentificacaoCheckin'] ?? 'QR') === 'NOME_CPF' && $termoBusca !== '') {
    $buscaResp = chamar_api('GET', "/atividades/{$idAtividade}/participantes-checkin?q=" . rawurlencode($termoBusca), null, headers_autenticados());
    if (!$buscaResp['erro']) $participantes = $buscaResp['corpo'] ?? [];
}

ob_start();
?>
<div class="secao-cabecalho"><div><h1 class="secao-titulo">Validação de presença</h1><p class="secao-subtitulo">Organizadores e colaboradores podem registrar presença nas atividades autorizadas. Somente organizadores corrigem presença ou falta.</p></div></div>

<?php if ($feedback): ?><div class="alerta <?= $feedback['sucesso'] ? 'alerta-sucesso' : 'alerta-erro' ?>"><?= htmlspecialchars($feedback['mensagem'], ENT_QUOTES, 'UTF-8') ?></div><?php endif; ?>

<?php if (!$eventosPermitidos): ?>
    <div class="card"><p>Você não tem atividades autorizadas para validar. O organizador do evento precisa vincular você às atividades correspondentes.</p></div>
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
            <select name="atividade_id" id="atividade_id" required onchange="this.form.submit()">
                <?php foreach ($atividades as $atividade): ?><option value="<?= (int)$atividade['idAtividade'] ?>" data-politica="<?= htmlspecialchars($atividade['tipoFrequencia'] ?? 'CHECKIN') ?>" <?= (int)$atividade['idAtividade'] === $idAtividade ? 'selected' : '' ?>><?= htmlspecialchars($atividade['nomeAtividade'] ?? ('Atividade #' . $atividade['idAtividade'])) ?> — <?= htmlspecialchars($atividade['tipoFrequencia'] ?? 'CHECKIN') ?></option><?php endforeach; ?>
            </select>
        </div>
        <noscript><button class="btn btn-secundario" type="submit">Carregar atividades</button></noscript>
    </form>

    <?php if (!$atividadeSelecionada): ?><p class="text-muted">Não há atividades atribuídas a você neste evento.</p><?php else: ?>
    <h2>2. Valide a presença</h2>
    <p>Atividade: <strong><?= htmlspecialchars($atividadeSelecionada['nomeAtividade'] ?? '') ?></strong> · <?= htmlspecialchars($atividadeSelecionada['data'] ?? '') ?> <?= htmlspecialchars($atividadeSelecionada['horaInicio'] ?? '') ?>–<?= htmlspecialchars($atividadeSelecionada['horaFim'] ?? '') ?></p>
    <p>Método: <strong><?= ($atividadeSelecionada['metodoIdentificacaoCheckin'] ?? 'QR') === 'QR' ? 'QR Code' : 'Busca por nome ou CPF' ?></strong></p>
    <?php if (($atividadeSelecionada['metodoIdentificacaoCheckin'] ?? 'QR') === 'QR'): ?>
        <form method="POST" action="checkin_qr.php" id="form-checkin-qr">
            <input type="hidden" name="id_evento" value="<?= $idEvento ?>">
            <input type="hidden" name="id_atividade" id="id_atividade_post" value="<?= $idAtividade ?>">
            <div class="form-grupo"><label for="acao_presenca">Ação</label><select name="acao_presenca" id="acao_presenca" required>
                <?php if (($atividadeSelecionada['tipoFrequencia'] ?? '') === 'CHECKIN'): ?><option value="CHECKIN">Registrar check-in</option>
                <?php elseif (($atividadeSelecionada['tipoFrequencia'] ?? '') === 'ENTRADA_SAIDA'): ?><option value="ENTRADA">Registrar entrada</option><option value="SAIDA">Registrar saída</option>
                <?php else: ?><option value="PRESENTE">Registrar presença</option><?php endif; ?>
            </select></div>
            <button type="button" class="btn btn-primario" id="iniciar-camera">Abrir câmera</button>
            <p id="status-camera" class="text-muted" aria-live="polite"></p>
            <video id="video-qr" autoplay playsinline style="display:none;width:100%;max-height:420px;background:#111;border-radius:8px"></video>
            <div class="form-grupo mt-md"><label for="token">Token QR</label><textarea id="token" name="token" rows="3" required placeholder="Leia o QR Code do participante ou cole o token"></textarea></div>
            <button type="submit" class="btn btn-sucesso">Registrar presença</button>
        </form>
    <?php else: ?>
        <form method="GET" action="checkin_qr.php" class="form-linha">
            <input type="hidden" name="evento_id" value="<?= $idEvento ?>"><input type="hidden" name="atividade_id" value="<?= $idAtividade ?>">
            <div class="form-grupo"><label for="q">Buscar participante por nome ou CPF</label><input id="q" name="q" value="<?= htmlspecialchars($termoBusca) ?>" minlength="2" required></div>
            <button class="btn btn-secundario" type="submit">Buscar</button>
        </form>
        <?php if ($termoBusca !== '' && !$participantes): ?><p class="text-muted">Nenhum inscrito ativo desta atividade corresponde à busca.</p><?php endif; ?>
        <?php if ($participantes): ?>
            <form method="POST" action="checkin_qr.php">
                <input type="hidden" name="id_evento" value="<?= $idEvento ?>"><input type="hidden" name="id_atividade" value="<?= $idAtividade ?>">
                <div class="form-grupo"><label for="id_inscricao_evento">Participante</label><select name="id_inscricao_evento" id="id_inscricao_evento" required>
                    <?php foreach ($participantes as $participante): ?><option value="<?= (int)$participante['idInscricaoEvento'] ?>"><?= htmlspecialchars($participante['nome']) ?> — <?= htmlspecialchars($participante['cpfMascarado'] ?? 'CPF não cadastrado') ?></option><?php endforeach; ?>
                </select></div>
                <div class="form-grupo"><label for="acao_presenca">Ação</label><select name="acao_presenca" id="acao_presenca" required>
                    <?php if (($atividadeSelecionada['tipoFrequencia'] ?? '') === 'CHECKIN'): ?><option value="CHECKIN">Registrar check-in</option>
                    <?php elseif (($atividadeSelecionada['tipoFrequencia'] ?? '') === 'ENTRADA_SAIDA'): ?><option value="ENTRADA">Registrar entrada</option><option value="SAIDA">Registrar saída</option>
                    <?php else: ?><option value="PRESENTE">Registrar presença</option><?php endif; ?>
                </select></div>
                <button type="submit" class="btn btn-sucesso">Registrar presença</button>
            </form>
        <?php endif; ?>
    <?php endif; ?>
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
            : politica === 'ENTRADA_SAIDA' ? ['ENTRADA', 'SAIDA'] : ['PRESENTE'];
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
