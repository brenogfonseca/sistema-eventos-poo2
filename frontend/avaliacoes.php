<?php
require_once __DIR__ . '/api_service.php';
exigir_login();

$idAtividade = filter_input(INPUT_GET, 'atividade_id', FILTER_VALIDATE_INT) ?: (int)($_POST['atividade_id'] ?? 0);
if ($idAtividade <= 0) { header('Location: eventos.php'); exit; }
$headers = headers_autenticados();
$erro = '';
$sucesso = '';
$atividadeResp = chamar_api('GET', "/atividades/{$idAtividade}");
$atividade = $atividadeResp['erro'] ? null : ($atividadeResp['corpo'] ?? null);
$evento = null;
$gerenciador = false;
if ($atividade) {
    $idEvento = (int)($atividade['eventoId'] ?? 0);
    $eventoResp = chamar_api('GET', "/eventos/{$idEvento}");
    $evento = $eventoResp['erro'] ? null : ($eventoResp['corpo'] ?? null);
    $gerenciador = eh_administrador() || in_array(id_usuario_logado(), $evento['organizadoresIds'] ?? []);
}
if (!$atividade) $erro = $atividadeResp['msg_erro'] ?? 'Atividade não encontrada.';

if ($_SERVER['REQUEST_METHOD'] === 'POST' && $atividade) {
    $acao = $_POST['acao'] ?? '';
    if ($acao === 'criar_questionario' && $gerenciador) {
        $r = chamar_api('POST', '/questionarios', [
            'idAtividade' => $idAtividade,
            'titulo' => trim($_POST['titulo'] ?? ''),
            'anonimo' => !empty($_POST['anonimo']),
            'publicado' => false,
        ], $headers);
        if ($r['erro']) $erro = $r['msg_erro']; else $sucesso = 'Avaliação criada. Agora cadastre as questões e depois publique.';
    } elseif ($acao === 'atualizar_questionario' && $gerenciador) {
        $idQuestionario = (int)($_POST['id_questionario'] ?? 0);
        $r = chamar_api('PUT', "/questionarios/{$idQuestionario}", [
            'titulo' => trim($_POST['titulo'] ?? ''),
            'anonimo' => !empty($_POST['anonimo']),
            'publicado' => !empty($_POST['publicado']),
        ], $headers);
        if ($r['erro']) $erro = $r['msg_erro']; else $sucesso = 'Configuração da avaliação atualizada.';
    } elseif ($acao === 'criar_questao' && $gerenciador) {
        $tipo = $_POST['tipo'] ?? 'TEXTUAL';
        $opcoes = preg_split('/\R/u', trim($_POST['opcoes'] ?? ''), -1, PREG_SPLIT_NO_EMPTY);
        $r = chamar_api('POST', '/questoes', [
            'idQuestionario' => (int)($_POST['id_questionario'] ?? 0),
            'enunciado' => trim($_POST['enunciado'] ?? ''),
            'tipo' => $tipo,
            'opcoes' => $tipo === 'ESCOLHA_UNICA' ? array_values($opcoes ?: []) : [],
            'escalaMinima' => $tipo === 'ESCALA_NUMERICA' ? (int)($_POST['escala_minima'] ?? 1) : null,
            'escalaMaxima' => $tipo === 'ESCALA_NUMERICA' ? (int)($_POST['escala_maxima'] ?? 5) : null,
        ], $headers);
        if ($r['erro']) $erro = $r['msg_erro']; else $sucesso = 'Questão adicionada.';
    } elseif ($acao === 'responder') {
        $r = chamar_api('POST', '/respostas', [
            'idQuestao' => (int)($_POST['id_questao'] ?? 0),
            'valor' => trim($_POST['valor'] ?? ''),
        ], $headers);
        if ($r['erro']) $erro = $r['msg_erro']; else $sucesso = 'Resposta registrada.';
    }
}

$questionarioResp = $atividade ? chamar_api('GET', "/questionarios/atividade/{$idAtividade}", null, $headers) : ['erro' => true];
$questionario = $questionarioResp['erro'] ? null : ($questionarioResp['corpo'] ?? null);
$questoes = [];
if ($questionario) {
    $questoesResp = chamar_api('GET', "/questoes/questionario/{$questionario['id']}", null, $headers);
    $questoes = $questoesResp['erro'] ? [] : ($questoesResp['corpo'] ?? []);
}
if (!empty($_GET['baixar']) && $questionario) {
    $relatorio = chamar_api('GET', "/questionarios/{$questionario['id']}/resultados.csv", null, $headers);
    if (!$relatorio['erro'] && is_string($relatorio['corpo'])) {
        header('Content-Type: text/csv; charset=utf-8');
        header('Content-Disposition: attachment; filename=avaliacao-' . (int)$questionario['id'] . '.csv');
        echo $relatorio['corpo'];
        exit;
    }
    $erro = $relatorio['msg_erro'] ?: 'Não foi possível exportar os resultados.';
}
if (!$questionario && !$gerenciador && !$erro) $erro = 'Esta atividade ainda não possui avaliação publicada.';

ob_start();
?>
<div class="secao-cabecalho"><div><a href="atividades.php?evento_id=<?= (int)($atividade['eventoId'] ?? 0) ?>" class="text-muted">← Voltar à programação</a><h1 class="secao-titulo mt-sm">Avaliação da atividade</h1><p class="secao-subtitulo"><?= htmlspecialchars($atividade['nome'] ?? '') ?></p></div></div>
<?php if ($erro): ?><div class="alerta alerta-erro"><?= htmlspecialchars($erro) ?></div><?php endif; ?>
<?php if ($sucesso): ?><div class="alerta alerta-sucesso"><?= htmlspecialchars($sucesso) ?></div><?php endif; ?>

<?php if (!$questionario && $gerenciador): ?>
<div class="card"><h2>Criar avaliação</h2><form method="POST">
    <input type="hidden" name="atividade_id" value="<?= $idAtividade ?>"><input type="hidden" name="acao" value="criar_questionario">
    <div class="form-grupo"><label>Título</label><input name="titulo" required maxlength="160"></div>
    <label><input type="checkbox" name="anonimo" value="1"> Ocultar a identificação nas respostas exportadas</label>
    <button class="btn btn-primario mt-sm">Criar questionário</button>
</form></div>
<?php endif; ?>

<?php if ($questionario): ?>
<div class="card mb-md">
    <h2><?= htmlspecialchars($questionario['titulo'] ?? 'Avaliação') ?></h2>
    <?php if ($gerenciador): ?>
    <p><a class="btn btn-secundario" href="avaliacoes.php?atividade_id=<?= $idAtividade ?>&baixar=1&questionario_id=<?= (int)$questionario['id'] ?>">Baixar resultados CSV</a></p>
    <form method="POST" class="mb-md"><input type="hidden" name="atividade_id" value="<?= $idAtividade ?>"><input type="hidden" name="id_questionario" value="<?= (int)$questionario['id'] ?>"><input type="hidden" name="acao" value="atualizar_questionario">
        <div class="form-grupo"><label>Título</label><input name="titulo" value="<?= htmlspecialchars($questionario['titulo'] ?? '') ?>" required></div>
        <label><input type="checkbox" name="anonimo" value="1" <?= !empty($questionario['anonimo']) ? 'checked' : '' ?>> Respostas anônimas para o organizador</label><br>
        <label><input type="checkbox" name="publicado" value="1" <?= !empty($questionario['publicado']) ? 'checked' : '' ?>> Publicar para participantes com presença validada</label><br>
        <button class="btn btn-secundario mt-sm">Salvar configuração</button>
    </form>
    <details><summary><strong>Adicionar questão</strong></summary><form method="POST" class="mt-md">
        <input type="hidden" name="atividade_id" value="<?= $idAtividade ?>"><input type="hidden" name="id_questionario" value="<?= (int)$questionario['id'] ?>"><input type="hidden" name="acao" value="criar_questao">
        <div class="form-grupo"><label>Enunciado</label><input name="enunciado" required maxlength="1000"></div>
        <div class="form-linha"><div class="form-grupo"><label>Tipo</label><select name="tipo" id="tipo-questao"><option value="TEXTUAL">Resposta aberta</option><option value="ESCOLHA_UNICA">Escolha única</option><option value="ESCALA_NUMERICA">Escala numérica</option></select></div>
            <div class="form-grupo" id="campo-opcoes"><label>Opções (uma por linha)</label><textarea name="opcoes" rows="4"></textarea></div>
            <div class="form-grupo" id="campo-escala"><label>Faixa (mínimo e máximo)</label><div class="form-linha"><input type="number" name="escala_minima" value="1"><input type="number" name="escala_maxima" value="5"></div></div></div>
        <button class="btn btn-secundario">Adicionar questão</button>
    </form></details>
    <?php endif; ?>
</div>

<?php foreach ($questoes as $questao): ?>
<div class="card mb-md"><h3><?= htmlspecialchars($questao['enunciado'] ?? '') ?></h3>
    <?php if (!empty($questionario['publicado'])): ?>
        <form method="POST"><input type="hidden" name="atividade_id" value="<?= $idAtividade ?>"><input type="hidden" name="acao" value="responder"><input type="hidden" name="id_questao" value="<?= (int)$questao['id'] ?>">
        <?php if (($questao['tipo'] ?? '') === 'ESCOLHA_UNICA'): ?>
            <?php foreach (($questao['opcoes'] ?? []) as $opcao): ?><label class="d-block"><input type="radio" name="valor" value="<?= htmlspecialchars($opcao, ENT_QUOTES) ?>" required> <?= htmlspecialchars($opcao) ?></label><?php endforeach; ?>
        <?php elseif (($questao['tipo'] ?? '') === 'ESCALA_NUMERICA'): ?>
            <input type="number" name="valor" min="<?= (int)($questao['escalaMinima'] ?? 1) ?>" max="<?= (int)($questao['escalaMaxima'] ?? 5) ?>" required>
        <?php else: ?><textarea name="valor" rows="3" required maxlength="4000"></textarea><?php endif; ?>
        <button class="btn btn-primario mt-sm">Enviar resposta</button></form>
    <?php else: ?><p class="text-muted">Esta avaliação ainda não foi publicada.</p><?php endif; ?>
</div>
<?php endforeach; ?>
<?php if ($gerenciador && !$questoes): ?><div class="card"><p>Adicione ao menos uma questão antes de publicar a avaliação.</p></div><?php endif; ?>
<?php endif; ?>

<script>
(() => {
    const tipo = document.getElementById('tipo-questao');
    const opcoes = document.getElementById('campo-opcoes');
    const escala = document.getElementById('campo-escala');
    function atualizar() { if (!tipo) return; opcoes.hidden = tipo.value !== 'ESCOLHA_UNICA'; escala.hidden = tipo.value !== 'ESCALA_NUMERICA'; }
    tipo?.addEventListener('change', atualizar); atualizar();
})();
</script>
<?php
$conteudo = ob_get_clean();
$titulo = 'Avaliação';
$paginaAtiva = 'eventos';
include __DIR__ . '/layout.php';
