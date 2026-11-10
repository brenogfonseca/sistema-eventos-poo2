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

/**
 * Formata data vinda da API (seja string 'YYYY-MM-DD' ou array Jackson [YYYY, MM, DD])
 */
function formatar_data_exibicao($data): string {
    if (is_array($data)) {
        if (count($data) >= 3) {
            return sprintf('%02d/%02d/%04d', $data[2], $data[1], $data[0]);
        }
        return implode('/', $data);
    }
    if (is_string($data) && !empty($data)) {
        $partes = explode('-', $data);
        if (count($partes) === 3) {
            return "{$partes[2]}/{$partes[1]}/{$partes[0]}";
        }
        return $data;
    }
    return '—';
}

/**
 * Formata horário vindo da API (seja string 'HH:MM:SS' ou array Jackson [HH, MM, ...])
 */
function formatar_hora_exibicao($hora): string {
    if (is_array($hora)) {
        if (count($hora) >= 2) {
            return sprintf('%02d:%02d', $hora[0], $hora[1]);
        }
        return implode(':', $hora);
    }
    if (is_string($hora) && !empty($hora)) {
        return substr($hora, 0, 5);
    }
    return '—';
}

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
        $msgErro = '';
        if (is_array($resultado['corpo']) && !empty($resultado['corpo']['erro'])) {
            $msgErro = (string)$resultado['corpo']['erro'];
        } elseif (!empty($resultado['msg_erro'])) {
            $msgErro = (string)$resultado['msg_erro'];
        }

        if (empty($msgErro) || preg_match('/(br\.ueg\.|java\.|exception|cannot construct|jackson|stacktrace|sql)/i', $msgErro)) {
            $msgErro = 'inconsistência nos dados da inscrição.';
        }

        $erro = "Atenção: Não foi possível realizar a inscrição devido a {$msgErro}";
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

        // Garante formato HH:MM:SS esperado pela API Javalin (LocalTime)
        $horaInicioFormatada = $horaInicio;
        if (!empty($horaInicioFormatada) && strlen($horaInicioFormatada) === 5) {
            $horaInicioFormatada .= ':00';
        }

        $horaFimFormatada = $horaFim;
        if (!empty($horaFimFormatada) && strlen($horaFimFormatada) === 5) {
            $horaFimFormatada .= ':00';
        }

        // AtividadeRequest.java usa LocalTime (HH:MM:SS) e LocalDate (yyyy-MM-dd)
        $dados = [
            'nome'          => $nome,
            'eventoId'      => $idEvento,
            'nomeLocal'     => $localNome,
            'local'         => !empty($localNome) ? ['nome' => $localNome] : null,
            'horaInicio'    => $horaInicioFormatada,
            'horaFim'       => $horaFimFormatada,
            'data'          => $data,
            'tipo'          => $tipo,
            'vagas'         => $vagas,
            'controlaVagas' => $controlaVagas,
            'tipoFrequencia'=> 'CHECKIN',
            'trilha'        => null,
        ];

        // Passa o header X-Usuario-Id pois o admin precisa estar autenticado
        $resultado = chamar_api('POST', '/atividades', $dados, headers_autenticados());

        if ($resultado['erro']) {
            $msgErro = '';
            if (is_array($resultado['corpo']) && !empty($resultado['corpo']['erro'])) {
                $msgErro = (string)$resultado['corpo']['erro'];
            } elseif (!empty($resultado['msg_erro'])) {
                $msgErro = (string)$resultado['msg_erro'];
            }

            // Remove vestígios técnicos de pacotes Java ou stacktraces da visão do usuário final
            if (preg_match('/(br\.ueg\.|java\.|exception|cannot construct|jackson|stacktrace)/i', $msgErro)) {
                $erro = "Atenção: Não foi possível salvar a atividade devido a uma inconsistência nos dados informados.";
            } else {
                $erro = "Atenção: Não foi possível salvar a atividade devido a " . ($msgErro ?: 'um erro inesperado.');
            }
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

$mostrarFormCriacao = !empty($erro) && isset($_POST['acao']) && $_POST['acao'] === 'criar_atividade';

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

<!-- ── FORMULÁRIO DE CRIAÇÃO DE ATIVIDADE (admin, visível se houve erro ou ao clicar no botão) ──────── -->
<?php if (eh_administrador()): ?>
<div id="form-nova-atividade" class="card mb-md <?= $mostrarFormCriacao ? '' : 'd-none' ?>">
    <div class="card-titulo">➕ Nova Atividade</div>

    <!--
        Os campos mapeiam ao AtividadeRequest em AtividadeController.java:
        { nome, eventoId, nomeLocal, local{nome}, horaInicio (HH:MM:SS), horaFim, data, tipo, vagas, ... }
    -->
    <form method="POST" action="atividades.php?evento_id=<?= $idEvento ?>" id="form-criar-atividade">
        <input type="hidden" name="acao" value="criar_atividade">

        <div class="form-linha">
            <div class="form-grupo">
                <label for="nome">Nome da Atividade *</label>
                <input type="text" id="nome" name="nome" placeholder="Ex: Palestra de OO"
                       value="<?= htmlspecialchars($mostrarFormCriacao ? ($_POST['nome'] ?? '') : '') ?>" required>
            </div>
            <div class="form-grupo">
                <label for="local_nome">Local</label>
                <input type="text" id="local_nome" name="local_nome" placeholder="Ex: Sala A101"
                       value="<?= htmlspecialchars($mostrarFormCriacao ? ($_POST['local_nome'] ?? '') : '') ?>">
            </div>
        </div>

        <div class="form-linha-3">
            <div class="form-grupo">
                <label for="data">Data *</label>
                <input type="date" id="data" name="data"
                       value="<?= htmlspecialchars($mostrarFormCriacao ? ($_POST['data'] ?? '') : '') ?>" required>
            </div>
            <div class="form-grupo">
                <label for="hora_inicio">Início *</label>
                <input type="time" id="hora_inicio" name="hora_inicio"
                       value="<?= htmlspecialchars($mostrarFormCriacao ? ($_POST['hora_inicio'] ?? '') : '') ?>" required>
            </div>
            <div class="form-grupo">
                <label for="hora_fim">Fim *</label>
                <input type="time" id="hora_fim" name="hora_fim"
                       value="<?= htmlspecialchars($mostrarFormCriacao ? ($_POST['hora_fim'] ?? '') : '') ?>" required>
            </div>
        </div>

        <div class="form-linha">
            <div class="form-grupo">
                <label for="tipo">Tipo</label>
                <select id="tipo" name="tipo">
                    <?php
                    $tipoSelecionado = $mostrarFormCriacao ? ($_POST['tipo'] ?? 'Palestra') : 'Palestra';
                    $tipos = ['Palestra', 'Workshop', 'Mesa Redonda', 'Minicurso', 'Apresentação'];
                    foreach ($tipos as $t):
                    ?>
                        <option value="<?= $t ?>" <?= $tipoSelecionado === $t ? 'selected' : '' ?>><?= $t ?></option>
                    <?php endforeach; ?>
                </select>
            </div>
            <div class="form-grupo">
                <label for="vagas">Vagas (0 = ilimitado)</label>
                <input type="number" id="vagas" name="vagas" min="0"
                       value="<?= htmlspecialchars($mostrarFormCriacao ? ($_POST['vagas'] ?? '0') : '0') ?>">
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
                        $nomeAtiv  = is_string($atividade['nome'] ?? null) ? $atividade['nome'] : '—';
                        $dataAtiv  = formatar_data_exibicao($atividade['data'] ?? '');
                        $hInicio   = formatar_hora_exibicao($atividade['horaInicio'] ?? '');
                        $hFim      = formatar_hora_exibicao($atividade['horaFim'] ?? '');
                        $tipoAtiv  = is_string($atividade['tipo'] ?? null) ? $atividade['tipo'] : '—';
                        
                        $localAtiv = '—';
                        if (isset($atividade['local'])) {
                            if (is_array($atividade['local']) && !empty($atividade['local']['nome'])) {
                                $localAtiv = (string)$atividade['local']['nome'];
                            } elseif (is_string($atividade['local'])) {
                                $localAtiv = $atividade['local'];
                            }
                        } elseif (!empty($atividade['localNome'])) {
                            $localAtiv = (string)$atividade['localNome'];
                        }

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
