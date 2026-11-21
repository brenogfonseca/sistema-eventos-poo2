<?php
require_once __DIR__ . '/api_service.php';
exigir_login();

$resultadoEventos = chamar_api('GET', '/eventos');
$eventos = $resultadoEventos['erro'] ? [] : ($resultadoEventos['corpo'] ?? []);
$erro = $resultadoEventos['erro'] ? $resultadoEventos['msg_erro'] : '';
$idEvento = filter_input(INPUT_GET, 'evento_id', FILTER_VALIDATE_INT) ?: 0;
$tipo = $_GET['tipo'] ?? '';

if ($idEvento > 0 && in_array($tipo, ['inscricoes', 'frequencias'], true)) {
    $endpoint = "/relatorios/eventos/{$idEvento}/{$tipo}.csv";
    $relatorio = chamar_api('GET', $endpoint, null, headers_autenticados());
    if (!$relatorio['erro'] && is_string($relatorio['corpo'])) {
        $nome = $tipo === 'inscricoes' ? 'inscricoes' : 'frequencias';
        header('Content-Type: text/csv; charset=utf-8');
        header("Content-Disposition: attachment; filename={$nome}-evento-{$idEvento}.csv");
        echo $relatorio['corpo'];
        exit;
    }
    $erro = $relatorio['msg_erro'] ?: 'Você não tem permissão para exportar esse relatório.';
}

ob_start();
?>
<div class="secao-cabecalho">
    <div><h1 class="secao-titulo">Relatórios do evento</h1><p class="secao-subtitulo">Exporte inscrições e frequência em CSV para análise.</p></div>
    <a class="btn btn-secundario" href="eventos.php">← Eventos</a>
</div>
<?php if ($erro): ?><div class="alerta alerta-erro"><?= htmlspecialchars($erro) ?></div><?php endif; ?>
<div class="card">
    <form method="GET" action="relatorios.php">
        <div class="form-grupo">
            <label for="evento_id">Evento</label>
            <select name="evento_id" id="evento_id" required>
                <option value="">Selecione um evento</option>
                <?php foreach ($eventos as $evento): ?>
                    <option value="<?= (int)($evento['id'] ?? 0) ?>" <?= (int)($evento['id'] ?? 0) === $idEvento ? 'selected' : '' ?>><?= htmlspecialchars($evento['titulo'] ?? 'Evento') ?></option>
                <?php endforeach; ?>
            </select>
        </div>
        <button class="btn btn-primario" type="submit" name="tipo" value="inscricoes">Baixar inscrições CSV</button>
        <button class="btn btn-secundario" type="submit" name="tipo" value="frequencias">Baixar frequência CSV</button>
    </form>
    <p class="text-muted mt-sm">Os arquivos contêm dados dos participantes e só podem ser baixados por administradores e organizadores do evento.</p>
</div>
<?php
$conteudo = ob_get_clean();
$titulo = 'Relatórios';
$paginaAtiva = 'relatorios';
include __DIR__ . '/layout.php';
