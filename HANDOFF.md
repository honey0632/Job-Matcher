# Job Matcher Handoff

Last updated: 2026-09-07  
Repository: `honey0632/Job-Matcher`  
Branch: `main`

## What this project is

Job Matcher is a React/Vite frontend with a Spring Boot backend. It supports:

- Google OAuth/OIDC login
- Google Careers job search and persistence
- Resume upload and parsing
- Job matching
- Privacy Policy and Terms of Service pages

Production domains:

- Frontend: `https://jobmatcher.in`
- API: `https://api.jobmatcher.in`

## Current repository state

The latest pushed commit is:

```text
0f5a5e2 Widen persisted job text fields
```

Recent relevant commits:

```text
8b249d9 Fix OAuth legal page routing
eb45dfb Redirect OAuth login to frontend
02dc49f Provision users for Google OIDC login
b6553da Build frontend assets on native builder
0f5a5e2 Widen persisted job text fields
```

The worktree was clean after the last change.

## Completed fixes

### Legal pages and routing

Legal pages are implemented in `frontend/src/App.tsx`:

- `https://jobmatcher.in/privacy-policy`
- `https://jobmatcher.in/terms-of-service`

Routing handles trailing slashes. Previously these URLs fell through to the home page.

### OAuth redirect

Google OAuth is handled by the backend API domain. Successful login now redirects to the configured frontend origin instead of `https://api.jobmatcher.in/`.

Relevant file:

```text
src/main/java/com/honey/jobfetcher/config/SecurityConfig.java
```

The configured value is `FRONTEND_ORIGIN`, normally `https://jobmatcher.in`.

### Google OIDC account provisioning

Google is configured with `openid,profile,email`, so Spring uses OIDC handling. `OidcAccountService` was added and wired into Spring Security.

Relevant files:

```text
src/main/java/com/honey/jobfetcher/service/OidcAccountService.java
src/main/java/com/honey/jobfetcher/service/OAuth2AccountService.java
src/main/java/com/honey/jobfetcher/config/SecurityConfig.java
```

The account upsert logic is shared so a successful Google login creates or updates the local user record.

### ARM64 frontend image build

The production server is ARM64 (`aarch64`). QEMU was causing illegal-instruction failures while compiling Node dependencies/assets.

`frontend/Dockerfile` now builds frontend assets using:

```dockerfile
FROM --platform=$BUILDPLATFORM node:22-bookworm-slim
```

The runtime image remains compatible with the target deployment architecture.

### Job persistence failure

The backend logged:

```text
ERROR: value too long for type character varying(255)
insert into jobs ...
```

`description` was already mapped to `TEXT`, but the live database still had the other job text fields as `varchar(255)`. The entity was updated so externally sourced fields use PostgreSQL `TEXT`:

```text
src/main/java/com/honey/jobfetcher/model/Jobs.java
```

The fields widened are:

- `externalId`
- `title`
- `description`
- `company`
- `location`
- `jobUrl`
- `source`

The full Maven test suite passed after this change.

## Production deployment

Server:

```text
ubuntu@161.118.165.140
```

Deployment directory:

```text
/opt/jobmatcher
```

Compose services:

```text
jobmatcher-postgres-1
jobmatcher-backend-1
jobmatcher-frontend-1
jobmatcher-proxy-1
```

Production Compose uses:

```text
ghcr.io/honey0632/job-fetcher-backend:latest
ghcr.io/honey0632/job-fetcher-frontend:latest
```

Files involved:

```text
compose.production.yaml
Caddyfile
.env
```

`.env` contains secrets and must never be committed or pasted into chat.

The PostgreSQL data volume was preserved. Do not delete it while troubleshooting.

### Live database hotfix already applied

The production `jobs` table columns were changed directly to `TEXT`:

```sql
ALTER TABLE jobs ALTER COLUMN external_id TYPE TEXT;
ALTER TABLE jobs ALTER COLUMN title TYPE TEXT;
ALTER TABLE jobs ALTER COLUMN company TYPE TEXT;
ALTER TABLE jobs ALTER COLUMN location TYPE TEXT;
ALTER TABLE jobs ALTER COLUMN job_url TYPE TEXT;
ALTER TABLE jobs ALTER COLUMN source TYPE TEXT;
```

A transaction-based insert test with long values succeeded and was rolled back.

The backend and other containers were running. An unauthenticated request to `/actuator/health` returned `401`, which confirms the API endpoint is reachable but protected; it is not proof of an unhealthy backend.

## Known warnings and problems

### PDFBox warnings

Warnings such as:

```text
No Unicode mapping for phone in font FontAwesome5Free-Solid
No Unicode mapping for envelope in font FontAwesome5Free-Solid
```

are non-fatal. They may affect extracted text from FontAwesome glyphs in PDFs, but they did not cause the job persistence failure.

### Image deployment status

The source fix was pushed as `0f5a5e2`, but the final GitHub Actions image publication and pull/restart of that exact image were not verified before ending the session.

The production SQL hotfix is already in place, so job inserts should no longer fail even before the new backend image is deployed.

### PostgreSQL credentials

Changing `POSTGRES_PASSWORD` in Compose does not change the password of an already initialized PostgreSQL role. Earlier, the existing `jobfetcher` role password had to be synchronized explicitly without deleting the volume.

## Important code paths

```text
frontend/src/App.tsx
frontend/Dockerfile

src/main/java/com/honey/jobfetcher/config/SecurityConfig.java
src/main/java/com/honey/jobfetcher/model/Jobs.java
src/main/java/com/honey/jobfetcher/parser/GoogleCareersParser.java
src/main/java/com/honey/jobfetcher/service/JobsService.java
src/main/java/com/honey/jobfetcher/service/OAuth2AccountService.java
src/main/java/com/honey/jobfetcher/service/OidcAccountService.java

src/main/resources/application.properties
.github/workflows/ci-cd.yml
compose.production.yaml
Caddyfile
```

`JobsService.upsertJob()` persists parsed Google Careers records. `GoogleCareersParser` extracts the title, URL, company, location, description, source, and external ID.

## Tomorrow's recommended checklist

1. Check GitHub Actions for commit `0f5a5e2`.
2. Confirm the backend and frontend images were published to GHCR.
3. On the server, pull and restart the stack:

   ```bash
   cd /opt/jobmatcher
   docker compose -f compose.production.yaml pull
   docker compose -f compose.production.yaml up -d
   ```

4. Confirm the running backend image digest corresponds to the new build.
5. Test Google login end-to-end:
   - login at `https://jobmatcher.in`
   - confirm redirect returns to the frontend
   - confirm `/api/auth/me` succeeds
   - confirm a user row exists
6. Test job search and confirm records are inserted into `jobs`.
7. Review backend logs for new errors:

   ```bash
   docker logs --tail 200 jobmatcher-backend-1
   ```

8. If job search still fails, log the lengths of parsed fields before `jobsRepository.save()` in `JobsService`, without logging secrets or full job descriptions.

## Validation already completed

Local backend tests:

```text
mvn test -q
```

Result: passed.

The production schema accepted a rollback-only insert containing values longer than 255 characters in every widened job field.

## Safety notes

- Never commit `.env`.
- Never print OAuth client secrets or database passwords.
- Do not remove the PostgreSQL volume.
- Do not use destructive Git commands to discard unrelated work.
- If OAuth credentials were exposed outside the intended environment, rotate them.
