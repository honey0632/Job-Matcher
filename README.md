# Job Fetcher

Spring Boot backend for fetching Google Careers jobs, storing them in PostgreSQL, accepting frontend resume uploads, extracting resume text, and producing baseline job matches.

## Requirements

- Java 21 or newer
- PostgreSQL 18 or compatible
- Maven Wrapper included in the repository

Docker Compose is available, but the current local configuration uses the installed PostgreSQL service.

## PostgreSQL configuration

Create a PostgreSQL database and user, then configure `src/main/resources/application.properties` or environment variables:

```text
DATABASE_URL=jdbc:postgresql://localhost:5432/jobfetcher
DATABASE_USERNAME=your-postgres-user
DATABASE_PASSWORD=your-postgres-password
GOOGLE_CLIENT_ID=your-google-oauth-client-id
GOOGLE_CLIENT_SECRET=your-google-oauth-client-secret
FRONTEND_ORIGIN=http://localhost:5173
```

The application currently uses:

```text
spring.jpa.hibernate.ddl-auto=update
```

This is suitable for development. Use migrations before production deployment.

## Run the application

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

The server starts on:

```text
http://localhost:8080
```

## Job API

Fetch and persist Google Careers results:

```text
GET /api/jobs/search?query=software%20engineer
```

List stored jobs:

```text
GET /api/jobs
```

Get one stored job:

```text
GET /api/jobs/{id}
```

Repeated Google Careers searches update existing records by `externalId` instead of creating duplicates.

## Resume API

OAuth2 login:

```text
GET /oauth2/authorization/google
```

Configure this Google OAuth redirect URI:

```text
http://localhost:8080/login/oauth2/code/google
```

The backend stores authentication in a server session and the browser uses an HttpOnly session cookie. OAuth tokens are not stored in local storage.

Current authenticated user:

```text
GET /api/auth/me
```

Create a user (legacy development endpoint):

```http
POST /api/users
Content-Type: application/json

{
  "email": "candidate@example.com"
}
```

Upload a PDF or DOCX resume from the authenticated frontend:

```text
POST /api/resumes/upload
Content-Type: multipart/form-data
```

The multipart field name must be:

```text
file
```

List the authenticated user's uploaded resumes:

```text
GET /api/resumes
```

Supported files:

- PDF: `application/pdf`
- DOCX: `application/vnd.openxmlformats-officedocument.wordprocessingml.document`
- Maximum request size: 10 MB

Files are stored under `data/resumes/` by default and that directory is ignored by Git. Override the location with:

```text
RESUME_STORAGE_DIR=C:\path\outside\the\repository
```

The upload response reports the extraction status. A successful upload currently reaches `EXTRACTED`; malformed documents report an extraction failure.

## Matching API

Save authenticated search preferences:

```http
PUT /api/profile/preferences
Content-Type: application/json

{
  "country": "India",
  "experienceYears": 3,
  "desiredRole": "Java Backend Engineer"
}
```

Run the criteria-based search and return only matches above 80%:

```http
POST /api/jobs/search
Content-Type: application/json

{
  "country": "India",
  "experienceYears": 3,
  "desiredRole": "Java Backend Engineer"
}
```

Request authenticated baseline matches:

```text
GET /api/jobs/matches?threshold=80&limit=20
```

The current implementation uses normalized keyword overlap between extracted resume text and stored job title/description. It is an intentionally replaceable baseline; embeddings and vector search are not enabled yet.

## Frontend

```powershell
Set-Location frontend
npm install
npm run dev
```

Set `frontend/.env` when the backend is not on the default origin:

```text
VITE_API_BASE_URL=http://localhost:8080
```

You can start from `frontend/.env.example`.

## Frontend integration

The React frontend should:

1. Sign in through Google OAuth2.
2. Save country, experience, and desired role.
3. Send the resume as `multipart/form-data` using the `file` field.
4. Submit the criteria search.
5. Display the returned jobs, all of which have a score strictly greater than 80.

For browser requests from a separate frontend origin, configure CORS before connecting the production frontend.

## Docker and CI/CD

The repository includes production Dockerfiles:

- `Dockerfile` builds and runs the Spring Boot backend.
- `frontend/Dockerfile` builds the React application and serves it with Nginx.

Build the images locally:

```powershell
docker build -t job-fetcher-backend .
docker build --build-arg VITE_API_BASE_URL=http://localhost:8080 -t job-fetcher-frontend frontend
```

The GitHub Actions workflow at `.github/workflows/ci-cd.yml`:

1. Starts a PostgreSQL service for backend tests.
2. Runs backend tests and the frontend production build.
3. On pushes to `main`, publishes both images to GitHub Container Registry.

The published images target both `linux/amd64` and `linux/arm64`, so they run on Oracle Cloud `VM.Standard.A1.Flex` instances.

Create this GitHub repository variable before publishing the frontend image:

```text
VITE_API_BASE_URL=https://your-api-domain.com
```

The workflow publishes:

```text
ghcr.io/<owner>/job-fetcher-backend:latest
ghcr.io/<owner>/job-fetcher-frontend:latest
```

The Oracle Cloud deployment server should pull these images and provide the runtime secrets through environment variables. Do not put database passwords or OAuth secrets in the workflow or Dockerfiles.

### Environment variables

Copy the root environment template before running locally or deploying:

```powershell
Copy-Item .env.example .env
```

Edit `.env` with the real database, Google OAuth, domain, storage, and registry values. The `.env` file is ignored by Git and must never be committed.

Spring Boot loads this file automatically when running locally, while Docker Compose loads it through `env_file`.

For an Oracle Cloud VM deployment:

```powershell
docker login ghcr.io
docker compose --env-file .env -f compose.production.yaml pull
docker compose --env-file .env -f compose.production.yaml up -d
```

The production Compose file runs PostgreSQL on the same Oracle VM to avoid an additional database charge. PostgreSQL data is stored in the named `postgres-data` volume and should be backed up separately. Copy `.env.oracle.example` to `.env` on the VM and replace `POSTGRES_PASSWORD` with a strong random value before deployment. Do not use the local developer `.env` on the VM.

The production stack also includes Caddy. It serves HTTPS for `jobmatcher.in` and `api.jobmatcher.in` and routes traffic to the frontend and backend containers. Both DNS records must point to the Oracle VM before Caddy can obtain certificates.

The frontend image receives `VITE_API_BASE_URL` during the GitHub Actions build. Set the corresponding GitHub repository variable before publishing production images. Runtime secrets such as database credentials and OAuth secrets belong only in the server's `.env`.

## Project structure

```text
src/main/java/com/honey/jobfetcher
├── client       External Google Careers client
├── config       Spring configuration
├── controller   REST endpoints
├── dto          API request/response objects
├── exception    Explicit application errors
├── extractor    PDF/DOCX text extraction
├── model        JPA entities
├── parser       Google Careers response parsing
├── repository   Spring Data repositories
└── service      Business logic and matching
```
