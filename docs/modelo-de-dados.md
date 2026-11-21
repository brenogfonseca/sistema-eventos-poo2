# Modelo de dados PostgreSQL

O esquema vazio é montado automaticamente na inicialização da API. As migrações SQL versionadas estão em `src/main/resources/db/migration/postgresql`.

## Entidades e relações

| Tabela | Responsabilidade e relações principais |
|---|---|
| `eventos` | Dados, período, modalidade, estado, capacidade, política de certificado, fuso e prazo de cancelamento. |
| `usuarios` | Contas, hash da senha e perfil. O e-mail normalizado é único. |
| `vinculos_organizadores` | Relação muitos-para-muitos entre usuário e evento; identifica o criador principal. |
| `atividades` | Programação de um evento, horário, categoria, local, trilha, vagas e política de frequência. |
| `locais` | Espaços cadastrados para serem associados a atividades. Excluir local remove o vínculo, preservando o nome histórico da atividade. |
| `categorias`, `trilhas` | Classificações vinculadas a cada evento e reutilizadas pela programação. |
| `pessoas_programacao`, `atividade_pessoas` | Palestrantes, apresentadores e responsáveis, sem exigir conta de usuário. |
| `inscricoes_evento` | Inscrição do participante no evento; chave única evento/usuário. É a identidade representada pelo QR único. |
| `inscricoes` | Escolhas de atividades dentro da inscrição no evento. Índice parcial impede duplicidade ativa. |
| `frequencias` | Marcações com data/hora, tipo, origem, presença e responsável que validou. |
| `questionarios`, `questoes`, `respostas` | Avaliações de uma atividade, perguntas tipadas e respostas individuais. Índice único impede resposta repetida por questão/usuário. |
| `certificados` | Histórico de emissão para participantes e pessoas da programação, com carga horária calculada em minutos. |
| `schema_migrations` | Versões aplicadas pelo inicializador PostgreSQL. |

## Integridade relevante

- Chaves estrangeiras protegem relações entre evento, atividade, conta, inscrição, frequência e questionário.
- Índices apoiam filtros de eventos, agenda por evento/data, inscrições e avaliações publicadas.
- Índices únicos impedem e-mail repetido ignorando caixa, inscrição duplicada no evento, atividade duplicada ativa e resposta repetida à mesma questão.
- Reservas de vaga do evento usam bloqueio da linha do evento; a reserva de vaga de atividade é um `UPDATE` condicional, evitando exceder o saldo em requisições concorrentes.
- Flags legadas permanecem como `INTEGER` restrito a 0/1; datas de evento/atividade são armazenadas em texto ISO para manter compatibilidade com os modelos existentes.

## Evolução

| Versão | Conteúdo |
|---|---|
| V1 | Eventos e atividades. |
| V2 | Usuários, inscrição em atividade, frequência, questionário, questões e respostas. |
| V3 | Vínculos de organizadores. |
| V4 | Inscrição no evento e migração das escolhas de atividade. |
| V5 | Estado/modalidade/fuso, categorias, trilhas, pessoas da programação, opções de questões, índices e certificados. |
| V6 | Prazo configurável para cancelamento. |
| V7 | Chave estrangeira entre atividade e local cadastrado. |
| V8 | Registro de certificados para palestrantes e apresentadores sem exigir conta de usuário. |

Migrações já aplicadas não devem ser alteradas. Mudanças posteriores devem ser acrescentadas como nova versão.
