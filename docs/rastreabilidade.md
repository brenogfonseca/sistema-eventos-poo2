# Rastreabilidade dos requisitos

Esta matriz relaciona os requisitos funcionais do documento do projeto aos módulos implementados. O status “implementado” descreve a presença do fluxo no código; ainda é necessário executar a aplicação com JDK, Maven, Docker, PostgreSQL e PHP para obter evidência de execução no computador de entrega.

| Requisito/fluxo | Implementação | Interface | Status |
|---|---|---|---|
| Cadastro, login e perfis | `UsuarioController`, `AuthController`, senha PBKDF2 e filtro bearer | `cadastro.php`, `login.php`, JavaFX | Implementado |
| Cadastro e ciclo de vida de evento | Evento, vínculo de organizador, estado, modalidade, fuso e prazo | `criar_evento.php`, `editar_evento.php`, JavaFX | Implementado |
| Atividades, categoria, trilha e local | CRUD de atividades; categorias/trilhas por evento; associação com `locais` | `atividades.php`, `editar_atividade.php`, JavaFX | Implementado |
| Pessoas da programação | Pessoas sem conta associadas a atividades e papel | `atividades.php`, JavaFX | Implementado |
| Programação pública e filtros | Consulta pública de eventos/atividades; filtros por data, categoria, trilha, local e horário | `eventos.php`, `atividades.php`, JavaFX | Implementado |
| Inscrição no evento e escolha de atividades | Inscrição primeiro; escolhas separadas; capacidade e conflito de horário no serviço | `atividades.php`, `minhas_inscricoes.php`, JavaFX | Implementado |
| Cancelamento | Prazo por evento e liberação de vagas das atividades | `minhas_inscricoes.php`, JavaFX | Implementado |
| Presença e auditoria | Política por atividade: check-in, entrada/saída ou manual; data, origem e responsável | `checkin_qr.php`, JavaFX | Implementado |
| QR único por evento | Token assinado associado à inscrição, válido até o encerramento no fuso do evento; organizador seleciona a atividade | `minhas_inscricoes.php`, `checkin_qr.php`, JavaFX | Implementado |
| Frequência mínima e carga horária | Percentual ponderado por minutos de atividade; mínimo 50%, 75% ou 100% | `minhas_inscricoes.php`, JavaFX | Implementado |
| Certificado de participante | PDF pessoal após o evento, se habilitado e elegível; registro na tabela `certificados` | `minhas_inscricoes.php`, JavaFX | Implementado |
| Questionários | Tipos textual, escolha única e escala; publicação, anonimato e uma resposta por questão | `avaliacoes.php`, JavaFX | Implementado |
| Consolidação e relatórios | Exportação CSV de inscrições, frequência e respostas; dados anônimos sem identificação | `relatorios.php`, `avaliacoes.php`, JavaFX | Implementado |
| Persistência PostgreSQL | Migrações V1–V8 e repositórios JDBC PostgreSQL | compartilhado pelos dois clientes | Implementado |
| Cliente desktop independente | JavaFX consome API REST e reaproveita as regras do servidor | `DesktopApplication` | Implementado; precisa de compilação e validação local |
| Certificado de palestrante/apresentador | PDF com a carga horária das atividades vinculadas; emissão restrita ao organizador após o evento e registro na tabela `certificados` | `atividades.php`, JavaFX | Implementado |
| Testes unitários e evidências de execução | Há testes de invariantes do evento/período, hash de senha e geração de PDF; o ambiente atual não dispõe de Maven/JDK 17/Docker/PHP para executar a suíte ou registrar evidências reais | `src/test/java`, roteiro abaixo | Implementados; execução pendente |

## Cenários para demonstrar o fluxo

1. Criar conta e entrar; validar que o perfil enviado no cadastro não promove a própria conta a administrador.
2. Criar um evento como rascunho, cadastrar local, atividade e pessoa da programação; confirmar que a programação do rascunho só aparece para organizadores.
3. Publicar o evento e abrir em sessão anônima; filtrar a programação por pelo menos três critérios.
4. Inscrever duas contas no mesmo evento; escolher atividades sem conflito; tentar selecionar duas atividades sobrepostas e uma atividade sem vagas.
5. Cancelar a inscrição antes do prazo e confirmar que vagas voltaram; repetir depois do prazo e confirmar rejeição.
6. Exibir o QR único do participante e ler no painel do organizador; confirmar cada modo de presença e testar duplicidade/ação incompatível.
7. Conferir os minutos validados, testar os limites 50%, 75% e 100%, encerrar o evento e baixar o PDF somente para uma inscrição elegível.
8. Criar avaliação com os três tipos, tentar responder antes de presença/publicação, responder após validar presença e tentar responder à mesma questão novamente.
9. Baixar CSV como organizador e como usuário sem vínculo; a API deve permitir o primeiro e negar o segundo.
10. Reiniciar a API e confirmar que eventos, contas, inscrições e marcações continuam no PostgreSQL.

As etapas acima são um roteiro manual de validação, não uma declaração de que foram executadas neste ambiente.
