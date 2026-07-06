# Backend Conventions

## Goal

The backend should stay easy to evolve while the MVP is developed quickly. The first standard is consistency: predictable packages, predictable API responses, predictable errors, and repeatable tests.

## Package Layout

```text
com.jobpilot
├─ common       shared response and error contracts
├─ controller   HTTP endpoints
├─ dto          request and response payloads
├─ model        domain objects and enums
└─ service      business logic
```

## API Response Format

All business endpoints return:

```json
{
  "success": true,
  "code": "OK",
  "message": "OK",
  "data": {}
}
```

Error response:

```json
{
  "success": false,
  "code": "JOB_NOT_FOUND",
  "message": "Job posting not found",
  "data": null
}
```

## Error Handling

- Use `BusinessException` for expected business failures.
- Use `ErrorCode` for stable error identifiers.
- Use `GlobalExceptionHandler` to convert exceptions into `ApiResponse`.
- Keep HTTP status meaningful:
  - `400`: invalid request.
  - `404`: resource not found.
  - `500`: unexpected server error.

## Controller Rules

- Controllers should be thin.
- Controllers call services and wrap success responses with `ApiResponse.ok(...)`.
- Controllers should not build domain workflows directly.

## Service Rules

- Services own business validation and state changes.
- Missing resources should throw `BusinessException`.
- In-memory storage is acceptable in the current MVP scaffold.

## Testing Rules

- Use `SpringBootTest` + `MockMvc` for API contract tests.
- Each module should test:
  - main success path;
  - at least one failure path;
  - response `success`, `code`, and key `data` fields.

## Verification

Run:

```powershell
cd D:\Jobpilot\backend
mvn test
```

