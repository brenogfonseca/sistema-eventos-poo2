# Arquitetura e decisões

## Visão geral

```mermaid
flowchart TB
  subgraph Clientes
    PHP[Web PHP]
    FX[Desktop JavaFX]
  end
  subgraph API[Aplicação Java / Javalin]
    WEB[Controllers REST]
    IN[Ports de entrada]
    APP[Services]
    DOM[Domínio: entidades e invariantes]
    OUT[Ports de saída]
    WEB --> IN --> APP --> DOM
    APP --> OUT
  end
  PG[(PostgreSQL)]
  PHP -->|HTTP JSON| WEB
  FX -->|HTTP JSON| WEB
  OUT --> JDBC[Adaptadores JDBC]
  JDBC --> PG
```

O PHP e o JavaFX são adaptadores de entrada. Ambos compartilham autenticação, regras de negócio e armazenamento. O JavaFX não acessa diretamente o PostgreSQL, evitando duplicar regras no cliente.

## Decisões de projeto

| ID | Decisão | Motivo e efeito |
|---|---|---|
| D01 | Arquitetura hexagonal | Regras ficam em services e domínio; HTTP e JDBC são adaptadores substituíveis. |
| D02 | PostgreSQL como persistência de execução | Oferece integridade referencial, índices parciais e bloqueio de linhas para controle concorrente de vagas. |
| D03 | PostgreSQL inicia vazio e evolui por migrações | A inicialização registra e executa versões V1–V8; não existe import automático do SQLite. |
| D04 | Clientes consomem a mesma API REST | Evita diferenças de validação entre web e desktop e centraliza autorização. |
| D05 | Sessões bearer assinadas e senhas PBKDF2 | O token carrega identidade e expiração; senhas novas usam salt aleatório e derivação lenta. Hashes SHA-256 antigos são atualizados no próximo login válido. |
| D06 | Um QR assinado por inscrição no evento | O identificador não inclui dados pessoais; o organizador escolhe a atividade na leitura e a API confirma a inscrição naquela atividade. |
| D07 | Critério de frequência pertence à atividade | CHECKIN, ENTRADA_SAIDA e MANUAL têm validações específicas e registros auditáveis. |
| D08 | Frequência do certificado é ponderada pelos minutos | O percentual compara os minutos das atividades escolhidas com os minutos das atividades validadas. O organizador configura 50%, 75% ou 100%. |
| D09 | Avaliações exigem inscrição e presença validada | Uma resposta por participante e questão; anonimato remove a identificação das visões/exportações do organizador. |
| D10 | Cancelamento lógico e histórico | Eventos e inscrições mantêm histórico; cancelar uma inscrição antes do prazo libera as vagas selecionadas. |

## Limites atuais

- Certificados de participantes dependem do mínimo de frequência configurado; palestrantes e apresentadores recebem documento de atuação com a carga horária das atividades às quais foram vinculados.
- O leitor JavaFX lê imagem de QR carregada do computador; a câmera ao vivo fica na interface web.
- Mensageria por e-mail e publicação em ambiente remoto precisam de configuração externa e não fazem parte da execução local descrita neste repositório.
