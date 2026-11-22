# API REST

Base local: `http://localhost:8080`. Os corpos e respostas usam JSON, salvo as rotas de CSV e PDF.

## Sessão

`POST /usuarios` aceita `{ "nome": "Ana Silva", "email": "ana@example.org", "senha": "SenhaForte@1" }`. O perfil recebido do cliente não concede privilégios: a API cria a conta como `USUARIO`.

`POST /auth/login` aceita `{ "email": "ana@example.org", "senha": "SenhaForte@1" }` e responde com `id`, `nome`, `email`, `perfil`, `token` e `expiraEm`.

Rotas privadas exigem os dois cabeçalhos abaixo. O filtro confere o cabeçalho de ID contra o token assinado.

```http
Authorization: Bearer <token>
X-Usuario-Id: <id retornado no login>
```

## Eventos e atividades

| Método e rota | Uso |
|---|---|
| `GET /eventos` | Lista eventos publicados; organizadores autenticados também veem seus rascunhos. |
| `GET /eventos/{id}` | Consulta um evento. Rascunho é restrito aos responsáveis. |
| `POST /eventos` | Cria evento e associa a conta autenticada como organizadora principal. |
| `PUT /eventos/{id}` | Atualiza evento; apenas criador principal ou administrador. |
| `DELETE /eventos/{id}` | Exclusão lógica; apenas criador principal ou administrador. |
| `GET /atividades/evento/{idEvento}` | Consulta programação publicada ou, com sessão de organizador, programação em rascunho. |
| `POST /atividades` | Cria atividade. |
| `PUT /atividades/{id}` | Atualiza atividade. |
| `DELETE /atividades/{id}` | Exclui atividade quando as regras permitem. |
| `GET /locais` | Lista locais para contas autenticadas. Escrita é restrita ao administrador. |
| `POST /atividades/{id}/pessoas` | Vincula palestrante, apresentador ou responsável à atividade. |

Exemplo de evento:

```json
{
  "titulo": "Jornada de Tecnologia",
  "descricao": "Programação do curso",
  "inicio": "2027-04-10T08:00:00",
  "fim": "2027-04-10T18:00:00",
  "capacidade": 200,
  "certificado": true,
  "frequenciaMinima": 75,
  "criterioFrequenciaCertificado": "PERCENTUAL",
  "frequenciaMinimaMinutos": 0,
  "modoCargaHorariaCertificado": "DINAMICA",
  "cargaHorariaFixaMinutos": 0,
  "inscricoesAbertas": true,
  "modalidade": "PRESENCIAL",
  "status": "PUBLICADO",
  "fusoHorario": "America/Sao_Paulo",
  "prazoCancelamento": "2027-04-09T23:59:00"
}
```

Exemplo de atividade:

```json
{
  "nome": "Introdução a APIs",
  "descricao": "Oficina prática",
  "eventoId": 1,
  "data": "2027-04-10",
  "horaInicio": "09:00:00",
  "horaFim": "10:30:00",
  "tipo": "Oficina",
  "tipoFrequencia": "CHECKIN",
  "metodoIdentificacaoCheckin": "QR",
  "controlaVagas": true,
  "vagas": 30,
  "local": { "id": 1, "nome": "Laboratório 1" },
  "trilha": { "nome": "Desenvolvimento" }
}
```

`tipoFrequencia` aceita `CHECKIN`, `ENTRADA_SAIDA` ou `MANUAL`. A hora final pode ser no dia seguinte se a atividade atravessar a meia-noite; o período completo deve permanecer dentro do evento.
`metodoIdentificacaoCheckin` aceita `QR` ou `NOME_CPF`. A API não bloqueia atividades no mesmo local com horário sobreposto: responde com `avisos` no POST/PUT para que o organizador avalie o conflito. O campo `vagas` é o saldo de vagas ainda disponíveis; zero com `controlaVagas=true` significa lotação.

## Inscrição, frequência e certificado

| Método e rota | Uso |
|---|---|
| `POST /inscricoes/evento` | Inscreve a conta autenticada: `{ "idEvento": 1 }`. |
| `GET /inscricoes/eventos/usuario/{idUsuario}` | Lista inscrições da própria conta. |
| `PUT /inscricoes/evento/{id}/atividades` | Substitui escolhas: `{ "idsAtividades": [1, 2] }`. A API valida inscrição ativa, horário, capacidade e evento. |
| `GET /inscricoes/evento/{id}/atividades` | Lista escolhas feitas. |
| `DELETE /inscricoes/evento/{id}` | Cancela a inscrição e libera vagas quando o prazo ainda permite. |
| `GET /inscricoes/evento/{id}/checkin-qr` | Emite QR assinado da inscrição, não dos dados pessoais. |
| `POST /frequencias/checkin-qr` | Exemplo: `{ "token": "...", "idAtividade": 1, "acao": "CHECKIN" }`. |
| `GET /frequencias/atividades-validaveis` | Lista atividades do organizador/administrador e atividades explicitamente atribuídas ao colaborador autenticado. |
| `GET /atividades/{id}/participantes-checkin?q=...` | Busca inscritos ativos daquela atividade por nome ou CPF, conforme o método configurado; CPF é mascarado na resposta. |
| `POST /frequencias/checkin-participante` | Registra check-in por identificação nominal: `{ "idInscricaoEvento": 1, "idAtividade": 1, "acao": "CHECKIN" }`. |
| `POST /frequencias/manual` | Somente administrador/organizador pode corrigir presença ou falta; cada correção é registrada com origem `CORRECAO_ORGANIZADOR`, responsável e instante. |
| `GET /frequencias/atividade/{id}/participantes` | Lista presença atual dos inscritos ativos para correção pelo organizador. |
| `POST /atividades/{id}/colaboradores` | Organizador vincula uma conta existente por e-mail para registrar presença nesta atividade. |
| `GET /atividades/{id}/colaboradores` | Lista colaboradores vinculados; acesso restrito a administrador/organizador. |
| `DELETE /atividades/{id}/colaboradores/{idUsuario}` | Remove o vínculo do colaborador com a atividade. |
| `GET /inscricoes/evento/{id}/frequencia-certificado` | Consulta presença acumulada, carga horária máxima do evento, critério de elegibilidade, carga horária do certificado e elegibilidade da própria conta. |
| `GET /inscricoes/evento/{id}/certificado.pdf` | Emite PDF pessoal após o fim do evento, se o mínimo foi atingido. |
| `GET /eventos/{idEvento}/pessoas-programacao/{idPessoa}/certificado.pdf?papel=PALESTRANTE` | Emite certificado de palestrante/apresentador após o evento; exige organizador autenticado. |

A ação de registro depende da política: `CHECKIN`; `ENTRADA` e `SAIDA`; ou `PRESENTE`. Administradores e organizadores podem registrar e corrigir presença; colaboradores vinculados podem somente registrar presença, sem marcar ausência ou alterar o status posteriormente. O método QR ou busca por nome/CPF é configurado por atividade. A conta do participante nunca recebe permissão para confirmar a própria frequência.

O evento configura `criterioFrequenciaCertificado` como `PERCENTUAL` (com `frequenciaMinima` em 50, 75 ou 100) ou `HORAS` (com `frequenciaMinimaMinutos`). A carga impressa no certificado pode ser `DINAMICA` (minutos validados do participante) ou `FIXA` (valor de `cargaHorariaFixaMinutos`). O cálculo divide a programação pelos limites de início/fim: cada trecho simultâneo conta uma vez no total do evento, e trechos de horários diferentes se somam. A carga individual considera os trechos das atividades escolhidas em que a frequência foi validada.

## Avaliações e relatórios

Questionários usam `POST /questionarios`, `GET /questionarios/atividade/{idAtividade}` e `PUT /questionarios/{id}`. Questões usam `POST /questoes`, `PUT /questoes/{id}`, `DELETE /questoes/{id}` e `GET /questoes/questionario/{idQuestionario}`. Tipos: `TEXTUAL`, `ESCOLHA_UNICA` e `ESCALA_NUMERICA`.

Respostas usam `POST /respostas` com `{ "idQuestao": 1, "valor": "Resposta" }`. A conta é derivada do token, nunca de um ID enviado no corpo.

- `GET /questionarios/{id}/resultados.csv`
- `GET /relatorios/eventos/{id}/inscricoes.csv`
- `GET /relatorios/eventos/{id}/frequencias.csv`

Relatórios de evento exigem administrador ou organizador vinculado. Os resultados de avaliação também são restritos ao organizador; questionários anônimos não exportam a identificação do participante.
