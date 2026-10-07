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

### 2. Executar a Aplicação (Back-end)
O projeto usa o micro-framework Javalin e o plugin `exec` do Maven para rodar a classe `Main` diretamente pelo terminal. Para subir a API REST na porta 8080, execute:
```bash
mvn compile exec:java -Dexec.mainClass="br.ueg.eventos.Main"
```
*(Ou se preferir, abra a classe `Main.java` na sua IDE e execute).*

---

## 💻 Como rodar o Front-end (PHP)

O front-end do projeto é construído em **PHP nativo**, **HTML5** e **CSS3** (localizado na pasta `/frontend`). Ele consome a API REST Java em `http://localhost:8080`.

### Pré-requisitos do Front
* PHP 8.0+ instalado na máquina (com extensões `curl` e `json` habilitadas).

### 1. Iniciar o servidor embutido do PHP
Em um novo terminal, entre na pasta do front-end e inicie o servidor na porta 3000:
```bash
cd frontend
php -S localhost:3000
```

### 2. Acessar no Navegador
Abra seu navegador e acesse:
```
http://localhost:3000
```

---

## ⚡ Inicialização Rápida (Script Windows)

Para facilitar, você pode iniciar tanto o back-end quanto o front-end simultaneamente com apenas dois cliques no arquivo:
```
iniciar_sistema.bat
```
Esse script abre automaticamente a API Java na porta 8080, o servidor PHP na porta 3000 e abre a página no navegador.

---

## 👤 Usuário Administrador Inicial (Para Testes)

Para testes imediatos e navegação com privilégios de administrador sem precisar cadastrar e promover manualmente, o sistema já inicia com um usuário administrador pré-carregado em memória:

| Campo | Valor de Teste |
|---|---|
| **Nome** | Administrador |
| **E-mail** | `admin@evento.com` |
| **Senha** | `Admin@123` |
| **Perfil** | `ADMINISTRADOR` |
| **ID** | `1` |

> ℹ️ **Nota:** Esse usuário foi configurado provisoriamente em `InMemoryUsuarioRepository.java` apenas para testes em ambiente de desenvolvimento.

---

## 🛠️ Como testar as Rotas da API via Terminal (cURL)

Com a aplicação Java rodando, você também pode testar os endpoints diretamente via terminal:

### Criar um novo Evento (POST)
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
> **Retorno Esperado:** Status `201 CREATED` com o JSON do evento criado.

### Listar todos os Eventos (GET)
```bash
curl http://localhost:8080/eventos
```
> **Retorno Esperado:** Status `200 OK` com a lista JSON de eventos. Como a persistência atual é em memória, reiniciar a aplicação reiniciará os dados.
