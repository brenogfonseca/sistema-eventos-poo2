<?php
/**
 * logout.php — Encerramento de sessão
 * ─────────────────────────────────────────────────────────────────────────────
 * Destrói a sessão PHP e redireciona para a tela de login.
 * Não há comunicação com a API (a sessão é apenas local no PHP).
 * ─────────────────────────────────────────────────────────────────────────────
 */
require_once __DIR__ . '/api_service.php';
iniciar_sessao();

// Apaga todos os dados da sessão
$_SESSION = [];

// Destrói a sessão no servidor
session_destroy();

// Redireciona para a tela de login
header('Location: login.php');
exit;
