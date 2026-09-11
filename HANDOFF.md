# Job Matcher Handoff

Last updated: 2026-09-11  
Repository: `honey0632/Job-Matcher`  
Branch: `main`

---

## 1. System Architecture Overview

Job Matcher is a full-stack Spring Boot + React platform designed to discover, ingest, store, extract, and match job opportunities against user resumes using AI (Google Gemini 2.5/3.6) and deterministic keyword fallback matching.

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              React Frontend (SPA)                               │
│  - Dashboard Hero & Live Criteria Summary                                       │
│  - Company Side Panel (Google, Amazon, Wells Fargo, NVIDIA)                     │
│  - Focused Single-Company On-Demand Matching                                    │
│  - Dual-Layer Persistent Saved Jobs (PostgreSQL + localStorage cache)           │
│  - Job Cards: Inline Description, Direct Apply, Portal Link, Bookmark ⭐       │
└────────────────────────────────────────┬────────────────────────────────────────┘
                                         │ REST API / Session Auth
┌────────────────────────────────────────▼────────────────────────────────────────┐
│                          Spring Boot Backend (Java 25)                          │
│                                                                                 │
│ ┌───────────────────────────┐    ┌────────────────────────────────────────────┐ │
│ │ Auth & Session Controller │    │ CriteriaSearchService & JobsController     │ │
│ │ - Google OAuth2           │    │ - Bounded on-demand provider scraping      │ │
│ │ - HttpOnly Session Cookie │    │ - Multi-source aggregation & source filter │ │
│ └───────────────────────────┘    └─────────────────────┬──────────────────────┘ │
│                                                        │                        │
│ ┌──────────────────────────────────────────────────────▼──────────────────────┐ │
│ │                     Job Matching Engine (Gemini / Keyword)                  │ │
│ │ - JobLocationMatcher (Country synonym tokens: IND/IN, US/USA, etc.)         │ │
│ │ - Balanced Cross-Provider Candidate Interleaving                            │ │
│ │ - LLM Semantic Batch Scoring (Gemini 2.5/3.6 with 100s HTTP Timeout)        │ │
│ │ - Fallback Keyword Overlap Scoring (Title-weighted term ratio)               │ │
│ └──────────────────────────────────────────────────────┬──────────────────────┘ │
│                                                        │                        │
│ ┌──────────────────────────────────────────────────────▼──────────────────────┐ │
│ │                      Approved Job Sources Strategy Map                      │ │
│ │ ┌───────────────────┐ ┌───────────────┐ ┌───────────────┐ ┌───────────────┐ │ │
│ │ │  Google Careers   │ │  Amazon Jobs  │ │  Wells Fargo  │ │  NVIDIA Jobs  │ │ │
│ │ │  (HTML Script DS) │ │  (JSON API)   │ │  (XML Feed)   │ │  (Sitemap/LD) │ │ │
│ │ └───────────────────┘ └───────────────┘ └───────────────┘ └───────────────┘ │ │
│ └─────────────────────────────────────────────────────────────────────────────┘ │
└────────────────────────────────────────┬────────────────────────────────────────┘
                                         │ JPA / Hibernate
┌────────────────────────────────────────▼────────────────────────────────────────┐
│                             PostgreSQL 18 Database                              │
│ - users, resumes, search_preferences, jobs (unique external_id), saved_jobs     │
└─────────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Approved Job Sources & Ingestion Mechanics

All job sources operate on public, unauthenticated HTTP endpoints with strict safety limits, canonical URL validation, and isolated error boundaries:

| Provider | Ingestion Source | Output Identifier | Safety Limits & Ingestion Policies |
| :--- | :--- | :--- | :--- |
| **Google Careers** | `https://www.google.com/about/careers/applications/jobs/results/` | `GOOGLE_CAREERS:<hash/id>` | HTML script parsing of `projects/gweb-careers-proto` payload. |
| **Amazon Jobs** | `https://www.amazon.jobs/en/search.json` | `AMAZON:<id_icims>` | Sequential pagination up to 3 pages (200 jobs max), 2 MB payload limit, location combination (`normalized_location` + `location`). |
| **Wells Fargo** | `https://www.wellsfargojobs.com/en/jobs/xml/` | `WELLS_FARGO:<referencenumber>` | Unpaged XML feed safely parsed with DTD/external entities disabled (`SafeXmlParser`), 10 MB payload safety limit, 500 records max. |
| **NVIDIA Jobs** | `https://nvidia.wd5.myworkdayjobs.com/.../sitemap.xml` | `NVIDIA:<identifier.value>` | Reads public sitemap XML (10,000 URL limit), filters query-matching job URLs, fetches up to 25 public job pages (1 MB limit), parses embedded `JobPosting` JSON-LD. |

---

## 3. Location Normalization & Candidate Balancing

### Problem: Why Non-Google Jobs Initially Failed to Match
Different job portals format location strings differently:
- **Google Careers:** `"Bengaluru, India"`, `"Hyderabad, India"`
- **Amazon Jobs:** `"Bengaluru, KA, IND"`, `"Seattle, WA, USA"`
- **NVIDIA Jobs:** `"Bengaluru, KA, 560001, IN"`
- **Wells Fargo:** `"Charlotte, NC, US, 28202"`

When the user searched for `country: "India"`, naive substring matching `jobLocation.toLowerCase().contains("india")` discarded all Amazon (`IND`), NVIDIA (`IN`), and Wells Fargo (`US`/`IND`) listings before reaching the match scoring stage.

### The Fix: `JobLocationMatcher` & Balanced Candidate Interleaving
1. **`JobLocationMatcher` (`src/main/java/com/honey/jobfetcher/provider/JobLocationMatcher.java`):**
   - Implements country synonym dictionaries with word-boundary regex tokens (e.g. `\bIND\b`, `\bIN\b` for India; `\bUSA\b`, `\bUS\b` for United States).
   - Matches `"Remote"` or substring matches automatically.
2. **Balanced Cross-Provider Candidate Interleaving:**
   - Instead of picking the first 25 jobs from `jobsRepository.findAll()` (which were predominantly Google jobs), `GeminiJobMatchingService` groups location-matching jobs by provider and interleaves them in round-robin order.
   - When a specific company is selected in the UI (`source: "AMAZON"`), it directly evaluates candidates strictly from that company.

---

## 4. Frontend Architecture & Workflows

### 1. Dashboard Homepage (`view === 'home'`)
- **Hero Card:** Displays user welcome, active target role, country, years of experience, and 1-click navigation buttons (`Search Jobs`, `Saved Roles`, `Update Profile`).
- **Stats Row:** Metric cards showing current desired role, total saved jobs count, and approved providers.
- **Company Focus Grid:** Company cards (Google, Amazon, Wells Fargo, NVIDIA) with individual icons and summaries that open the focused search directly.
- **Top Recommended Roles:** Displays top-scoring match opportunities based on the candidate's active resume.

### 2. Company Side Panel & Single-Company Matching (`view === 'search'`)
- **Side Panel Navigation:** Users can toggle between `All Companies`, `Google Careers`, `Amazon Jobs`, `Wells Fargo`, and `NVIDIA Jobs`.
- **Targeted Execution:** Selecting a company automatically runs search and matching *only* for the open company (`POST /api/jobs/search` with `{ source: "AMAZON", ... }`), eliminating wasteful external API calls and token consumption for unselected companies.

### 3. Persistent Saved Jobs (Dual-Layer Cache)
- **Problem:** Saved jobs would vanish if API calls failed or state reset during tab transitions.
- **Solution:** 
  - Dual-layer storage: Every bookmark action updates PostgreSQL via `POST / DELETE /api/jobs/saved/{jobId}` and instantly syncs with browser `localStorage` (`job-fetcher.saved-jobs.v1`).
  - On page load, initial state is seeded immediately from `localStorage` while fresh server state is retrieved in the background.

### 4. Job Card Actions
- **Save Job ⭐ / Saved:** Toggle bookmark state instantly with visual feedback.
- **Job Description:** Inline expandable accordion displaying full formatted job requirements.
- **Portal ↗ / Apply ↗:** Direct link to original job posting URL.

---

## 5. Detailed File-by-File Summary of Recent Changes

### Core Configuration & Services
1. **`src/main/java/com/honey/jobfetcher/config/RestClientConfig.java`**
   - Configured JDK `HttpClient` request factory for Spring `RestClient`.
   - Set `CONNECT_TIMEOUT = Duration.ofSeconds(10)` and `READ_TIMEOUT = Duration.ofSeconds(100)` to handle heavy Gemini AI evaluations without connection drop.

2. **`src/main/java/com/honey/jobfetcher/provider/JobLocationMatcher.java`** (New File)
   - Handles multi-country synonym dictionaries (`IND`, `IN`, `USA`, `US`, `UK`, `GB`, etc.) and word boundary regex matching so non-Google listings match user country criteria.

3. **`src/main/java/com/honey/jobfetcher/service/JobMatchingService.java`**
   - Extended interface contract with `findMatches(Long resumeId, int limit, String location, String source)`.

4. **`src/main/java/com/honey/jobfetcher/service/GeminiJobMatchingService.java`**
   - Integrated `JobLocationMatcher` for location filtering.
   - Added `matchesSource(...)` and `selectBalancedCandidates(...)` for fair multi-provider representation in LLM prompts.
   - Robust try-catch fallback around `requestScores(resume, candidates)`: falls back to title-weighted keyword scoring on upstream HTTP 503 or quota limits.

5. **`src/main/java/com/honey/jobfetcher/service/KeywordJobMatchingService.java`**
   - Updated to support `source` filtering and `JobLocationMatcher`.
   - Title-weighted overlap scoring relative to job term count rather than total resume length.

6. **`src/main/java/com/honey/jobfetcher/service/JobsService.java`**
   - Added `fetchAndSaveForSource(JobSource source, String query)` for single-source queries.
   - Resilient try-catch in `fetchAndSaveApprovedJobs(query)`: logs warnings and continues when individual providers fail.

7. **`src/main/java/com/honey/jobfetcher/service/CriteriaSearchService.java`**
   - Supports source-filtered fetching (`fetchAndSaveForSource`) and passes `sourceFilter` down to `jobMatchingService.findMatches(...)`.

### Job Source Clients, Parsers & Providers
8. **`src/main/java/com/honey/jobfetcher/parser/AmazonJobsParser.java`**
   - Combines `normalized_location` (e.g. `"Bengaluru, KA, IND"`) and `location` (e.g. `"India"`) into a unified location string.

9. **`src/main/java/com/honey/jobfetcher/client/WellsFargoJobsClient.java`**
   - Set `MAX_RESPONSE_CHARACTERS = 10_000_000` (10 MB) to safely accommodate live XML feed size (~3.5 MB).

10. **`src/main/java/com/honey/jobfetcher/parser/SafeXmlParser.java`**
    - Secure XML parser wrapper with disabled DTD and external entity resolution.

11. **`src/main/java/com/honey/jobfetcher/provider/AmazonJobProvider.java`**, **`GoogleCareersJobProvider.java`**, **`NvidiaJobProvider.java`**, **`WellsFargoJobProvider.java`**
    - Source-specific provider adapters adhering to `JobProvider` interface.

### Frontend (`frontend/src/`)
12. **`frontend/src/App.tsx`**
    - Implemented dashboard hero banner and profile summary.
    - Added company search panel with focused single-company trigger.
    - Added `localStorage` dual-layer caching for saved jobs.
    - Updated company filter buttons to prevent invalid nesting.

13. **`frontend/src/styles.css`**
    - Added CSS styles for `.homepage-hero`, `.hero-summary`, `.company-card-button`, `.company-status`, `.saved-btn`, and responsive layouts. Fixed media query syntax.

14. **`frontend/src/api.ts`**
    - Type definitions and REST methods for criteria search with optional `source` parameter, saved jobs CRUD, and CSRF handling.

### Comprehensive Test Suite
15. **`src/test/java/com/honey/jobfetcher/provider/JobLocationMatcherTest.java`** (New File)
    - Verifies country token matching for India (`IND`, `IN`), United States (`USA`, `US`), remote roles, and rejection of mismatched locations.
16. **`src/test/java/com/honey/jobfetcher/service/CriteriaSearchServiceTest.java`**
    - Verifies score > 80 filtering and source-targeted search delegation.
17. **`src/test/java/com/honey/jobfetcher/service/KeywordJobMatchingServiceTest.java`**
    - Verifies keyword scoring and source/location filtering.
18. **`src/test/java/com/honey/jobfetcher/parser/AmazonJobsParserTest.java`**
    - Verifies Amazon JSON mapping and combined location parsing.
19. **`src/test/java/com/honey/jobfetcher/service/JobsServiceTest.java`**
    - Verifies resilient provider iteration when a provider fails.

---

## 6. Production Operations & Incident History

### Incident 1: Wells Fargo XML Safety Limit (Pushed in `8b5499c`)
- **Symptom:** `IllegalStateException: Wells Fargo Jobs response exceeds the 2 MB safety limit` causing HTTP 500.
- **Fix:** Increased `MAX_RESPONSE_CHARACTERS` to 10 MB in `WellsFargoJobsClient.java` and wrapped provider iteration in `JobsService` with try-catch.

### Incident 2: Gemini Read Timeout (Pushed in `0bc0e7a`)
- **Symptom:** `HttpTimeoutException: Request cancelled` during batch LLM evaluation of 25 jobs.
- **Fix:** Increased `READ_TIMEOUT` in `RestClientConfig.java` to 100 seconds (`Duration.ofSeconds(100)`).

### Incident 3: 0 Jobs Returned Under Keyword Fallback (Pushed in `3cf2754`)
- **Symptom:** Sub-80% keyword scores filtered out all results when Gemini was unavailable.
- **Fix:** Refined keyword overlap formula to weight required job keywords and title terms, and implemented graceful Gemini 503 fallback.

### Incident 4: Non-Google Portal Matching Exclusion (Pushed in `db79280`)
- **Symptom:** Only Google jobs appeared in search results; Amazon/NVIDIA jobs were omitted.
- **Fix:** Created `JobLocationMatcher` for multi-country code parsing (`IND`/`IN`), added balanced candidate selection in `GeminiJobMatchingService`, and combined location fields in `AmazonJobsParser`.

---

## 7. Production Deployment, Environment Variables & Remote Access

### Environment Variables (`.env` / `application.properties`)
```properties
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/jobfetcher
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres

# OAuth & Security
GOOGLE_CLIENT_ID=your-google-oauth-client-id
GOOGLE_CLIENT_SECRET=your-google-oauth-client-secret
FRONTEND_ORIGIN=http://localhost:5173

# Job Matching & AI
app.matching.provider=gemini
app.gemini.api-key=your-gemini-api-key
app.gemini.model=gemini-2.5-flash

# Approved Sources
app.job-sources.enabled=GOOGLE_CAREERS,AMAZON,WELLS_FARGO,NVIDIA
```

### Server & Deployment Paths
- **Local Working Copy:** `D:\Projects\Job Fetcher`
- **SSH Private Key:** `D:\oraclekeys\job-fetcher\ssh-key-2026-09-06.key`
- **Production Server:** `ubuntu@161.118.165.140`
- **Production Directory:** `/opt/jobmatcher`
- **Production Compose File:** `/opt/jobmatcher/compose.production.yaml`
- **Reverse Proxy Caddyfile:** `/opt/jobmatcher/Caddyfile`

### Useful Production Management Commands
```bash
# Connect to production server
ssh -i "D:\oraclekeys\job-fetcher\ssh-key-2026-09-06.key" ubuntu@161.118.165.140

# Check container status
cd /opt/jobmatcher
docker compose -f compose.production.yaml ps

# Pull and redeploy latest image
docker compose -f compose.production.yaml pull
docker compose -f compose.production.yaml up -d

# Tail backend application logs
docker logs --tail 200 -f jobmatcher-backend-1
```

---

## 8. Build & Verification Commands

```powershell
# Run backend test suite (30 unit & integration tests)
.\mvnw.cmd test

# Run frontend build (TypeScript + Vite)
Set-Location frontend
npm run build
```

Production container data paths:

```text
/var/lib/docker/volumes/jobmatcher_postgres-data/_data
/var/lib/docker/volumes/jobmatcher_caddy-data/_data
/var/lib/docker/volumes/jobmatcher_caddy-config/_data
```

Do not delete the PostgreSQL volume or its data directory.

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
008a724 Restore production match threshold
```

Recent relevant commits:

```text
008a724 Restore production match threshold
eb9d9c0 Complete job action response support
d0afddc Add job portal action button
8b249d9 Fix OAuth legal page routing
eb45dfb Redirect OAuth login to frontend
02dc49f Provision users for Google OIDC login
b6553da Build frontend assets on native builder
0f5a5e2 Widen persisted job text fields
```

The worktree was clean after the last change.

## Latest session: Java 25 LTS runtime upgrade

The backend runtime target was upgraded from Java 21 to Java 25 LTS on 2026-09-11.

- `pom.xml` now sets `java.version` to `25`.
- `Dockerfile` uses Eclipse Temurin 25 for both the Maven build stage and JRE runtime stage.
- JDK 25.0.2 was installed locally at `C:\Users\honey kumar\AppData\Local\jdks\jdk-25.0.2`.
- `./mvnw.cmd clean test-compile -q`, `./mvnw.cmd clean test -q`, and `./mvnw.cmd clean verify` succeeded with Java 25.
- The Maven test suite passed: 30 tests, 0 failures, 0 errors.
- A direct-dependency CVE scan found no known issues.

The test run emitted non-failing future-compatibility warnings: current Lombok code uses a deprecated `sun.misc.Unsafe` path, and Mockito dynamically attaches its Java agent. Monitor their next dependency releases before a JDK default blocks those behaviors.

### Follow-up: XML parser unit-test coverage

Direct unit coverage was added for `src/main/java/com/honey/jobfetcher/parser/SafeXmlParser.java` in `src/test/java/com/honey/jobfetcher/parser/SafeXmlParserTest.java`.

- Covers valid namespaced XML parsing, empty input rejection, malformed XML rejection, and DTD/external-entity rejection.
- The focused test class passed under JDK 25.0.2.
- The complete Maven suite passed after the addition: 34 tests, 0 failures, 0 errors, 0 skipped.

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
.github/workflows/backend-ci.yml
.github/workflows/backend-cd.yml
.github/workflows/frontend-ci.yml
.github/workflows/frontend-cd.yml
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

## Latest change: independent frontend and backend CI/CD

Commit:

```text
e71f010 Split frontend and backend CI/CD
```

The previous combined workflow was removed:

```text
.github/workflows/ci-cd.yml
```

It was replaced with four workflows:

```text
.github/workflows/backend-ci.yml
.github/workflows/backend-cd.yml
.github/workflows/frontend-ci.yml
.github/workflows/frontend-cd.yml
```

### Backend CI

`backend-ci.yml` runs only when backend-related files change, including
`src/**`, `pom.xml`, `mvnw`, `.mvn/**`, `Dockerfile`, or backend workflow files.
It starts PostgreSQL 16 as a GitHub Actions service and runs:

```bash
./mvnw -B test
```

### Frontend CI

`frontend-ci.yml` runs only when `frontend/**` or frontend workflow files change.
It installs Node.js 22 dependencies with `npm ci` and runs the production build
using the repository variable `JOBMATCHER_VITE_API_BASE_URL`, with
`https://api.jobmatcher.in` as the fallback.

### Backend CD

`backend-cd.yml` starts after a successful `Backend CI` workflow on `main`.
It can also be started manually with `workflow_dispatch`.

The backend CD workflow checks out the exact commit that passed backend CI,
builds and pushes only the backend image, publishes both `latest` and
`sha-<commit>` tags to GHCR, then pulls and restarts only the backend service:

```bash
IMAGE_TAG=sha-<commit> docker compose -f compose.production.yaml pull backend
IMAGE_TAG=sha-<commit> docker compose -f compose.production.yaml up -d --no-deps backend
```

It does not rebuild or restart the frontend, proxy, or PostgreSQL service.

### Frontend CD

`frontend-cd.yml` starts after a successful `Frontend CI` workflow on `main`.
It can also be started manually with `workflow_dispatch`.

The frontend CD workflow builds and publishes only the frontend image for both
`linux/amd64` and `linux/arm64`, then pulls and restarts only the frontend:

```bash
IMAGE_TAG=sha-<commit> docker compose -f compose.production.yaml pull frontend
IMAGE_TAG=sha-<commit> docker compose -f compose.production.yaml up -d --no-deps frontend
```

### Required GitHub configuration

Repository secrets:

```text
JOBMATCHER_PRODUCTION_HOST
JOBMATCHER_PRODUCTION_USER
JOBMATCHER_PRODUCTION_SSH_KEY
```

Repository variable:

```text
JOBMATCHER_VITE_API_BASE_URL
```

The SSH key secret must contain the complete private key. Do not put its value
in this file or in the repository. The production server must already be
authenticated to GHCR so Docker Compose can pull the private images.

The workflow files were checked with YAML parsing and `git diff --check`.
The commit was pushed successfully to `origin/main`. `HANDOFF.md` was not part
of commit `e71f010`; this handoff update is the next pending repository change.

## Backend deployment speed optimization

The backend CD workflow was optimized for the production Oracle VM, which is
ARM64 (`aarch64`):

- Backend CD now publishes only `linux/arm64` instead of building both
  `linux/amd64` and `linux/arm64`.
- GitHub Actions BuildKit cache was enabled with `cache-from: type=gha` and
  `cache-to: type=gha,mode=max`.
- The Maven builder stage in `Dockerfile` now uses
  `FROM --platform=$BUILDPLATFORM`, so Maven and Java compilation run on the
  GitHub runner architecture instead of under ARM64 emulation.
- The ARM64 runtime image remains unchanged and is still used by production.

The QEMU setup step remains because the runtime stage still performs its
target-platform filesystem setup during the image build, but the expensive
Maven compilation is no longer emulated.

Only `.github/workflows/backend-cd.yml` and `Dockerfile` were pushed for this
optimization in commit:

```text
c984665 Speed up ARM64 backend deployment
```

`HANDOFF.md` remains local and ignored by Git.

## Match threshold temporarily lowered for testing

The criteria search threshold was changed from scores above 80% to scores
above 20% so more jobs are visible during testing.

Updated surfaces:

```text
src/main/java/com/honey/jobfetcher/service/CriteriaSearchService.java
src/main/java/com/honey/jobfetcher/controller/JobMatchingController.java
frontend/src/api.ts
frontend/src/App.tsx
src/test/java/com/honey/jobfetcher/service/CriteriaSearchServiceTest.java
```

The criteria search now filters with `score > 20`. The `/api/jobs/matches`
default threshold and frontend request also use 20. The UI labels now say
`20%+` and `above 20%`.

Validation completed:

```text
./mvnw.cmd -q test       passed
frontend: npm run build  passed
```

This is a testing change. Restore the threshold to 80 before production
behavior is finalized if high-confidence-only matching is required.

## Match threshold changed to negative testing mode

The local working tree now uses a threshold of `-1` for testing:

- Criteria search filters with `score > -1`.
- `/api/jobs/matches` defaults to `-1`.
- The frontend requests `/api/jobs/matches?threshold=-1`.
- The controller accepts thresholds from `-100` through `100`.
- The UI states that all positive-scoring matches are shown.

The keyword matcher itself only creates matches with scores above zero, so
`-1` effectively allows every match produced by the matcher to be displayed.
These changes were pushed in commit:

```text
2f4bf84 Show all positive matches for testing
```

`HANDOFF.md` itself remains local and ignored.

## Gemini matching and location filtering implementation

The working tree now contains an unpushed implementation for:

1. Filtering search results by the requested country/location.
2. Including jobs marked `remote`.
3. Replacing exact keyword scoring with optional Gemini scoring.

Changed backend surfaces:

```text
src/main/java/com/honey/jobfetcher/service/JobMatchingService.java
src/main/java/com/honey/jobfetcher/service/KeywordJobMatchingService.java
src/main/java/com/honey/jobfetcher/service/GeminiJobMatchingService.java
src/main/java/com/honey/jobfetcher/service/CriteriaSearchService.java
src/main/java/com/honey/jobfetcher/controller/JobMatchingController.java
src/main/resources/application.properties
pom.xml
```

`GeminiJobMatchingService` is enabled only when:

```text
MATCHING_PROVIDER=gemini
```

It reads:

```text
GEMINI_API_KEY
GEMINI_MODEL=gemini-2.5-flash
```

The backend sends the extracted resume and up to 25 location-filtered jobs to
Gemini and expects JSON scores from 0 to 100. The API key remains server-side.
The keyword matcher remains available when `MATCHING_PROVIDER=keyword` or when
the provider setting is absent.

The `/api/jobs/matches` endpoint now accepts an optional `location` parameter.
Criteria search passes the selected country to the matcher automatically.

Validation completed after implementation:

```text
./mvnw.cmd -q test       passed
frontend: npm run build  passed
```

These changes were committed and pushed in:

```text
1235ddc Add Gemini matching and location filtering
```

The Gemini API key remains only in the local/server environment files.

## Gemini production log finding and local fix

After commit `1235ddc` was deployed, the backend started successfully but a
search request failed at the Gemini API with:

```text
Invalid JSON payload received.
Unknown name "array"
Unknown name "bigDecimal"
...
```

The cause was that `RestClient` serialized Jackson's `JsonNode` request body
as bean properties instead of sending raw JSON. The local fix changes the
Gemini client to:

- Send `body.toString()` with `Content-Type: application/json`.
- Receive the Gemini response as a raw string.
- Parse the response explicitly with `ObjectMapper`.

Local validation after the fix:

```text
./mvnw.cmd -q test       passed
frontend: npm run build  passed
```

The fix is not committed or pushed yet. Production remains on the previous
Gemini integration until the fix is approved and deployed.

## Code comments

Meaningful comments were added locally to the matching implementation,
Gemini integration, controller, configuration, and related tests. Comments
document intent, provider selection, location filtering, batching, and the
explicit JSON parsing fix without adding line-by-line noise.

These comment-only changes remain uncommitted and unpushed.

The project-wide documentation pass covered all 56 Java source/test files, all
5 frontend source files, and all 4 GitHub Actions workflow files. Generated
artifacts, dependencies, lockfiles, secrets, and build output were excluded.
Backend tests, frontend build, and `git diff --check` passed.

The documentation pass and Gemini JSON transport fix were pushed in:

```text
de5aeb1 Document project code and fix Gemini JSON
```

`HANDOFF.md` remains local and ignored.

## Worktree rollback and source review: 2026-09-11

The uncommitted local changes present after commit `008a724` were deliberately
discarded at the user's request. This removed the pending `pom.xml` test-source
directory change, the Gemini Markdown-fence parsing change, and the untracked
relocated `test/java` test tree. The tracked worktree is restored exactly to
`008a724` (`Restore production match threshold`); no code was pushed.

A complete source review confirmed the current flow: React/Vite frontend,
Google OAuth2 session authentication, preference and resume upload, PDF/DOCX
text extraction, Google Careers fetching and upsert, then keyword or Gemini
matching backed by PostgreSQL.

Known follow-up inconsistencies:

- The frontend and criteria search still present/use an 80% threshold, despite
  the current handoff stating that the intended production threshold is 0%.
- Failed resume extraction retains the uploaded file and failed database row.
- Google Careers fetching/parsing has no explicit timeout and depends on
  fragile HTML indexes.
- Job status is global rather than scoped to a user.
- Backend CI/CD publishes ARM64 only, while the README says published images
  target both `linux/amd64` and `linux/arm64`.

## Match threshold aligned at 80%: 2026-09-11

The product threshold is fixed at scores strictly greater than 80%.

- Criteria search and `/api/jobs/matches` both filter out scores of exactly 80.
- `/api/jobs/matches` no longer accepts a caller-controlled threshold, so it
  cannot expose lower-scoring matches.
- The frontend no longer sends a threshold parameter, and the README documents
  the updated endpoint.
- `JobMatchingControllerTest` verifies that an 81% match is included and an
  80% match is excluded.

This change is local and has not been pushed.

## First company-source cohort assessment: 2026-09-11

The requested first cohort is Amazon, Microsoft, D. E. Shaw, NVIDIA, and Wells
Fargo. No source-integration code has been added yet.

- Amazon exposes a verified public JSON jobs feed and is suitable for a
  read-only integration.
- Wells Fargo exposes a verified public XML jobs feed and is suitable for a
  read-only integration.
- NVIDIA exposes a robots-permitted sitemap and public JobPosting JSON-LD;
  this is a controlled fallback, not a documented jobs API.
- Microsoft blocks direct access to its careers API and has no verified
  supported server-to-server feed. Do not emulate browser access or bypass the
  restriction; obtain written permission or a supported feed.
- D. E. Shaw's robots policy explicitly disallows its open-roles path. Do not
  automate it without written permission or a publisher-provided feed.

All integrations must remain read-only, use explicit timeouts and host
allowlists, preserve canonical employer job links, and avoid collecting
application data or credentials.

## First cohort decision update: 2026-09-11

Microsoft is deferred. The user requested D. E. Shaw without an upstream
timestamp, but timestamp handling is unrelated to the site's explicit robots
restriction on automated open-roles access. D. E. Shaw remains blocked until
written permission or a publisher-provided feed is available. No source code
was added in this decision update.

## Job action buttons

The job result cards now include:

1. `Job Description` — toggles the stored job description inline.
2. `Apply Now` — opens the external job URL in a new tab.

The frontend now also shows `View on Job Portal`. Both external buttons use
the currently available Google Careers detail URL because the parser does not
yet receive a separate application URL. The frontend build passed. This
change was pushed in commits `d0afddc` and `eb9d9c0`.

`JobMatchResponse` now includes the job description so the frontend can render
it without another request. Backend tests and the frontend production build passed. These changes were
pushed in commits `d0afddc` and `eb9d9c0`.

## Historical testing threshold — superseded

The threshold was changed locally from `0` back to `-1` for testing. Scores
greater than `-1` are eligible, including zero-score Gemini results.
The Gemini matcher now retains scores greater than or equal to zero.

Validation passed:

```text
./mvnw.cmd -q test
frontend: npm run build
```

These historical testing changes were committed and pushed in:

```text
98bf892 Include zero score matches for testing
```

`HANDOFF.md` remains local and ignored.

The current production threshold is documented in the latest completed-work
section above and was restored in commit `008a724`.

## Gemini model error: 2026-09-07 22:12 IST

The deployed backend image is current and all containers are running. A real
Search Jobs request reached Gemini, but Gemini returned HTTP 404:

```text
This model models/gemini-2.5-flash is no longer available to new users.
Please update your code to use models/gemini-3.6-flash.
```

The production `.env` currently contains:

```text
GEMINI_MODEL=gemini-2.5-flash
```

The backend must be updated to use an available Gemini model, and the server
`.env` must be changed accordingly before retesting. No secret values were
logged or copied.

## Gemini availability error: 2026-09-07 22:32 IST

After deployment of commit `98bf892`, all production containers are healthy and
the server is configured with:

```text
MATCHING_PROVIDER=gemini
GEMINI_MODEL=gemini-3.6-flash
```

Search requests now reach the configured model, but Gemini returns HTTP 503:

```text
This model is currently experiencing high demand.
Spikes in demand are usually temporary. Please try again later.
```

This is an upstream Gemini availability problem, not a location or match
threshold problem. Immediate testing options are to retry later, temporarily
use `MATCHING_PROVIDER=keyword`, or configure another available Gemini model.

## Validation run: 2026-09-07 22:03 IST

Local validation passed:

```text
./mvnw.cmd -q test
frontend: npm run build
```

Public production checks passed:

```text
https://jobmatcher.in                 HTTP 200
https://api.jobmatcher.in/actuator/health  HTTP 401
```

The production containers are all running, and recent backend logs show a
successful Spring Boot startup with no recent errors:

```text
jobmatcher-backend-1
jobmatcher-frontend-1
jobmatcher-postgres-1
jobmatcher-proxy-1
```

The server `.env` contains `MATCHING_PROVIDER=gemini` and
`GEMINI_MODEL=gemini-2.5-flash`. However, the running backend image metadata
still reports revision `0f5a5e2`, so the newer Gemini/location implementation
and JSON transport fix are not yet running in production. An authenticated
end-to-end Search Jobs test must be repeated after the current image is
deployed.

Validation completed:

```text
./mvnw.cmd -q test       passed
frontend: npm run build  passed
```

## Production outage recovery: 2026-09-07

At approximately 20:52 IST, both production domains initially failed at the
transport layer:

```text
https://jobmatcher.in
https://api.jobmatcher.in
```

Server inspection showed that only the backend and frontend containers existed.
The backend was restart-looping because its Docker network could not resolve
the `postgres` hostname:

```text
java.net.UnknownHostException: postgres
```

The `postgres` and `proxy` containers were absent even though both services
were still declared in `/opt/jobmatcher/compose.production.yaml`.

Recovery was performed over SSH with:

```bash
cd /opt/jobmatcher
docker compose -f compose.production.yaml up -d postgres proxy
```

This recreated/started the missing PostgreSQL, backend, frontend, and Caddy
containers through Compose dependencies. The named PostgreSQL volume was not
deleted or replaced.

Post-recovery status:

- `jobmatcher-postgres-1`: running
- `jobmatcher-backend-1`: running
- `jobmatcher-frontend-1`: running
- `jobmatcher-proxy-1`: running
- `https://jobmatcher.in`: HTTP 200
- `https://api.jobmatcher.in/actuator/health`: HTTP 401, expected because the
  endpoint is protected

Caddy successfully loaded its certificates for both `jobmatcher.in` and
`api.jobmatcher.in`. The backend connected to PostgreSQL 16 and completed
Spring Boot startup successfully.

## Match threshold corrected to 0%

The temporary testing threshold is now `0`, not `-1`.

- Criteria search returns scores strictly above 0%.
- `/api/jobs/matches` defaults to a threshold of 0.
- The frontend requests `threshold=0`.
- The controller validates thresholds from 0 through 100.
- UI text states that matches above 0% are shown.

These changes were committed and pushed in:

```text
68c4fb4 Set match threshold to zero
```

`HANDOFF.md` remains local and ignored.

## UX Enhancements: Homepage Dashboard, Persistent Saved Jobs, & Company Search Side Panel (September 2026)

### Overview of Changes
To address user issues where search queries returned 0 results, jobs disappeared on navigation, and running all 4 external provider clients at once caused slowness, a major overhaul was implemented across backend services and React frontend components.

### 1. Provider Query Clean-up & Targeted Matching
- **Problem**: `CriteriaSearchService` constructed query strings by appending `" 2 years experience"` to the role. External search endpoints (Google Careers, Amazon, Wells Fargo, NVIDIA) treated this as keyword requirements and returned zero search results.
- **Fix**: Updated `CriteriaSearchService.java` to use clean desired role queries (`request.desiredRole().trim()`). The user's experience level and country are now evaluated during AI/keyword matching, resulting in rich job results from external provider APIs.

### 2. Company Side Panel & Single-Provider Match Control
- **Backend**:
  - `dto/JobSearchRequest.java`: Added an optional `source` parameter (`GOOGLE_CAREERS`, `AMAZON`, `WELLS_FARGO`, `NVIDIA`, or `ALL`).
  - `dto/JobMatchResponse.java`: Added `source` field and constructor support to return the provider origin on matched job cards.
  - `service/JobsService.java`: Added `fetchAndSaveForSource(JobSource source, String query)` to query a single external provider instead of iterating all 4 providers sequentially.
  - `service/CriteriaSearchService.java`: Added company/source filtering logic so when a user selects a company, search and match operations run exclusively for that provider.
- **Frontend**:
  - `frontend/src/App.tsx`: Added a Company Panel to the Search view with company-specific icons and launch buttons (Google Careers 🔵, Amazon 🟧, Wells Fargo 🔴, NVIDIA 🟢, All Companies 🌐).
  - Selecting a company runs fast, targeted searches for that specific source.

### 3. Persistent User Saved / Bookmarked Jobs
- **Backend Model & Persistence**:
  - `model/SavedJob.java`: JPA entity storing user ID, job ID, match score, and timestamp with unique `(user_id, job_id)` constraint.
  - `repository/SavedJobRepository.java`: JPA repository supporting saved job queries, existence checks, and transactional deletion.
  - `service/SavedJobService.java`: Business service managing saving, unsaving, and retrieving saved jobs and job ID sets per authenticated user.
  - `controller/JobsController.java`: Added REST endpoints:
    - `GET /api/jobs/saved`: Returns user's saved jobs list.
    - `GET /api/jobs/saved/ids`: Returns saved job IDs for fast frontend badge state lookup.
    - `POST /api/jobs/saved/{jobId}`: Saves/bookmarks a job.
    - `DELETE /api/jobs/saved/{jobId}`: Removes a saved job.
- **Frontend UI Integration**:
  - `frontend/src/api.ts`: Added typed methods for `getSavedJobs`, `getSavedJobIds`, `saveJob`, and `unsaveJob`.
  - `frontend/src/App.tsx`: Added "Save Job ⭐" / "Saved ⭐" toggle buttons to all job cards across Homepage, Search, Matches, and Saved Jobs views.
  - Created a dedicated "Saved Jobs" sidebar view (`SavedJobs` component) where bookmarked roles persist across sessions and page refreshes.

### 4. Interactive Homepage / Dashboard
- `frontend/src/App.tsx`: Replaced initial default view with a full-featured Dashboard Homepage (`Home` component):
  - Personal welcome header (`Welcome back, [User]! 👋`).
  - Stat cards summarizing Target Role & Location, Saved Jobs count, and Approved Sources.
  - Quick-launch Company Search grid.
  - Top Recommended Matches preview pre-loaded directly on the dashboard.
- `frontend/src/styles.css`: Added responsive CSS classes (`.dashboard-grid`, `.stat-card`, `.company-grid`, `.company-card`, `.company-side-panel`, `.saved-btn`, `.badge-company`).

### Comprehensive File-by-File Summary
1. `src/main/java/com/honey/jobfetcher/model/SavedJob.java`: New JPA entity mapping `saved_jobs` table.
2. `src/main/java/com/honey/jobfetcher/repository/SavedJobRepository.java`: New Spring Data repository interface for user saved jobs.
3. `src/main/java/com/honey/jobfetcher/service/SavedJobService.java`: New service for bookmarking & retrieving saved job lists.
4. `src/main/java/com/honey/jobfetcher/dto/JobSearchRequest.java`: Updated to include optional `source` field.
5. `src/main/java/com/honey/jobfetcher/dto/JobMatchResponse.java`: Updated to include `source` field and constructor.
6. `src/main/java/com/honey/jobfetcher/service/JobsService.java`: Added `fetchAndSaveForSource` method.
7. `src/main/java/com/honey/jobfetcher/service/CriteriaSearchService.java`: Cleaned search query format and added provider source filtering.
8. `src/main/java/com/honey/jobfetcher/controller/JobsController.java`: Exposed `/api/jobs/saved*` endpoints and injected `SavedJobService`.
9. `src/test/java/com/honey/jobfetcher/controller/JobsControllerTest.java`: Updated test instantiations for `JobsController`.
10. `frontend/src/api.ts`: Added saved job API client methods and updated `Preferences` and `Match` types.
11. `frontend/src/App.tsx`: Implemented Homepage Dashboard, Company Side Panel, Saved Jobs state, and job card bookmarking buttons.
12. `frontend/src/styles.css`: Added styles for Dashboard, Company Panel, and Saved Job badges/buttons.

### Verification Performed
- `./mvnw.cmd test`: All 27 backend unit and integration tests passed.
- `cd frontend; npm run build`: Vite & TypeScript production compilation succeeded without errors.

