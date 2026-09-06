# Implementation Steps

## Existing backend foundation

1. Configured PostgreSQL connectivity through environment-overridable Spring properties.
2. Added the `Jobs` entity, repository, service, and REST endpoints.
3. Added the Google Careers client using `RestClient`.
4. Parsed Google Careers `ds:1` callback data and normalized job descriptions.
5. Added duplicate-safe job upserts using the Google external job ID.
6. Added PDF/DOCX resume upload, storage, metadata persistence, and text extraction.
7. Added a replaceable matching interface with a keyword-based baseline scorer.
8. Added parser, service, controller, extraction, and matching tests.

## Frontend and OAuth2 implementation

9. Added Spring Security OAuth2 client support for Google login.
10. Added OAuth provider/subject fields to users and automatic account upsert after login.
11. Added authenticated `/api/auth/me` and CSRF token endpoints.
12. Added session-protected API rules, REST-friendly `401` responses, logout handling, and explicit frontend CORS.
13. Added authenticated search preferences for country, experience, and desired role.
14. Changed resume upload and listing to derive ownership from the authenticated session instead of a browser-supplied user ID.
15. Added criteria-based job search orchestration that combines role, country, and experience.
16. Added authenticated matching against the latest extracted resume and strict score filtering above 80%.
17. Added a React + TypeScript frontend under `frontend/` with login, preferences, resume upload, search, and match screens.
18. Added a credentialed frontend API client with CSRF token support and environment-based backend URL.
19. Added the criteria-search test proving that score `80` is excluded and only scores strictly above `80` are returned.
20. Added security smoke tests for protected API access and CSRF initialization.
21. Added backend and frontend production Dockerfiles, Nginx SPA serving, Docker ignore files, and a GitHub Actions CI/CD workflow that tests the project and publishes images to GHCR on `main`.
22. Centralized local and Oracle deployment configuration in an ignored `.env` file, added `.env.example`, and added `compose.production.yaml` for pulling and running the published images.
23. Built both Docker images locally with Docker Desktop and verified the backend and frontend images were created successfully.
24. Updated CI/CD Docker publishing to build `linux/amd64` and `linux/arm64` images for Oracle Cloud A1 ARM instances.
25. Prepared the Oracle VM with Docker, Compose, persistent resume storage, and firewall access for SSH, HTTP, and HTTPS.
26. Added Caddy to the production Compose stack for automatic HTTPS and reverse proxy routing for the frontend and API domains.
27. Enabled forwarded-header handling so OAuth callbacks retain HTTPS when the backend runs behind Caddy.

## Verification

Backend:

```powershell
.\mvnw.cmd test
```

Frontend:

```powershell
Set-Location frontend
npm install
npm run build
```

Both builds currently pass. OAuth login requires real `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET` environment variables and a matching Google redirect URI before end-to-end sign-in can be used.
