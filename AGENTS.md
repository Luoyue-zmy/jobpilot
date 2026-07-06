# Jobpilot Agent Guide

These rules guide future Codex/vibe-coding work in this repository.

## Development Scope

- Work on one module per task.
- Keep edits scoped to files required by the requested module.
- Do not add new dependencies unless the task explicitly needs them and the reason is documented.
- Prefer local mock or in-memory implementations before introducing MySQL, Redis, Security, or real AI APIs.

## Backend Rules

- Keep package boundaries clear:
  - `controller`: HTTP request/response only.
  - `service`: business rules and workflow decisions.
  - `dto`: request/response payloads.
  - `model`: domain objects and enums.
  - `common`: shared response, exception, and error contracts.
- All business APIs must return `ApiResponse<T>`.
- Do not return raw objects, raw strings, or framework default error bodies from controllers.
- Service-layer business failures should throw `BusinessException`.
- Controllers should not contain business validation beyond request shape delegation.
- Every new endpoint needs at least one automated test or a documented reason why it is deferred.

## Testing Rules

- Run `mvn test` before considering backend work complete.
- Add focused tests for each changed module.
- Cover success and failure paths for controller-level behavior.
- Keep tests deterministic and independent of external services.

