<?php
/**
 * atividades.php — Módulo de Eventos: Listagem e criação de atividades
 * ─────────────────────────────────────────────────────────────────────────────
 * FLUXO DIDÁTICO:
 *
 * [Listagem]
 *   1. Recebe o ID do evento via query string: ?evento_id=1
 *   2. Chama chamar_api('GET', '/atividades/evento/{idEvento}') para listar.
 *
 * [Cadastro — apenas admin]
 *   3. Formulário coleta: nome, local (objeto), horaInicio, horaFim, data,
 *      tipo, vagas, tipoFrequencia.
 *   4. Ao submeter via POST, monta o JSON e chama POST /atividades.
 *      Campos mapeados ao AtividadeRequest.java do AtividadeController.
 *
 * [Inscrição — usuário logado]
 *   5. Botão "Inscrever-se" envia POST /inscricoes com { idAtividade, idUsuario }.
 *
 * Endpoints utilizados:
 *   GET  /atividades/evento/{idEvento}
 *   POST /atividades
 *   POST /inscricoes
 * ─────────────────────────────────────────────────────────────────────────────
 */
require_once __DIR__ . '/api_service.php';
iniciar_sessao();

// Obtém o ID do evento da query string
$idEvento = (int)($_GET['evento_id'] ?? 0);

if ($idEvento <= 0) {
    header('Location: eventos.php');
    exit;
}

$erro    = '';
$sucesso = '';

// ── Processa inscrição em atividade (POST do form de inscrição) ───────────────
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['acao']) && $_POST['acao'] === 'inscrever') {
    exigir_login();

    $idAtividade = (int)($_POST['id_atividade'] ?? 0);
    $idUsuario   = id_usuario_logado();

    // Monta o JSON para POST /inscricoes
    // Conforme InscricaoController.InscricaoRequest: { idAtividade, idUsuario }
    $dados = [
        'idAtividade' => $idAtividade,
        'idUsuario'   => $idUsuario,
    ];

    $resultado = chamar_api('POST', '/inscricoes', $dados);

    if ($resultado['erro']) {
        $erro = $resultado['msg_erro'] ?: 'Erro ao se inscrever.';
    } else {
        $sucesso = 'Inscrição realizada com sucesso! ✅';
    }
}

// ── Processa criação de atividade (admin) ─────────────────────────────────────
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['acao']) && $_POST['acao'] === 'criar_atividade') {
    exigir_login();
    if (!eh_administrador()) {
        $erro = 'Apenas administradores podem criar atividades.';
    } else {
        $nome        = trim($_POST['nome']        ?? '');
        $localNome   = trim($_POST['local_nome']  ?? '');
        $horaInicio  = trim($_POST['hora_inicio'] ?? '');
        $horaFim     = trim($_POST['hora_fim']    ?? '');
        $data        = trim($_POST['data']        ?? '');
        $tipo        = trim($_POST['tipo']        ?? 'Palestra');
        $vagas       = (int)($_POST['vagas']      ?? 0);
        $controlaVagas = $vagas > 0;

        // AtividadeRequest.java usa LocalTime (HH:MM:SS) e LocalDate (yyyy-MM-dd)
        $dados = [
            'nome'          => $nome,
            'eventoId'      => $idEvento,
            'local'         => ['nome' => $localNome],  // Objeto Local com campo nome
            'horaInicio'    => $horaInicio . ':00',      // "08:00:00"
            'horaFim'       => $horaFim    . ':00',      // "09:00:00"
            'data'          => $data,                    // "2026-10-01"
            'tipo'          => $tipo,
            'vagas'         => $vagas,
            'controlaVagas' => $controlaVagas,
            'tipoFrequencia'=> 'PRESENCIAL',             // Enum TipoFrequenciaEnum
            'trilha'        => null,
        ];

        // Passa o header X-Usuario-Id pois o admin precisa estar autenticado
        $resultado = chamar_api('POST', '/atividades', $dados, headers_autenticados());

        if ($resultado['erro']) {
            $erro = $resultado['msg_erro'] ?: 'Erro ao criar atividade.';
        } else {
            $sucesso = 'Atividade criada com sucesso! ✅';
        }
    }
}

// ── Busca lista de atividades do evento ──────────────────────────────────────
$resAtividades = chamar_api('GET', "/atividades/evento/{$idEvento}");
$atividades    = [];
$erroListagem  = '';

if ($resAtividades['erro']) {
    $erroListagem = $resAtividades['msg_erro'];
} else {
    $atividades = $resAtividades['corpo'] ?? [];
}

// ── Monta o HTML ──────────────────────────────────────────────────────────────
ob_start();
?>

<div class="secao-cabecalho">
    <div>
        <a href="eventos.php" class="text-muted">← Voltar aos Eventos</a>
        <h1 class="secao-titulo mt-sm">🗂️ Atividades do Evento #<?= $idEvento ?></h1>
        <p class="secao-subtitulo">Selecione uma atividade e se inscreva</p>
    </div>
    <?php if (eh_administrador()): ?>
        <button
            onclick="document.getElementById('form-nova-atividade').classList.toggle('d-none')"
            class="btn btn-primario"
            id="btn-toggle-criar-atividade">
            + Nova Atividade
        </button>
    <?php endif; ?>
</div>

<!-- Alertas -->
<?php if ($sucesso): ?>
    <div class="alerta alerta-sucesso">✅ <?= htmlspecialchars($sucesso) ?></div>
<?php endif; ?>
<?php if ($erro): ?>
    <div class="alerta alerta-erro">⚠️ <?= htmlspecialchars($erro) ?></div>
<?php endif; ?>

<!-- ── FORMULÁRIO DE CRIAÇÃO DE ATIVIDADE (admin, inicialmente oculto) ──────── -->
<?php if (eh_administrador()): ?>
<div id="form-nova-atividade" class="card mb-md d-none">
    <div class="card-titulo">➕ Nova Atividade</div>

    <!--
        Os campos mapeiam ao AtividadeRequest em AtividadeController.java:
        { nome, eventoId, local{nome}, horaInicio (HH:MM:SS), horaFim, data, tipo, vagas, ... }
    -->
    <form method="POST" action="atividades.php?evento_id=<?= $idEvento ?>" id="form-criar-atividade">
        <input type="hidden" name="acao" value="criar_atividade">

        <div class="form-linha">
            <div class="form-grupo">
                <label for="nome">Nome da Atividade *</label>
                <input type="text" id="nome" name="nome" placeholder="Ex: Palestra de OO" required>
            </div>
            <div class="form-grupo">
                <label for="local_nome">Local</label>
                <input type="text" id="local_nome" name="local_nome" placeholder="Ex: Sala A101">
            </div>
        </div>

        <div class="form-linha-3">
            <div class="form-grupo">
                <label for="data">Data *</label>
                <input type="date" id="data" name="data" required>
            </div>
            <div class="form-grupo">
                <label for="hora_inicio">Início *</label>
                <input type="time" id="hora_inicio" name="hora_inicio" required>
            </div>
            <div class="form-grupo">
                <label for="hora_fim">Fim *</label>
                <input type="time" id="hora_fim" name="hora_fim" required>
            </div>
        </div>

        <div class="form-linha">
            <div class="form-grupo">
                <label for="tipo">Tipo</label>
                <select id="tipo" name="tipo">
                    <option value="Palestra">Palestra</option>
                    <option value="Workshop">Workshop</option>
                    <option value="Mesa Redonda">Mesa Redonda</option>
                    <option value="Minicurso">Minicurso</option>
                    <option value="Apresentação">Apresentação</option>
                </select>
            </div>
            <div class="form-grupo">
                <label for="vagas">Vagas (0 = ilimitado)</label>
                <input type="number" id="vagas" name="vagas" min="0" value="0">
            </div>
        </div>

        <button type="submit" class="btn btn-sucesso" id="btn-salvar-atividade">
            ✅ Salvar Atividade
        </button>
    </form>
</div>
<?php endif; ?>

<!-- ── TABELA DE ATIVIDADES ───────────────────────────────────────────────────── -->
<?php if ($erroListagem): ?>
    <div class="alerta alerta-erro">⚠️ <?= htmlspecialchars($erroListagem) ?></div>
<?php elseif (empty($atividades)): ?>
    <div class="card text-center" style="padding: 2rem;">
        <p class="text-muted">Nenhuma atividade cadastrada para este evento.</p>
    </div>
<?php else: ?>
    <div class="card">
        <div class="tabela-responsiva">
            <table class="tabela" id="tabela-atividades">
                <thead>
                    <tr>
                        <th>Atividade</th>
                        <th>Data</th>
                        <th>Horário</th>
                        <th>Tipo</th>
                        <th>Local</th>
                        <th>Vagas</th>
                        <th>Ação</th>
                    </tr>
                </thead>
                <tbody>
                    <?php foreach ($atividades as $atividade):
                        $idAtiv    = $atividade['id']         ?? '—';
                        $nomeAtiv  = $atividade['nome']       ?? '—';
                        $dataAtiv  = $atividade['data']       ?? '—';
                        $hInicio   = $atividade['horaInicio'] ?? '—';
                        $hFim      = $atividade['horaFim']    ?? '—';
                        $tipoAtiv  = $atividade['tipo']       ?? '—';
                        $localAtiv = $atividade['local']['nome'] ?? '—';
                        $vagas     = $atividade['vagas']      ?? 0;
                        $controlVagas = $atividade['controlaVagas'] ?? false;
                    ?>
                        <tr id="atividade-<?= (int)$idAtiv ?>">
                            <td><strong><?= htmlspecialchars($nomeAtiv) ?></strong></td>
                            <td><?= htmlspecialchars($dataAtiv) ?></td>
                            <td><?= htmlspecialchars($hInicio) ?> – <?= htmlspecialchars($hFim) ?></td>
                            <td><?= htmlspecialchars($tipoAtiv) ?></td>
                            <td><?= htmlspecialchars($localAtiv) ?></td>
                            <td>
                                <?php if ($controlVagas): ?>
                                    <?= (int)$vagas ?>
                                <?php else: ?>
                                    <span class="text-muted">Ilimitado</span>
                                <?php endif; ?>
                            </td>
                            <td>
                                <?php if (usuario_logado()): ?>
                                    <!--
                                        Formulário de inscrição: envia POST /inscricoes
                                        com { idAtividade, idUsuario }
                                    -->
                                    <form method="POST"
                                          action="atividades.php?evento_id=<?= $idEvento ?>"
                                          id="form-inscrever-<?= (int)$idAtiv ?>"
                                          style="display:inline;">
                                        <input type="hidden" name="acao" value="inscrever">
                                        <input type="hidden" name="id_atividade" value="<?= (int)$idAtiv ?>">
                                        <button type="submit"
                                                class="btn btn-sucesso"
                                                id="btn-inscrever-<?= (int)$idAtiv ?>"
                                                style="font-size:0.82rem; padding:0.3rem 0.8rem;">
                                            Inscrever-se
                                        </button>
                                    </form>
                                <?php else: ?>
                                    <a href="login.php" class="btn btn-secundario"
                                       style="font-size:0.82rem; padding:0.3rem 0.8rem;">
                                        Login para inscrever
                                    </a>
                                <?php endif; ?>
                            </td>
                        </tr>
                    <?php endforeach; ?>
                </tbody>
            </table>
        </div>
    </div>
<?php endif; ?>

<?php
$conteudo    = ob_get_clean();
$titulo      = 'Atividades do Evento';
$paginaAtiva = 'eventos';
include __DIR__ . '/layout.php';
