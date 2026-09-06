# Job Fetcher Implementation Plan

## Problem and approach

Build the first working version of the Spring Boot job-matching backend around PostgreSQL. The initial milestone will fetch Google Careers jobs, map them into a stable domain model, and persist/query them through Spring Data JPA. The design will also establish user/resume ownership boundaries and an upload API shape so PDF/DOCX resume ingestion can be added without redesigning the job pipeline.

## Todos

1. **Complete** — Configure PostgreSQL connectivity and environment-safe application settings.
2. **Complete** — Create the persisted job domain model, repository, and basic jobs API.
3. **Complete** — Implement a reliable Google Careers fetcher with parsing, normalization, and error handling.
4. **Complete** — Add persistence integration and targeted tests for job mapping, deduplication, and API behavior.
5. **Complete** — Design and implement user/resume entities and a multipart upload endpoint for PDF/DOCX files.
6. **Complete** — Add resume text extraction and structured resume representation behind a service boundary.
7. **Complete** — Add job matching abstractions and defer embeddings/vector search until the ingestion pipeline is stable.
8. **Complete** — Document local setup, PostgreSQL/Compose usage, API contracts, and frontend integration.

## Dependencies and sequencing

- PostgreSQL configuration must be established before persistence integration.
- The job model and repository must exist before the fetcher can persist jobs.
- The fetcher should be tested independently before wiring the jobs API.
- Resume upload depends on the initial persistence conventions but should remain independently deployable.
- Matching depends on both persisted jobs and extracted resume representations.

## Notes and considerations

- Use PostgreSQL for V1 and keep database access behind repository/service interfaces so storage details do not leak into matching logic.
- Do not add Spring AI or pgvector in the first milestone; reserve fields/interfaces for future embeddings rather than coupling the initial schema to them.
- Avoid hardcoding credentials; support environment variables with development defaults only where appropriate.
- Treat Google Careers HTML/API behavior as an external dependency: use timeouts, validation, deduplication, and explicit failure reporting.
- Store uploaded resume files outside source control and persist metadata plus extracted content/reference, with file-size and content-type validation.
- Support PDF and DOCX first; keep extraction behind a replaceable interface.
