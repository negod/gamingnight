# Deployment

## Frontend: Cloudflare Pages

1. Connect the Git repository in Cloudflare Pages.
2. Set project root to `frontend`.
3. Use build command `npm run build`.
4. Use build output directory `dist`.
5. Add environment variable:

```text
VITE_API_BASE_URL=https://your-render-service.onrender.com/api
```

Deployments are static assets served by Cloudflare. The frontend talks to the backend through `VITE_API_BASE_URL`.

## Backend: Render

Create a Render Web Service.

```text
Root directory: backend
Build command: mvn clean package
Start command: java -jar target/gaming-night-0.0.1-SNAPSHOT.jar
```

Environment variables:

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://HOST:PORT/DATABASE?sslmode=require
SPRING_DATASOURCE_USERNAME=USER
SPRING_DATASOURCE_PASSWORD=PASSWORD
CORS_ALLOWED_ORIGINS=https://your-cloudflare-pages-domain.pages.dev
PORT=8080
```

Render provides `PORT`. The app reads it through `server.port=${PORT:8080}`.

## Database: Supabase or Neon

Create a PostgreSQL database in Supabase or Neon and copy the connection details.

Use a JDBC URL:

```text
jdbc:postgresql://HOST:PORT/DATABASE?sslmode=require
```

Set username and password from the provider dashboard. Liquibase applies schema migrations automatically on backend startup.

## Liquibase Migration Workflow

Schema changes are managed with Liquibase YAML changelogs.

### File layout

```text
backend/src/main/resources/db/changelog/
├── db.changelog-master.yaml        # ordered include list
└── changes/
    ├── 0001-create-items.yaml
    ├── 0002-create-players.yaml
    └── ...                         # one file per migration
```

### Adding a new migration

1. Create a file in `changes/` with the next sequential prefix and a descriptive name:

   ```
   changes/0008-add-avatar-to-players.yaml
   ```

2. Write the changeset using Liquibase YAML syntax. Always include `id` (matching the filename prefix and description), `author`, and the relevant `changes` block:

   ```yaml
   databaseChangeLog:
     - changeSet:
         id: 0008-add-avatar-to-players
         author: backede
         changes:
           - addColumn:
               tableName: players
               columns:
                 - column:
                     name: avatar_url
                     type: varchar(500)
   ```

3. Append an `include` entry to `db.changelog-master.yaml`:

   ```yaml
   - include:
       file: db/changelog/changes/0008-add-avatar-to-players.yaml
   ```

4. Restart the application (or run tests). Liquibase applies only unapplied changesets by comparing against its `DATABASECHANGELOG` table.

### Rules

- Never edit a changeset that has already been applied to any environment. Add a new changeset instead.
- Use descriptive IDs that match the filename.
- Test each migration locally with `docker compose up -d postgres` before pushing.
- Use `rollback` blocks for destructive changes if rollback support is needed.

## Production Notes

- Keep `ddl-auto=validate`; schema changes must be Liquibase changelogs.
- Set CORS to the exact frontend origin.
- Do not commit real `.env` files.
- Prefer provider-managed connection pooling for production traffic.
