# Sistema de Gestão Financeira — POO (Quarkus + Kafka + Angular)

Software de gestão financeira pessoal desenvolvido com **Quarkus (Java 17)** em **Clean Architecture multi-módulo**, **Apache Kafka** para ingestão assíncrona sob alta carga, **Angular 22** no frontend e **PostgreSQL**.

## Arquitetura do Backend (Multi-Módulo Maven)
Seguindo a separação estrita em camadas:
- `backend/domain/` — Entidades puras de domínio e enums (sem dependências de framework)
- `backend/implementation/` — Interfaces de serviços e repositórios, DTOs e exceções de negócio
- `backend/application/` — Regras de negócio e serviços de aplicação (depende apenas de `domain` e `implementation`)
- `backend/infra/` — Persistência JPA/Panache, cache em memória, segurança (BCrypt + JWT) e produtor/consumidor Kafka
- `backend/web/` — Controllers REST finos, mapeador de exceções, configuração Quarkus e testes `@QuarkusTest`

## Funcionalidades
- **Autenticação JWT via Cookie HttpOnly** (`POST /auth/register`, `POST /auth/login`, `POST /auth/logout`, `GET /auth/me`)
- **Tela Inicial (Dashboard)**: saldo geral, total de receitas e despesas, saldo individual por conta e últimas movimentações
- **Contas e Categorias**: CRUD completo com proteção contra exclusão quando há lançamentos vinculados
- **Transações e Transferências via Kafka**: `POST /Transacao` e `POST /Transacao/transferencia` enfileiram eventos no tópico `financeiro-transacoes` (`202 Accepted`) com validação em cache e consumidor dedicado para proteger o banco em picos de requisições
- **Filtros**: transações filtradas por conta, categoria, tipo, período e busca textual

## Configuração Local (sem versionar senhas)
1. Copie `backend/config/application.properties.example` para `backend/config/application.properties` (arquivo ignorado pelo Git, equivalente ao `appsettings.Development.json`).
2. Preencha `quarkus.datasource.jdbc.url`, `quarkus.datasource.username`, `quarkus.datasource.password` e `kafka.bootstrap.servers` com os dados do seu container PostgreSQL e Kafka.
   *(Alternativamente, defina as variáveis de ambiente `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` e `KAFKA_BOOTSTRAP_SERVERS`.)*

## Como executar
1. **Subir o Apache Kafka via Docker Compose** (na raiz do projeto):
   ```bash
   docker compose up -d
   ```
   *(O banco PostgreSQL `dev_financeiro` já deve estar rodando na porta `5432`, ex.: container `db-postgre`).*
2. **Pelo IntelliJ IDEA**: selecione a configuração **Fullstack (Backend + Frontend)** (ou **Backend (Quarkus Dev)** / **Frontend (Angular Serve)**) na barra superior e clique em **Run** (`Shift+F10`).
3. **Pelo terminal**:
   - **Backend** (porta `8080`):
     ```bash
     cd backend
     ./mvnw -pl web quarkus:dev
     ```
   - **Frontend** (porta `4200`):
     ```bash
     cd frontend
     npm install
     npm start
     ```
