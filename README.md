# Jobpilot

Jobpilot is an AI-assisted job application progress manager for students and early-career job seekers. The MVP focuses on job posting records, application status tracking, interview review notes, a personal question bank, and mockable AI analysis.

## Local-first Tech Choice

This repository is scaffolded to match the local machine and reduce install cost:

- Backend: Java 17 + Spring Boot 2.6.6, using dependencies already present in the local Maven cache.
- Frontend: dependency-free static HTML/CSS/JS prototype for the first runnable UI shell.
- Future upgrade path: Vue 3 + TypeScript + Vite can be introduced under `frontend/` when dependency installation is acceptable.

## Structure

```text
backend/       Spring Boot API skeleton
frontend/      Static Web MVP shell
docs/          Product requirements and project notes
```

## Run Backend

```powershell
cd backend
mvn spring-boot:run
```

The API starts at `http://localhost:8080`.

Useful endpoints:

- `GET /api/health`
- `GET /api/dashboard/summary`
- `GET /api/jobs`
- `POST /api/jobs`
- `POST /api/ai/parse-jd`

## Run Frontend

Start the backend first, then serve the static frontend from the repository root:

```powershell
python -m http.server 5500 --directory frontend
```

Open `http://localhost:5500`. The job module calls the backend at `http://localhost:8080/api` and does not require npm install.

Run the dependency-free frontend API tests with:

```powershell
node frontend/api.test.js
```

## Default Demo Account

The current skeleton keeps auth as a mock contract. Use this as the intended first account during future implementation:

- username: `demo`
- password: `demo123456`
