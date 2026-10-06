<?php
/**
 * cadastro.php — Módulo de Usuários: Formulário de Cadastro
 * ─────────────────────────────────────────────────────────────────────────────
 * FLUXO DIDÁTICO:
 *   1. O formulário HTML coleta: nome, email e senha.
 *   2. Ao submeter via POST, este PHP captura os campos de $_POST.
 *   3. Monta um array PHP com os dados, fixando o perfil como USUARIO.
 *   4. Chama chamar_api('POST', '/usuarios', $dados):
 *      a. A função converte o array para JSON.
 *      b. Envia via cURL com método POST e Content-Type: application/json.
 *      c. A API Java recebe, valida e persiste o novo usuário.
 *   5. Se a API retornar 201 (Created), exibe sucesso e redireciona para login.
 *   6. Se retornar erro, exibe a mensagem de erro da API.
 *
 * REGRA DE NEGÓCIO — Perfis:
 *   - VISITANTE    = qualquer pessoa que acessa o site SEM conta (anônimo).
 *                    Não precisa se cadastrar. Não tem registro no banco.
 *   - USUARIO      = qualquer pessoa que SE CADASTRA. É o perfil padrão/automático.
 *                    Pode se inscrever em atividades.
 *   - ADMINISTRADOR = promovido pelo admin após o cadastro via PATCH /perfil.
 *
 * Portanto: o formulário público de cadastro SEMPRE cria um USUARIO.
 * O usuário NÃO escolhe o próprio perfil.
 *
 * Endpoint utilizado:
 *   POST /usuarios
 *   Corpo: { "nome": "...", "email": "...", "senha": "...", "perfil": "USUARIO" }
 * ─────────────────────────────────────────────────────────────────────────────
 */
require_once __DIR__ . '/api_service.php';
iniciar_sessao();

// Se já está logado, redireciona
if (usuario_logado() !== null) {
    header('Location: eventos.php');
    exit;
}

$erro    = '';
$sucesso = '';

// ── Processa o formulário quando enviado via POST ─────────────────────────────
if ($_SERVER['REQUEST_METHOD'] === 'POST') {

    // 1. Captura os campos do formulário HTML
    $nome  = trim($_POST['nome']  ?? '');
    $email = trim($_POST['email'] ?? '');
    $senha = trim($_POST['senha'] ?? '');

    // Perfil é sempre USUARIO: qualquer pessoa que se cadastra é um USUARIO.
    // VISITANTE é o estado anônimo (sem conta). Não é escolhido no cadastro.
    // ADMINISTRADOR é promovido depois pelo admin via PATCH /usuarios/{id}/perfil.
    $perfil = 'USUARIO';

    // 2. Validação básica no front-end antes de chamar a API
    if (empty($nome) || empty($email) || empty($senha)) {
        $erro = 'Todos os campos são obrigatórios.';

    } elseif (strlen($senha) < 6) {
        $erro = 'A senha deve ter pelo menos 6 caracteres.';

    } else {
        // 3. Monta o array de dados a ser enviado como JSON para a API
        //    Os campos correspondem exatamente ao que CadastrarUsuarioRequest
        //    espera no UsuarioController.java
        $dados = [
            'nome'   => $nome,
            'email'  => $email,
            'senha'  => $senha,
            'perfil' => $perfil, // Sempre 'USUARIO' — perfil padrão de quem se cadastra
        ];

        // 4. Chama a API: POST /usuarios
        //    chamar_api() converte $dados para JSON e dispara via cURL
        $resultado = chamar_api('POST', '/usuarios', $dados);

        // 5. Verifica a resposta da API
        if ($resultado['erro']) {
            // A API retornou erro (ex: email já cadastrado, dados inválidos)
            $erro = $resultado['msg_erro'] ?: 'Erro ao cadastrar. Tente novamente.';
        } else {
            // Usuário criado com sucesso (HTTP 201)
            $sucesso = 'Conta criada com sucesso! Faça login para continuar.';
        }
    }
}
?>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cadastro — Gestão de Eventos UEG</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>

<div class="pagina-auth">
    <div class="card-auth" style="max-width: 480px;">

        <!-- Cabeçalho -->
        <div class="logo-auth">
            <h1>📅 Criar Conta</h1>
            <p>Junte-se ao Sistema de Eventos UEG</p>
        </div>

        <!-- Mensagem de sucesso -->
        <?php if ($sucesso): ?>
            <div class="alerta alerta-sucesso">
                ✅ <?= htmlspecialchars($sucesso) ?>
                <br>
                <a href="login.php"><strong>Clique aqui para fazer login</strong></a>
            </div>
        <?php endif; ?>

        <!-- Mensagem de erro -->
        <?php if ($erro): ?>
            <div class="alerta alerta-erro">
                ⚠️ <?= htmlspecialchars($erro) ?>
            </div>
        <?php endif; ?>

        <?php if (!$sucesso): ?>
        <!-- ── FORMULÁRIO DE CADASTRO ─────────────────────────────────────── -->
        <!--
            Os campos abaixo correspondem exatamente ao que a API Java espera:
            { "nome", "email", "senha", "perfil" }
        -->
        <form method="POST" action="cadastro.php" id="form-cadastro">

            <div class="form-grupo">
                <label for="nome">Nome completo</label>
                <input
                    type="text"
                    id="nome"
                    name="nome"
                    placeholder="Seu nome"
                    value="<?= htmlspecialchars($_POST['nome'] ?? '') ?>"
                    required
                    autocomplete="name"
                >
            </div>

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
                <label for="senha">Senha (mín. 6 caracteres)</label>
                <input
                    type="password"
                    id="senha"
                    name="senha"
                    placeholder="••••••••"
                    required
                    autocomplete="new-password"
                >
            </div>

            <!--
                PERFIL FIXADO AUTOMATICAMENTE COMO 'USUARIO'.
                Não é exibido nem escolhido pelo usuário no cadastro.
                - VISITANTE = acesso anônimo ao site (sem conta, sem formulário).
                - USUARIO   = qualquer pessoa registrada. Perfil automático.
                - ADMINISTRADOR = promovido pelo admin após o cadastro.
            -->

            <button type="submit" class="btn btn-primario btn-bloco" id="btn-cadastrar">
                Criar Conta
            </button>
        </form>
        <?php endif; ?>

        <!-- Link para login -->
        <div class="separador-auth">ou</div>
        <div class="text-center">
            <a href="login.php">Já tem conta? <strong>Faça login</strong></a>
        </div>

    </div>
</div>

</body>
</html>
