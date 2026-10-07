<?php
/**
 * api_service.php
 * ─────────────────────────────────────────────────────────────────────────────
 * Camada de infraestrutura do front-end PHP.
 *
 * Responsabilidade única: encapsular toda a comunicação HTTP com a API
 * REST Java (Javalin) que roda em http://localhost:8080.
 *
 * Por que um arquivo separado?
 *   Mantém as páginas de interface limpas: elas apenas chamam funções aqui
 *   e exibem os dados retornados, sem misturar lógica de rede com HTML.
 *
 * Essa separação reflete, no front-end, o mesmo princípio da Arquitetura
 * Hexagonal usada no back-end: as "portas de saída" do front-end ficam
 * isoladas dos adaptadores de entrada (as páginas PHP).
 * ─────────────────────────────────────────────────────────────────────────────
 */

// ─── Configuração global da API ──────────────────────────────────────────────

/** URL base da API Java. Altere a porta se necessário. */
define('API_BASE_URL', 'http://localhost:8080');

// ─── Função principal: realiza qualquer requisição HTTP para a API ────────────

/**
 * Executa uma requisição HTTP para a API REST usando cURL.
 *
 * @param string      $metodo   Método HTTP: 'GET', 'POST', 'PUT', 'PATCH', 'DELETE'
 * @param string      $endpoint Caminho do endpoint, ex: '/usuarios' ou '/eventos/1'
 * @param array|null  $dados    Array associativo com os dados a enviar no corpo (JSON)
 * @param array       $headers  Headers HTTP adicionais (ex: ['X-Usuario-Id: 1'])
 *
 * @return array Associativo com chaves:
 *               - 'status'  (int)         : código HTTP da resposta (200, 201, 400…)
 *               - 'corpo'   (array|string): corpo da resposta decodificado (ou string bruta)
 *               - 'erro'    (bool)        : true se status >= 400 ou falha de conexão
 *               - 'msg_erro'(string)      : mensagem de erro legível, se houver
 */
function chamar_api(string $metodo, string $endpoint, ?array $dados = null, array $headers = []): array
{
    // Monta a URL completa: base + endpoint (ex: "http://localhost:8080/usuarios")
    $url = API_BASE_URL . $endpoint;

    // Inicializa o cURL
    $ch = curl_init($url);

    // ── Headers padrão ──────────────────────────────────────────────────────
    // A API Javalin espera JSON; avisamos isso em Content-Type.
    $headersCompletos = array_merge(
        ['Content-Type: application/json', 'Accept: application/json'],
        $headers
    );

    // ── Configurações comuns do cURL ─────────────────────────────────────────
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);  // Retorna o corpo como string
    curl_setopt($ch, CURLOPT_HTTPHEADER, $headersCompletos);
    curl_setopt($ch, CURLOPT_TIMEOUT, 10);            // Timeout de 10 segundos
    curl_setopt($ch, CURLOPT_CONNECTTIMEOUT, 5);      // Timeout de conexão

    // ── Configuração específica por método HTTP ───────────────────────────────
    $corpoJson = $dados !== null ? json_encode($dados) : null;

    switch (strtoupper($metodo)) {
        case 'POST':
            // POST: envia o corpo JSON para criar um novo recurso
            curl_setopt($ch, CURLOPT_POST, true);
            curl_setopt($ch, CURLOPT_POSTFIELDS, $corpoJson);
            break;

        case 'PUT':
            // PUT: substitui completamente um recurso existente
            curl_setopt($ch, CURLOPT_CUSTOMREQUEST, 'PUT');
            curl_setopt($ch, CURLOPT_POSTFIELDS, $corpoJson);
            break;

        case 'PATCH':
            // PATCH: atualização parcial de um recurso
            curl_setopt($ch, CURLOPT_CUSTOMREQUEST, 'PATCH');
            curl_setopt($ch, CURLOPT_POSTFIELDS, $corpoJson);
            break;

        case 'DELETE':
            // DELETE: remove o recurso; normalmente não tem corpo
            curl_setopt($ch, CURLOPT_CUSTOMREQUEST, 'DELETE');
            break;

        case 'GET':
        default:
            // GET: apenas lê dados; sem corpo
            break;
    }

    // ── Executa a requisição ─────────────────────────────────────────────────
    $respostaRaw  = curl_exec($ch);
    $statusHttp   = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    $erroCurl     = curl_error($ch);
    curl_close($ch);

    // ── Trata falha de conexão (API offline, porta errada etc.) ─────────────
    if ($respostaRaw === false) {
        return [
            'status'   => 0,
            'corpo'    => null,
            'erro'     => true,
            'msg_erro' => 'Não foi possível conectar à API. Verifique se o servidor Java está rodando. Detalhe: ' . $erroCurl,
        ];
    }

    // ── Tenta decodificar o corpo como JSON; se não for JSON, mantém bruto ──
    $corpoDecodificado = json_decode($respostaRaw, true);
    $corpo = ($corpoDecodificado !== null) ? $corpoDecodificado : $respostaRaw;

    // ── Determina se houve erro de negócio (4xx) ou servidor (5xx) ──────────
    $temErro = ($statusHttp >= 400);

    // Tenta extrair mensagem de erro legível da resposta
    $msgErro = '';
    if ($temErro) {
        if (is_string($corpo)) {
            $msgErro = $corpo; // A API retornou texto puro no corpo do erro
        } elseif (isset($corpo['mensagem'])) {
            $msgErro = $corpo['mensagem'];
        } elseif (isset($corpo['erro'])) {
            $msgErro = $corpo['erro'];
        } else {
            $msgErro = 'Ocorreu um erro (HTTP ' . $statusHttp . ').';
        }
    }

    return [
        'status'   => $statusHttp,
        'corpo'    => $corpo,
        'erro'     => $temErro,
        'msg_erro' => $msgErro,
    ];
}

// ─── Funções auxiliares de sessão ─────────────────────────────────────────────

/**
 * Inicia a sessão PHP (se ainda não iniciada).
 * Usamos a sessão para armazenar os dados do usuário logado.
 */
function iniciar_sessao(): void
{
    if (session_status() === PHP_SESSION_NONE) {
        session_start();
    }
}

/**
 * Retorna os dados do usuário atualmente logado (da sessão),
 * ou null se não houver sessão ativa.
 *
 * @return array|null Ex: ['id' => 1, 'nome' => 'Ana', 'email' => '...', 'perfil' => 'USUARIO']
 */
function usuario_logado(): ?array
{
    iniciar_sessao();
    return $_SESSION['usuario'] ?? null;
}

/**
 * Retorna o ID do usuário logado para ser usado no header X-Usuario-Id,
 * ou null se não houver usuário logado.
 */
function id_usuario_logado(): ?int
{
    $usuario = usuario_logado();
    return $usuario ? (int) $usuario['id'] : null;
}

/**
 * Monta o array de headers de autenticação com X-Usuario-Id.
 * A API Javalin usa esse header para identificar o executor da ação.
 *
 * @return array Ex: ['X-Usuario-Id: 1']
 */
function headers_autenticados(): array
{
    $id = id_usuario_logado();
    if ($id !== null) {
        return ["X-Usuario-Id: {$id}"];
    }
    return [];
}

/**
 * Verifica se o usuário está logado; se não, redireciona para o login.
 */
function exigir_login(): void
{
    if (usuario_logado() === null) {
        header('Location: login.php');
        exit;
    }
}

/**
 * Verifica se o usuário logado tem perfil ADMINISTRADOR.
 */
function eh_administrador(): bool
{
    $usuario = usuario_logado();
    return $usuario && $usuario['perfil'] === 'ADMINISTRADOR';
}
