@echo off
title Sistema Eventos - POO2
echo ========================================================
echo   Iniciando Sistema de Eventos (POO2)
echo ========================================================
echo.

echo 1. Abrindo Back-end (API Java Javalin na porta 8080)...
start "API Java (Porta 8080)" cmd /k "cd /d "%~dp0" && mvn compile exec:java"

echo.
echo Aguardando 3 segundos...
timeout /t 3 /nobreak >nul

echo 2. Abrindo Front-end (PHP na porta 3000)...
:: 🌐 Mudança aqui: Tiramos o caminho fixo e chamamos apenas 'php' direto, confiando no PATH do Windows de cada um
start "Front-end PHP (Porta 3000)" cmd /k "cd /d "%~dp0frontend" && php -S localhost:3000"

echo.
echo 3. Abrindo navegador em http://localhost:3000 ...
timeout /t 2 /nobreak >nul
start http://localhost:3000

echo.
echo ========================================================
echo   Tudo pronto! As duas janelas foram iniciadas.
echo   Para encerrar, basta fechar as janelas pretas do CMD.
echo ========================================================
pause
