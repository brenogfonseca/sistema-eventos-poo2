<?php
require_once __DIR__ . '/api_service.php';
iniciar_sessao();

$idEvento = (int)($_GET['evento_id'] ?? 0);
if ($idEvento <= 0) { header('Location: eventos.php'); exit; }

$idUsuario = id_usuario_logado();
$feedbackAtividade = $_SESSION['atividade_feedback'] ?? null;
unset($_SESSION['atividade_feedback']);
$erro = '';
$sucesso = '';
$resEvento = chamar_api('GET', "/eventos/{$idEvento}", null, headers_autenticados());
$evento = $resEvento['erro'] ? null : ($resEvento['corpo'] ?? null);
if (!$evento) { $erro = $resEvento['msg_erro'] ?? 'Evento não encontrado.'; }
$organizadores = $evento['organizadoresIds'] ?? [];
$podeGerenciar = eh_administrador() || ($idUsuario !== null && in_array($idUsuario, $organizadores));
$eventoEncerrado = false;
try {
    $fusoEvento = new DateTimeZone($evento['fusoHorario'] ?? 'America/Sao_Paulo');
    $eventoEncerrado = new DateTimeImmutable($evento['fim'] ?? 'now', $fusoEvento)
        <= new DateTimeImmutable('now', $fusoEvento);
} catch (Throwable $ignorado) { }

if (isset($_GET['certificado_pessoa'])) {
    exigir_login();
    $idPessoaCertificado = filter_input(INPUT_GET, 'certificado_pessoa', FILTER_VALIDATE_INT) ?: 0;
    $papelCertificado = strtoupper(trim($_GET['papel'] ?? ''));
    if ($idPessoaCertificado > 0 && in_array($papelCertificado, ['PALESTRANTE', 'APRESENTADOR'], true)) {
        $pdf = chamar_api('GET', "/eventos/{$idEvento}/pessoas-programacao/{$idPessoaCertificado}/certificado.pdf?papel={$papelCertificado}",
            null, headers_autenticados());
        if (!$pdf['erro'] && is_string($pdf['corpo'])) {
            header('Content-Type: application/pdf');
            header('Content-Disposition: attachment; filename=certificado-evento-' . $idEvento . '-pessoa-' . $idPessoaCertificado . '.pdf');
            header('X-Content-Type-Options: nosniff');
            echo $pdf['corpo'];
            exit;
        }
        $erro = $pdf['msg_erro'] ?: 'Não foi possível emitir o certificado da programação.';
    }
}
$locais = [];
if ($podeGerenciar) {
    $locaisResp = chamar_api('GET', '/locais', null, headers_autenticados());
    if (!$locaisResp['erro']) $locais = $locaisResp['corpo'] ?? [];
}

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
            'tipo' => trim($_POST['tipo'] ?? 'Palestra'),
            'vagas' => $vagas,
            'controlaVagas' => $vagas > 0,
            'tipoFrequencia' => $_POST['tipoFrequencia'] ?? 'CHECKIN',
            'metodoIdentificacaoCheckin' => $_POST['metodoIdentificacaoCheckin'] ?? 'QR',
            'trilha' => $nomeTrilha !== '' ? ['nome' => $nomeTrilha] : null,
        ];
        $resultado = chamar_api('POST', '/atividades', $dados, headers_autenticados());
        if ($resultado['erro']) $erro = $resultado['msg_erro'] ?: 'Erro ao criar atividade.';
        else {
            $_SESSION['atividade_feedback'] = [
                'sucesso' => 'Atividade criada com a política de frequência selecionada.',
                'avisos' => $resultado['corpo']['avisos'] ?? [],
            ];
            header('Location: atividades.php?evento_id=' . $idEvento);
            exit;
        }
    } elseif ($acao === 'vincular_pessoa' && $podeGerenciar) {
        $idAtividadePessoa = (int)($_POST['id_atividade'] ?? 0);
        $dadosPessoa = [
            'nome' => trim($_POST['pessoa_nome'] ?? ''),
            'organizacao' => trim($_POST['pessoa_organizacao'] ?? ''),
            'biografia' => trim($_POST['pessoa_biografia'] ?? ''),
            'fotoUrl' => trim($_POST['pessoa_foto'] ?? ''),
            'papel' => $_POST['pessoa_papel'] ?? 'PALESTRANTE',
        ];
        $resultado = chamar_api('POST', "/atividades/{$idAtividadePessoa}/pessoas", $dadosPessoa, headers_autenticados());
        if ($resultado['erro']) $erro = $resultado['msg_erro'] ?: 'Não foi possível vincular a pessoa.';
        else $sucesso = 'Pessoa vinculada à programação.';
    } elseif ($acao === 'excluir_atividade' && $podeGerenciar) {
        $idAtividadeExcluir = (int)($_POST['id_atividade'] ?? 0);
        $resultado = chamar_api('DELETE', "/atividades/{$idAtividadeExcluir}", null, headers_autenticados());
        if ($resultado['erro']) $erro = $resultado['msg_erro'] ?: 'Não foi possível excluir a atividade.';
        else $sucesso = 'Atividade excluída.';
    }
}

$atividadesResp = chamar_api('GET', "/atividades/evento/{$idEvento}", null, headers_autenticados());
$atividades = $atividadesResp['erro'] ? [] : ($atividadesResp['corpo'] ?? []);
$pessoasResp = chamar_api('GET', "/eventos/{$idEvento}/pessoas-programacao");
$pessoasPorAtividade = [];
if (!$pessoasResp['erro']) {
    foreach (($pessoasResp['corpo'] ?? []) as $vinculo) {
        $pessoasPorAtividade[(int)($vinculo['idAtividade'] ?? 0)][] = $vinculo;
    }
}
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
<?php if ($feedbackAtividade): ?>
    <?php if (!empty($feedbackAtividade['sucesso'])): ?><div class="alerta alerta-sucesso"><?= htmlspecialchars($feedbackAtividade['sucesso']) ?></div><?php endif; ?>
    <?php foreach (($feedbackAtividade['avisos'] ?? []) as $aviso): ?><div class="alerta alerta-aviso" role="status"><?= htmlspecialchars($aviso) ?></div><?php endforeach; ?>
<?php endif; ?>

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

<?php if ($podeGerenciar && $atividades): ?>
<details class="card mb-md">
    <summary><strong>Adicionar pessoa à programação</strong></summary>
    <form method="POST" class="mt-md">
        <input type="hidden" name="acao" value="vincular_pessoa">
        <div class="form-linha">
            <div class="form-grupo"><label>Atividade</label><select name="id_atividade" required><?php foreach ($atividades as $atividade): ?><option value="<?= (int)$atividade['id'] ?>"><?= htmlspecialchars($atividade['nome'] ?? 'Atividade') ?></option><?php endforeach; ?></select></div>
            <div class="form-grupo"><label>Nome</label><input name="pessoa_nome" required maxlength="160"></div>
            <div class="form-grupo"><label>Papel</label><select name="pessoa_papel"><option value="PALESTRANTE">Palestrante</option><option value="APRESENTADOR">Apresentador</option><option value="RESPONSAVEL">Responsável</option></select></div>
        </div>
        <div class="form-linha">
            <div class="form-grupo"><label>Organização</label><input name="pessoa_organizacao" maxlength="160"></div>
            <div class="form-grupo"><label>Foto (URL)</label><input type="url" name="pessoa_foto" maxlength="500"></div>
        </div>
        <div class="form-grupo"><label>Biografia</label><textarea name="pessoa_biografia" rows="3" maxlength="2000"></textarea></div>
        <button class="btn btn-secundario" type="submit">Adicionar à programação</button>
    </form>
</details>
<?php endif; ?>

<?php if ($podeGerenciar): ?>
<div id="form-nova-atividade" class="card mb-md d-none">
    <h2 class="card-titulo">Criar atividade</h2>
    <form method="POST" action="atividades.php?evento_id=<?= $idEvento ?>">
        <input type="hidden" name="acao" value="criar_atividade">
        <div class="form-linha">
            <div class="form-grupo"><label>Nome *</label><input name="nome" required></div>
        </div>
        <div class="form-grupo"><label>Descrição</label><textarea name="descricao" rows="3" maxlength="4000"></textarea></div>
        <div class="form-linha-3">
            <div class="form-grupo"><label>Data *</label><input type="date" name="data" required></div>
            <div class="form-grupo"><label>Início *</label><input type="time" name="hora_inicio" required></div>
            <div class="form-grupo"><label>Fim *</label><input type="time" name="hora_fim" required></div>
        </div>
        <div class="form-linha">
            <div class="form-grupo"><label>Trilha</label><input name="trilha_nome" placeholder="Ex.: Desenvolvimento Web"></div>
            <div class="form-grupo"><label>Local cadastrado</label><select name="local_id"><option value="0">A definir</option><?php foreach ($locais as $local): ?><option value="<?= (int)$local['id'] ?>"><?= htmlspecialchars($local['nome'] ?? '') ?></option><?php endforeach; ?></select><?php if (!$locais): ?><small class="text-muted">Um administrador precisa cadastrar locais antes de associá-los.</small><?php endif; ?></div>
        </div>
        <div class="form-linha">
            <div class="form-grupo"><label>Categoria/tipo</label>
                <select name="tipo">
                    <?php foreach (['Palestra', 'Minicurso', 'Oficina', 'Mesa-redonda', 'Apresentação', 'Outro'] as $tipo): ?>
                        <option value="<?= htmlspecialchars($tipo) ?>"><?= htmlspecialchars($tipo) ?></option>
                    <?php endforeach; ?>
                </select>
            </div>
            <div class="form-grupo"><label>Vagas da atividade (0 = ilimitado)</label><input type="number" name="vagas" min="0" value="0"></div>
            <div class="form-grupo"><label>Critério de presença *</label>
                <select name="tipoFrequencia" required>
                    <option value="CHECKIN">Check-in único por QR</option>
                    <option value="ENTRADA_SAIDA">Entrada e saída por QR</option>
                    <option value="MANUAL">Validação manual pelo organizador</option>
                </select>
            </div>
            <div class="form-grupo"><label>Identificação no check-in *</label>
                <select name="metodoIdentificacaoCheckin" required>
                    <option value="QR">QR Code</option>
                    <option value="NOME_CPF">Busca por nome ou CPF</option>
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
    <div class="card mb-md">
        <h2>Filtrar programação</h2>
        <div class="form-linha-3">
            <div class="form-grupo"><label for="filtro-data">Data</label><input type="date" id="filtro-data"></div>
            <div class="form-grupo"><label for="filtro-tipo">Categoria</label><input type="search" id="filtro-tipo" placeholder="Palestra, oficina..."></div>
            <div class="form-grupo"><label for="filtro-trilha">Trilha</label><input type="search" id="filtro-trilha" placeholder="Nome da trilha"></div>
        </div>
        <div class="form-linha">
            <div class="form-grupo"><label for="filtro-local">Local</label><input type="search" id="filtro-local" placeholder="Sala ou endereço"></div>
            <div class="form-grupo"><label for="filtro-horario">Horário a partir de</label><input type="time" id="filtro-horario"></div>
        </div>
        <p id="resultado-filtros" class="text-muted" aria-live="polite"></p>
    </div>
    <?php if ($membroAtivo): ?>
    <form method="POST" action="atividades.php?evento_id=<?= $idEvento ?>" class="card">
        <input type="hidden" name="acao" value="salvar_atividades">
        <input type="hidden" name="id_inscricao_evento" value="<?= (int)$inscricaoEvento['id'] ?>">
        <div class="tabela-responsiva"><table class="tabela">
        <thead><tr><th>Escolher</th><th>Atividade</th><th>Categoria</th><th>Trilha</th><th>Local</th><th>Data</th><th>Horário</th><th>Presença</th><th>Programação</th><th>Avaliação</th><th>Vagas restantes</th><?php if ($podeGerenciar): ?><th>Ações</th><?php endif; ?></tr></thead>
            <tbody><?php foreach ($atividades as $atividade):
                $idAtividade = (int)($atividade['id'] ?? 0);
                $limite = !empty($atividade['controlaVagas']) && (int)($atividade['vagas'] ?? 0) <= 0;
                $tipoPresenca = $atividade['tipoFrequencia'] ?? 'CHECKIN';
                $nomeLocalLinha = $atividade['local']['nome'] ?? $atividade['localNome'] ?? '';
                $nomeTrilhaLinha = $atividade['trilha']['nome'] ?? '';
            ?>
                <tr class="linha-programacao" data-data="<?= htmlspecialchars($atividade['data'] ?? '') ?>" data-tipo="<?= htmlspecialchars(strtolower($atividade['tipo'] ?? '')) ?>" data-trilha="<?= htmlspecialchars(strtolower($nomeTrilhaLinha)) ?>" data-local="<?= htmlspecialchars(strtolower($nomeLocalLinha)) ?>" data-hora="<?= htmlspecialchars($atividade['horaInicio'] ?? '') ?>">
                    <td><input type="checkbox" name="ids_atividades[]" value="<?= $idAtividade ?>" <?= isset($selecionadas[$idAtividade]) ? 'checked' : '' ?> <?= $limite && !isset($selecionadas[$idAtividade]) ? 'disabled' : '' ?> aria-label="Selecionar atividade"></td>
                    <td><strong><?= htmlspecialchars($atividade['nome'] ?? 'Atividade') ?></strong><?php if (!empty($atividade['descricao'])): ?><br><small><?= nl2br(htmlspecialchars($atividade['descricao'])) ?></small><?php endif; ?></td>
                    <td><?= htmlspecialchars($atividade['tipo'] ?? '—') ?></td>
                    <td><?= htmlspecialchars($nomeTrilhaLinha ?: '—') ?></td>
                    <td><?= htmlspecialchars($nomeLocalLinha ?: '—') ?></td>
                    <td><?= htmlspecialchars($atividade['data'] ?? '—') ?></td>
                    <td><?= htmlspecialchars($atividade['horaInicio'] ?? '—') ?>–<?= htmlspecialchars($atividade['horaFim'] ?? '—') ?></td>
                    <td><?= htmlspecialchars($tipoPresenca) ?></td>
            <td><?php foreach (($pessoasPorAtividade[$idAtividade] ?? []) as $vinculo): $pessoa = $vinculo['pessoa'] ?? []; $papelVinculo = $vinculo['papel'] ?? ''; ?><div><?= htmlspecialchars($pessoa['nome'] ?? '') ?> <small>(<?= htmlspecialchars($papelVinculo) ?>)</small><?php if ($podeGerenciar && $eventoEncerrado && !empty($evento['certificado']) && in_array($papelVinculo, ['PALESTRANTE', 'APRESENTADOR'], true) && !empty($pessoa['id'])): ?> — <a href="atividades.php?evento_id=<?= $idEvento ?>&certificado_pessoa=<?= (int)$pessoa['id'] ?>&papel=<?= rawurlencode($papelVinculo) ?>">Baixar certificado</a><?php endif; ?></div><?php endforeach; ?></td>
                    <td><a href="avaliacoes.php?atividade_id=<?= $idAtividade ?>">Abrir</a></td>
                    <td><?= !empty($atividade['controlaVagas']) ? (int)($atividade['vagas'] ?? 0) : 'Ilimitadas' ?></td>
                    <?php if ($podeGerenciar): ?><td><a href="editar_atividade.php?id=<?= $idAtividade ?>">Editar</a> · <a href="colaboradores.php?atividade_id=<?= $idAtividade ?>">Colaboradores</a> · <a href="presencas.php?atividade_id=<?= $idAtividade ?>">Presenças</a><form method="POST" class="inline-form" onsubmit="return confirm('Excluir esta atividade? Só será possível se ainda não houver inscrições.');"><input type="hidden" name="acao" value="excluir_atividade"><input type="hidden" name="id_atividade" value="<?= $idAtividade ?>"><button class="btn btn-perigo" type="submit">Excluir</button></form></td><?php endif; ?>
                </tr>
            <?php endforeach; ?></tbody>
        </table></div>
        <p class="text-muted mt-sm">Participantes não podem escolher atividades com horários sobrepostos. O sistema avisa sobre colisões de sala, mas permite salvar para o organizador avaliar.</p>
        <button class="btn btn-primario mt-sm" type="submit">Salvar minha agenda</button>
    </form>
    <?php else: ?>
    <div class="card"><div class="tabela-responsiva"><table class="tabela">
        <thead><tr><th>Atividade</th><th>Categoria</th><th>Trilha</th><th>Local</th><th>Data</th><th>Horário</th><th>Presença</th><th>Programação</th><th>Avaliação</th><th>Vagas</th><?php if ($podeGerenciar): ?><th>Ações</th><?php endif; ?></tr></thead>
        <tbody><?php foreach ($atividades as $atividade): $nomeLocalLinha = $atividade['local']['nome'] ?? $atividade['localNome'] ?? ''; $nomeTrilhaLinha = $atividade['trilha']['nome'] ?? ''; ?><tr class="linha-programacao" data-data="<?= htmlspecialchars($atividade['data'] ?? '') ?>" data-tipo="<?= htmlspecialchars(strtolower($atividade['tipo'] ?? '')) ?>" data-trilha="<?= htmlspecialchars(strtolower($nomeTrilhaLinha)) ?>" data-local="<?= htmlspecialchars(strtolower($nomeLocalLinha)) ?>" data-hora="<?= htmlspecialchars($atividade['horaInicio'] ?? '') ?>">
            <td><?= htmlspecialchars($atividade['nome'] ?? '—') ?><?php if (!empty($atividade['descricao'])): ?><br><small><?= nl2br(htmlspecialchars($atividade['descricao'])) ?></small><?php endif; ?></td><td><?= htmlspecialchars($atividade['tipo'] ?? '—') ?></td><td><?= htmlspecialchars($nomeTrilhaLinha ?: '—') ?></td><td><?= htmlspecialchars($nomeLocalLinha ?: '—') ?></td><td><?= htmlspecialchars($atividade['data'] ?? '—') ?></td>
            <td><?= htmlspecialchars($atividade['horaInicio'] ?? '—') ?>–<?= htmlspecialchars($atividade['horaFim'] ?? '—') ?></td>
            <td><?= htmlspecialchars($atividade['tipoFrequencia'] ?? 'CHECKIN') ?></td>
            <td><?php foreach (($pessoasPorAtividade[(int)($atividade['id'] ?? 0)] ?? []) as $vinculo): $pessoa = $vinculo['pessoa'] ?? []; $papelVinculo = $vinculo['papel'] ?? ''; ?><div><?= htmlspecialchars($pessoa['nome'] ?? '') ?> <small>(<?= htmlspecialchars($papelVinculo) ?>)</small><?php if ($podeGerenciar && $eventoEncerrado && !empty($evento['certificado']) && in_array($papelVinculo, ['PALESTRANTE', 'APRESENTADOR'], true) && !empty($pessoa['id'])): ?> — <a href="atividades.php?evento_id=<?= $idEvento ?>&certificado_pessoa=<?= (int)$pessoa['id'] ?>&papel=<?= rawurlencode($papelVinculo) ?>">Baixar certificado</a><?php endif; ?></div><?php endforeach; ?></td>
            <td><a href="avaliacoes.php?atividade_id=<?= (int)($atividade['id'] ?? 0) ?>">Abrir</a></td>
            <td><?= !empty($atividade['controlaVagas']) ? (int)($atividade['vagas'] ?? 0) : 'Ilimitadas' ?></td>
            <?php if ($podeGerenciar): ?><td><a href="editar_atividade.php?id=<?= (int)($atividade['id'] ?? 0) ?>">Editar</a> · <a href="colaboradores.php?atividade_id=<?= (int)($atividade['id'] ?? 0) ?>">Colaboradores</a> · <a href="presencas.php?atividade_id=<?= (int)($atividade['id'] ?? 0) ?>">Presenças</a><form method="POST" class="inline-form" onsubmit="return confirm('Excluir esta atividade? Só será possível se ainda não houver inscrições.');"><input type="hidden" name="acao" value="excluir_atividade"><input type="hidden" name="id_atividade" value="<?= (int)($atividade['id'] ?? 0) ?>"><button class="btn btn-perigo" type="submit">Excluir</button></form></td><?php endif; ?>
        </tr><?php endforeach; ?></tbody>
    </table></div></div>
    <?php endif; ?>
<?php endif; ?>

<script>
(() => {
    const filtros = {
        data: document.getElementById('filtro-data'),
        tipo: document.getElementById('filtro-tipo'),
        trilha: document.getElementById('filtro-trilha'),
        local: document.getElementById('filtro-local'),
        horario: document.getElementById('filtro-horario')
    };
    const linhas = [...document.querySelectorAll('.linha-programacao')];
    const resultado = document.getElementById('resultado-filtros');
    function aplicar() {
        let visiveis = 0;
        for (const linha of linhas) {
            const corresponde = (!filtros.data?.value || linha.dataset.data === filtros.data.value)
                && (!filtros.tipo?.value || linha.dataset.tipo.includes(filtros.tipo.value.toLocaleLowerCase()))
                && (!filtros.trilha?.value || linha.dataset.trilha.includes(filtros.trilha.value.toLocaleLowerCase()))
                && (!filtros.local?.value || linha.dataset.local.includes(filtros.local.value.toLocaleLowerCase()))
                && (!filtros.horario?.value || linha.dataset.hora >= filtros.horario.value);
            linha.hidden = !corresponde;
            if (corresponde) visiveis++;
        }
        if (resultado) resultado.textContent = `${visiveis} atividade(s) na programação filtrada.`;
    }
    Object.values(filtros).forEach(campo => campo?.addEventListener('input', aplicar));
})();
</script>

<?php
$conteudo = ob_get_clean();
$titulo = 'Atividades do Evento';
$paginaAtiva = 'eventos';
include __DIR__ . '/layout.php';
