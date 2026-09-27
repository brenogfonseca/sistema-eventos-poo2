# Sistema de Eventos POO2

Este é o repositório base para o sistema de gestão de eventos desenvolvido para a disciplina de POO2. O projeto utiliza uma **Arquitetura Hexagonal** limpa e o micro-framework **Javalin** para as rotas web REST, mantendo a persistência em memória para facilitar os testes iniciais.

## 🚀 Como rodar o projeto

### Pré-requisitos
* Java 17 ou superior
* Maven instalado na máquina (`mvn`)

### 1. Compilar o Projeto
Antes de rodar a primeira vez (ou sempre que fizer alterações estruturais), limpe os builds antigos e compile o projeto novamente:
```bash
mvn clean compile
```

### 2. Executar a Aplicação
O projeto usa o plugin `exec` do Maven para rodar a classe `Main` diretamente pelo terminal. Para subir o servidor na porta 8080, rode:
```bash
mvn exec:java -Dexec.mainClass="br.ueg.eventos.Main"
```
*(Se você estiver usando uma IDE como IntelliJ, VSCode ou Eclipse, basta abrir a classe `Main.java` e clicar no botão de "Play"/Run).*

---

## 🛠️ Como testar as Rotas da API

Com a aplicação rodando no seu terminal, abra **outra aba do terminal** (ou use softwares como Postman/Insomnia) e execute os comandos cURL abaixo.

### Criar um novo Evento (POST)
Cria um novo evento no sistema.
```bash
curl -X POST http://localhost:8080/eventos \
     -H "Content-Type: application/json" \
     -d '{
           "titulo": "Feira de Computação 2026",
           "descricao": "Maior evento de tecnologia do semestre",
           "inicio": "2026-10-10T08:00:00",
           "fim": "2026-10-12T18:00:00",
           "capacidade": 200
         }'
```
> **Retorno Esperado:** Status `201 CREATED` com o JSON completo contendo o ID gerado para o evento.

### Listar todos os Eventos (GET)
Retorna todos os eventos que foram salvos em memória.
```bash
curl http://localhost:8080/eventos
```
> **Retorno Esperado:** Status `200 OK` com a lista JSON de eventos criados. Como a persistência atual é em memória, reiniciar a aplicação apagará a lista.
