<?php
/**
 * layout.php — Template de layout compartilhado
 * ─────────────────────────────────────────────────────────────────────────────
 * Este arquivo é incluído pelas páginas para montar o HTML completo com
 * navbar e rodapé. As páginas definem $titulo e $paginaAtiva antes de incluí-lo.
 *
 * Uso:
 *   $titulo = 'Eventos'; $paginaAtiva = 'eventos';
 *   ob_start(); // captura o conteúdo da página
 *   ... HTML do conteúdo ...
 *   $conteudo = ob_get_clean();
 *   include 'layout.php';
 * ─────────────────────────────────────────────────────────────────────────────
 */
require_once __DIR__ . '/api_service.php';
iniciar_sessao();

$usuario = usuario_logado();

// Página ativa para destacar o link correto na navbar
$paginaAtiva = $paginaAtiva ?? '';
$titulo      = $titulo ?? 'Sistema de Eventos';
?>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><?= htmlspecialchars($titulo) ?> — Gestão de Eventos UEG</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>

<!-- ══ NAVBAR ══════════════════════════════════════════════════════════════ -->
<nav class="navbar">
    <a href="index.php" class="navbar-marca">
        📅 Eventos <span>UEG</span>
    </a>

    <ul class="navbar-links">
        <li>
            <a href="eventos.php"
               class="<?= $paginaAtiva === 'eventos' ? 'ativo' : '' ?>">
                Eventos
            </a>
        </li>

        <?php if ($usuario): ?>
            <li>
                <a href="minhas_inscricoes.php"
                   class="<?= $paginaAtiva === 'inscricoes' ? 'ativo' : '' ?>">
                    Minhas Inscrições
                </a>
            </li>
            <?php if (eh_administrador()): ?>
                <li>
                    <a href="criar_evento.php"
                       class="<?= $paginaAtiva === 'criar_evento' ? 'ativo' : '' ?>">
                        + Criar Evento
                    </a>
                </li>
                <li>
                    <a href="usuarios.php"
                       class="<?= $paginaAtiva === 'usuarios' ? 'ativo' : '' ?>">
                        Usuários
                    </a>
                </li>
            <?php endif; ?>
        <?php endif; ?>
    </ul>

    <div class="navbar-usuario">
        <?php if ($usuario): ?>
            Olá, <strong><?= htmlspecialchars($usuario['nome']) ?></strong>
            &nbsp;|&nbsp;
            <a href="logout.php" class="btn btn-secundario" style="font-size:0.82rem; padding:0.3rem 0.7rem;">
                Sair
            </a>
        <?php else: ?>
            <a href="login.php" class="btn btn-primario" style="font-size:0.82rem; padding:0.3rem 0.9rem;">
                Entrar
            </a>
        <?php endif; ?>
    </div>
</nav>

<!-- ══ CONTEÚDO PRINCIPAL ═══════════════════════════════════════════════════ -->
<main class="conteudo-principal">
    <div class="container">
        <?= $conteudo ?? '' ?>
    </div>
</main>

<!-- ══ RODAPÉ ═══════════════════════════════════════════════════════════════ */
<footer class="rodape">
    Sistema de Gestão de Eventos · UEG POO2 &copy; <?= date('Y') ?>
</footer>

</body>
</html>
