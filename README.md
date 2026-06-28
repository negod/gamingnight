# Gaming Night

Gaming Night is a web application for keeping track of scores during game nights. The goal is to make it easy to add games, create scoreboards, record players and rounds, and see who is winning over time.

The app is intentionally built with a production-ready structure from the start: React, Vite, TypeScript, Tailwind CSS, Spring Boot 3, Java 21, PostgreSQL, Spring Data JPA, Hibernate, and Liquibase.

Current implementation status:

- Create item
- List items
- Get item by id
- Update item
- Delete item
- Health endpoint

The current `Item` feature is a starter vertical slice used to prove the architecture, testing setup, database access, REST API, and frontend flow. The domain will evolve toward Gaming Night features such as:

- Add and manage games
- Create scoreboards
- Add players or teams
- Track scores per game session
- View standings and score history

More product details will be added as the app design becomes clearer.

## Final File Structure

```text
.
├── backend
│   ├── pom.xml
│   ├── .env.example
│   └── src
│       ├── main
│       │   ├── java/se/backede
│       │   │   ├── domain
│       │   │   ├── application
│       │   │   ├── infrastructure
│       │   │   └── shared
│       │   └── resources
│       └── test/java/se/backede
├── frontend
│   ├── package.json
│   ├── .env.example
│   └── src
│       ├── app
│       ├── pages
│       ├── features/items
│       └── shared
├── docs
│   ├── ai-instructions.md
│   ├── architecture.md
│   ├── deployment.md
│   └── testing.md
├── AGENTS.md
├── CLAUDE.md
├── MISTRAL.md
├── docker-compose.yml
├── package.json
└── README.md
```

## Architecture Overview

Gaming Night follows Clean Architecture on the backend:

- `domain`: framework-free business model and repository ports.
- `application`: use cases, DTOs, and DTO mappers.
- `infrastructure`: Spring web controllers, persistence entities, repositories, configuration, and adapters.
- `shared`: framework-free exceptions and cross-cutting primitives.

Dependencies point inward. Controllers call use cases. Use cases depend on domain ports. Infrastructure implements those ports. Domain code has no Spring, JPA, database, or web dependency.

The frontend keeps API access outside UI components:

- `app`: routing and application shell.
- `pages`: route-level screens.
- `features/items`: current starter feature API wrappers, hooks, and components. Future game-night features should live under their own feature folders.
- `shared`: reusable API client, components, and types.

## Prerequisites

- Java 21
- Maven 3.9+
- Node.js 20+
- npm 10+
- Docker and Docker Compose for local PostgreSQL

## Local Setup

```bash
npm install --prefix frontend
docker compose up -d postgres
cp backend/.env.example backend/.env
cp frontend/.env.example frontend/.env
```

## Running Backend

```bash
cd backend
mvn spring-boot:run
```

Backend API: `http://localhost:8080/api`

## Running Frontend

```bash
cd frontend
npm run dev
```

Frontend app: `http://localhost:5173`

## Running Tests

```bash
cd backend
mvn test

cd ../frontend
npm test
```

Repository adapter tests use Testcontainers and require Docker.

## Environment Variables

Backend:

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/gaming-night
SPRING_DATASOURCE_USERNAME=gaming-night
SPRING_DATASOURCE_PASSWORD=gaming-night
CORS_ALLOWED_ORIGINS=http://localhost:5173
PORT=8080
```

Frontend:

```text
VITE_API_BASE_URL=http://localhost:8080/api
```

## Database Setup

Local PostgreSQL is provided by `docker-compose.yml`.

```bash
docker compose up -d postgres
```

Liquibase runs automatically when the Spring Boot application starts and applies all changelogs from `backend/src/main/resources/db/changelog/db.changelog-master.yaml`.

To add a schema change, create a new file under `backend/src/main/resources/db/changelog/changes/` and include it in `db.changelog-master.yaml`. See `docs/deployment.md` for the full Liquibase workflow.

## Cloudflare Pages Deployment

Set the frontend project root to `frontend`.

- Build command: `npm run build`
- Build output directory: `dist`
- Environment variable: `VITE_API_BASE_URL=https://your-render-service.onrender.com/api`

## Render Deployment

Create a Render Web Service from the repository.

- Root directory: `backend`
- Runtime: Java
- Build command: `mvn clean package`
- Start command: `java -jar target/gaming-night-0.0.1-SNAPSHOT.jar`
- Environment variables: set `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, and `CORS_ALLOWED_ORIGINS`.

## Supabase or Neon PostgreSQL

Use the provider's pooled or direct PostgreSQL connection details and map them to Spring environment variables.

For JDBC URLs, use this format:

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://HOST:PORT/DATABASE?sslmode=require
SPRING_DATASOURCE_USERNAME=USER
SPRING_DATASOURCE_PASSWORD=PASSWORD
```

Use `sslmode=require` unless your provider recommends a different SSL setting.
