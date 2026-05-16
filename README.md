# Personal Task Manager API

A Java 17 Spring Boot REST API for managing personal tasks, backed by an H2 in-memory database. It includes a simple static frontend and an AI suggestion endpoint that converts plain-language reminders into structured task JSON.

## Tech Stack

- Java 17
- Spring Boot 4.0.6
- Maven with Maven Wrapper
- Spring Web MVC
- Spring Data JPA
- H2 in-memory database
- Gemini API integration behind a mockable service interface
- JUnit, Mockito, and MockMvc tests

## Setup

Install a Java 17 or newer JDK. Maven does not need to be installed globally because the repository includes `./mvnw`.

Do not commit secrets. If you want live Gemini suggestions, set the API key in your shell:

```bash
export GEMINI_API_KEY="your_gemini_api_key_here"
./mvnw spring-boot:run

If `GEMINI_API_KEY` is missing, the app returns a deterministic demo suggestion so reviewers can run it locally without a Gemini account.

## Run

```bash
./mvnw spring-boot:run
```

Open the static frontend at:

```text
http://localhost:8080/
```

## Test

```bash
./mvnw test
```

## Task Model

```json
{
  "id": 1,
  "title": "Submit report",
  "description": "Submit the quarterly report",
  "dueDate": "2026-05-22",
  "priority": "MEDIUM",
  "status": "TODO"
}
```

`priority` must be `LOW`, `MEDIUM`, or `HIGH`.

`status` must be `TODO`, `IN_PROGRESS`, or `DONE`.

## CRUD Endpoints

Create a task:

```bash
curl -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Submit report",
    "description": "Submit the quarterly report",
    "dueDate": "2026-05-22",
    "priority": "MEDIUM",
    "status": "TODO"
  }'
```

List tasks:

```bash
curl http://localhost:8080/tasks
```

Get one task:

```bash
curl http://localhost:8080/tasks/1
```

Update a task:

```bash
curl -X PUT http://localhost:8080/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Submit final report",
    "description": "Submit the final quarterly report",
    "dueDate": "2026-05-23",
    "priority": "HIGH",
    "status": "IN_PROGRESS"
  }'
```

Delete a task:

```bash
curl -X DELETE http://localhost:8080/tasks/1
```

## AI Suggestion Endpoint

`POST /tasks/suggest` accepts plain-language reminder text and returns a structured task suggestion. It does not persist the suggestion automatically.

Request:

```bash
curl -X POST http://localhost:8080/tasks/suggest \
  -H "Content-Type: application/json" \
  -d '{
    "text": "remind me to submit the quarterly report before Friday"
  }'
```

Example response:

```json
{
  "title": "Submit quarterly report",
  "description": "Submit the quarterly report before Friday",
  "dueDate": "2026-05-22",
  "priority": "MEDIUM",
  "status": "TODO"
}
```

The backend reads `GEMINI_API_KEY` from the environment and sends requests to Gemini only from the server-side service. The frontend calls only this Spring Boot backend and never receives the Gemini key.

## Design Choices

- The app uses a layered structure: `controller`, `service`, `repository`, `model`, `dto`, `config`, and `exception`.
- CRUD behavior lives in `TaskService`; controllers handle HTTP mapping and validation.
- AI behavior is isolated behind `AiSuggestionService`, implemented by `GeminiAiSuggestionService`, so tests can mock it and the rest of the app does not depend on Gemini details.
- The fallback AI response keeps local development and review deterministic when no API key is configured.
- DTOs keep request/response shapes separate from the JPA entity.
- Tests cover service happy paths, end-to-end CRUD with MockMvc, and the AI endpoint with a mocked AI service.

## Security Notes

- Never commit Gemini API keys.
- `.env` is ignored by Git.
- The application reads only the `GEMINI_API_KEY` environment variable for Gemini credentials.
- No API key is hardcoded in Java, properties, tests, README examples, or frontend JavaScript.
