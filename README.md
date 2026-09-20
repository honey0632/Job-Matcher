# Job Fetcher (LLM-Optimized Architecture Guide)

> **Context for AI Agents & LLMs**: This document describes the full-stack architecture, key domain models, data flows, and configuration for the **Job Fetcher** repository. Use this as the definitive structural map when refactoring, adding features, or debugging.

---

## 1. High-Level Architectural Overview

Job Fetcher is a full-stack platform comprising:
1. **Backend**: Spring Boot 4.1.1 running on **Java 25**, Spring Data JPA, Spring Security (OAuth2 / OIDC), and PostgreSQL 18+.
2. **Frontend**: React 18 SPA built with Vite, Tailwind CSS, and TypeScript, adhering to a **Feature-Sliced Architecture**.
3. **Infrastructure**: Docker Compose, Caddy reverse proxy, and GitHub Actions CD pipelines targeting ARM64 Oracle Cloud VMs (`ghcr.io/honey0632/job-fetcher-*`).

### Core Pipeline Data Flow
```
User (Browser) 
  ──(HttpOnly Cookie / CSRF)──► Spring Boot REST Controllers
                                      │
        ┌─────────────────────────────┴─────────────────────────────┐
        ▼                                                           ▼
 Resume Uploads (PDF/DOCX)                              Job Search & Aggregation
   - Extractor: PDFBox / POI                              - Approved Sources:
   - Stored under data/resumes/                             Google, Amazon, Wells Fargo, NVIDIA
                                                                    │
                                                                    ▼
                                                        Job Matching Engine
                                                          - Location Normalization (`JobLocationMatcher`)
                                                          - Gemini AI Semantic Scoring / Keyword Fallback
                                                          - PostgreSQL Persistence (`jobs`, `saved_jobs`)
```

---

## 2. Key Package Structure

### Backend (`src/main/java/com/honey/jobfetcher`)
- **`client/`**: External HTTP clients using Spring `RestClient` with explicit timeouts (`RestClientConfig`).
- **`config/`**: Security configuration (`SecurityConfig`), OAuth2/OIDC account provisioning (`OAuth2AccountService`, `OidcAccountService`), and HTTP client beans.
- **`controller/`**: REST endpoints (`AuthController`, `JobsController`, `ProfileController`, `ResumeController`).
- **`dto/`**: Request/response DTO records (`JobSearchRequest`, `JobMatchResponse`, etc.).
- **`exception/`**: Explicit application error handlers.
- **`extractor/`**: `PdfDocxTextExtractor` wrapping Apache PDFBox and Apache POI.
- **`model/`**: JPA Entities (`User`, `Resume`, `SearchPreferences`, `Jobs`, `SavedJob`).
- **`parser/`**: Parsers for external feeds (`GoogleCareersParser`, `AmazonJobsParser`, `SafeXmlParser` for Wells Fargo, JSON-LD parser for NVIDIA).
- **`provider/`**: `JobProvider` implementations and `JobLocationMatcher` (regex token synonyms for multi-country matching like `IND`/`IN`, `USA`/`US`).
- **`repository/`**: Spring Data JPA repositories.
- **`service/`**: Business logic, including `CriteriaSearchService`, `JobsService`, `GeminiJobMatchingService`, and `KeywordJobMatchingService`.

### Frontend (`frontend/src/`)
Structured according to **Feature-Sliced Design (FSD)**:
- **`app/`**: Root shells, constants (`constants.ts`), and legal page wrappers (`Legal.tsx`).
- **`components/ui/`**: Domain-agnostic atomic UI primitives (`Button`, `Card`, `Badge`, `Input`) designed with modern Tailwind styling (zinc-950 canvas, inner glows, subtle backdrop blurs).
- **`features/`**: Business modules structured into `components/`, `hooks/`, `types/`:
  - `auth/`: Google OAuth / OIDC landing page and session hooks (`useAuth`).
  - `jobs/`: Job cards (`JobCard`), skeletons (`JobSkeleton`), list management (`JobList`), dashboard (`Home`), search panel (`Search`), AI recommendations (`Matches`), and bookmarking logic (`useSavedJobs`).
  - `profile/`: Search parameters configuration (`Profile`) and preferences hook (`usePreferences`).
  - `resume/`: PDF/DOCX file extraction UI (`Resume`).
- **`lib/`**: Centralized utilities:
  - `api-client.ts`: Typed fetch wrapper handling CSRF headers (`X-XSRF-TOKEN`) and session credentials.
  - `utils.ts`: Tailwind merge helper (`cn()`).

---

## 3. Critical Domain Rules & Technical Gotchas

1. **Java 25 Runtime**:
   - `pom.xml` sets `<java.version>25</java.version>`.
   - `Dockerfile` uses Eclipse Temurin 25 for both multi-stage builds and runtime.
2. **Job Sources & Identification**:
   - Ingested jobs are upserted by `externalId` to prevent duplicates.
   - Non-Google providers prefix their IDs (e.g., `AMAZON:id`, `WELLS_FARGO:ref`, `NVIDIA:id`).
3. **Location Filtering (`JobLocationMatcher`)**:
   - Substring matching fails across diverse employer formats. `JobLocationMatcher` uses word-boundary regex tokens (e.g. `\bIND\b`, `\bIN\b`) to correctly filter jobs against the user's target country while always including remote roles.
4. **Matching Providers**:
   - Configured via `app.matching.provider` (`keyword` or `gemini`).
   - `GeminiJobMatchingService` sends batches of location-filtered jobs to Google Gemini (`gemini-2.5-flash` / `gemini-3.6-flash`) with a 100-second read timeout (`RestClientConfig`), falling back gracefully to title-weighted keyword overlap on failure.
5. **Saved Jobs Dual-Layer Cache**:
   - Frontend (`useSavedJobs` hook) syncs bookmarks immediately with browser `localStorage` (`job-fetcher.saved-jobs.v1`) while persisting updates asynchronously to `/api/jobs/saved/{jobId}`.

---

## 4. Configuration Reference (`src/main/resources/application.properties`)

```properties
spring.datasource.url=${DATABASE_URL:jdbc:postgresql://localhost:5432/jobfetcher}
spring.datasource.username=${DATABASE_USERNAME:honey0632}
spring.datasource.password=${DATABASE_PASSWORD:Honey@632}

spring.jpa.hibernate.ddl-auto=update
app.frontend.origin=${FRONTEND_ORIGIN:http://localhost:5173}

# Matching & Sources
app.matching.provider=${MATCHING_PROVIDER:keyword}
app.job-sources.enabled=${JOB_SOURCES_ENABLED:GOOGLE_CAREERS,AMAZON,WELLS_FARGO,NVIDIA}
app.gemini.api-key=${GEMINI_API_KEY:}
app.gemini.model=${GEMINI_MODEL:gemini-2.5-flash}
```

---

## 5. Build, Test & Verification Commands

```powershell
# Run Java backend test suite (JUnit 5 / Spring Boot Tests)
.\mvnw.cmd clean test

# Build frontend SPA (TypeScript type check + Vite production bundle)
Set-Location frontend
npm install
npm run build
```
