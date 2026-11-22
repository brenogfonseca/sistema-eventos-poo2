<?php
/**
 * login.php — Módulo de Usuários: Tela de Login
 * ─────────────────────────────────────────────────────────────────────────────
 * FLUXO DIDÁTICO:
 *   1. O formulário HTML coleta o e-mail digitado pelo usuário.
 *   2. Ao submeter, este PHP captura o campo via $_POST.
 * A API verifica a senha e retorna uma sessão bearer assinada e com expiração.
 * ─────────────────────────────────────────────────────────────────────────────
 */
require_once __DIR__ . '/api_service.php';
iniciar_sessao();

// Se o usuário já está logado, redireciona para a listagem de eventos
if (usuario_logado() !== null) {
    header('Location: eventos.php');
    exit;
}

$erro   = '';
$sucesso = '';

// ── Processa o formulário quando enviado via POST ─────────────────────────────
if ($_SERVER['REQUEST_METHOD'] === 'POST') {

    // 1. Captura os dados do formulário
    $email = trim($_POST['email'] ?? '');
    $senha = (string) ($_POST['senha'] ?? '');

    // 2. Validação básica no front-end antes de chamar a API
    if (empty($email) || empty($senha)) {
        $erro = 'Preencha o e-mail e a senha.';

    } else {
        // A senha é verificada pelo back-end; o hash nunca sai da API.
        $resultado = chamar_api('POST', '/auth/login', ['email' => $email, 'senha' => $senha]);

        if ($resultado['erro']) {
            // A API retornou erro (ex: 404 usuário não encontrado)
            $erro = 'E-mail ou senha incorretos.';

        } else {
            $usuario = $resultado['corpo'];
            if (isset($usuario['id'], $usuario['email'], $usuario['token'], $usuario['expiraEm'])) {
                session_regenerate_id(true);
                $_SESSION['usuario'] = [
                    'id'     => $usuario['id'],
                    'nome'   => $usuario['nome'],
                    'email'  => $usuario['email'],
                    'perfil' => $usuario['perfil'],
                    'token'  => $usuario['token'],
                    'expiraEm' => $usuario['expiraEm'],
                ];
                header('Location: eventos.php');
                exit;
            } else {
                $erro = 'Resposta inesperada da API.';
            }
        }
    }
}
?>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login — Gestão de Eventos UEG</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>

<!-- Página de login usa layout centralizado, sem navbar -->
<div class="pagina-auth">
    <div class="card-auth">

        <!-- Cabeçalho do card de autenticação -->
        <div class="logo-auth">
            <h1>📅 Eventos UEG</h1>
            <p>Sistema de Gestão de Eventos · POO2</p>
        </div>

        <!-- Mensagem de erro (se houver) -->
        <?php if ($erro): ?>
            <div class="alerta alerta-erro">
                ⚠️ <?= htmlspecialchars($erro) ?>
            </div>
        <?php endif; ?>

        <!-- ── FORMULÁRIO DE LOGIN ────────────────────────────────────────── -->
        <!--
            Ao clicar em "Entrar", este formulário envia os campos para este
            mesmo arquivo (action=""), onde o PHP faz o processamento acima.
        -->
        <form method="POST" action="login.php" id="form-login">

            <div class="form-grupo">
                <label for="email">E-mail</label>
                <input
                    type="email"
                    id="email"
                    name="email"
                    placeholder="seu@email.com"
                    value="<?= htmlspecialchars($_POST['email'] ?? '') ?>"
                    required
                    autocomplete="email"
                >
            </div>

            <div class="form-grupo">
                <label for="senha">Senha</label>
                <input
                    type="password"
                    id="senha"
                    name="senha"
                    placeholder="••••••••"
                    required
                    autocomplete="current-password"
                >
            </div>

            <button type="submit" class="btn btn-primario btn-bloco" id="btn-entrar">
                Entrar
            </button>
        </form>

        <!-- Link para cadastro -->
        <div class="separador-auth">ou</div>
        <div class="text-center">
            <a href="cadastro.php">Não tem conta? <strong>Cadastre-se aqui</strong></a>
        </div>

        <!-- Link para ver eventos sem logar -->
        <div class="text-center mt-sm">
            <a href="eventos.php" class="text-muted">Explorar eventos sem fazer login</a>
        </div>

    </div>
</div>

</body>
</html>
