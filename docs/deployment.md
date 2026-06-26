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

Set username and password from the provider dashboard. Flyway applies schema migrations automatically on backend startup.

## Production Notes

- Keep `ddl-auto=validate`; schema changes should be Flyway migrations.
- Set CORS to the exact frontend origin.
- Do not commit real `.env` files.
- Prefer provider-managed connection pooling for production traffic.
