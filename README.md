# Janitri Backend — Patient Heart-Rate Monitoring API

A Spring Boot REST API for a patient monitoring system, written as a backend assignment for
Janitri. Doctors (users) register and log in, patients are assigned to a doctor, and heart-rate
readings are recorded per patient and automatically classified as `NORMAL`, `WARNING` or `CRITICAL`.

## Features

- **Users (doctors):** register with name, email, password, phone and role; log in with email +
  password; fetch a user by id. Emails are unique. Passwords are stored as BCrypt hashes and are
  never included in API responses.
- **Patients:** create a patient assigned to an existing doctor; fetch by id, by doctor, or by status
  (e.g. `ADMITTED`). Admission date defaults to the creation time.
- **Heart-rate readings:** record heart rate (1–300 bpm) plus optional blood pressure, oxygen
  saturation, notes and timestamp (defaults to now); list a patient's readings, readings in a time
  window, or all readings with a given status (case-insensitive). Critical readings are logged as
  warnings.
- **Consistent responses:** every endpoint returns `{ "success", "message", "data" }`. Validation
  errors return `400` with a field → message map in `data`; unknown ids `404`; duplicate email
  `409`; wrong login `401`.

### Heart-rate status rules

Calculated by the `HeartRateData` entity whenever a reading is saved:

| Heart rate (bpm) | Status |
| --- | --- |
| 60 – 100 | `NORMAL` |
| 50 – 59 or 101 – 120 | `WARNING` |
| below 50 or above 120 | `CRITICAL` |

## Tech stack

Java 11+ · Spring Boot 2.7.18 (Web, Data JPA, Validation) · Hibernate · H2 (in-memory by default) ·
spring-security-crypto (BCrypt only) · Maven (wrapper included) · JUnit 5 · Mockito · MockMvc

## Running locally

Prerequisite: a JDK, version 11 or newer (tested with Temurin 17). Maven is optional; the wrapper
downloads it.

```bash
git clone https://github.com/SAMARTHJAISWAL/janitri-backend.git
cd janitri-backend
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run
```

The API starts on http://localhost:8080. To build and run a jar instead:

```bash
./mvnw clean package
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

### Configuration

All settings live in `src/main/resources/application.properties` and can be overridden with
environment variables. No credentials are committed; the defaults use a throw-away in-memory H2
database, so data is lost on restart.

| Variable | Default | Purpose |
| --- | --- | --- |
| `PORT` | `8080` | HTTP port |
| `DB_URL` | `jdbc:h2:mem:janitridb` | JDBC URL (e.g. `jdbc:h2:file:./data/janitridb` to persist) |
| `DB_USERNAME` | `sa` | Database user |
| `DB_PASSWORD` | *(empty)* | Database password |
| `DB_DRIVER` | `org.h2.Driver` | JDBC driver class |
| `DB_DIALECT` | `org.hibernate.dialect.H2Dialect` | Hibernate dialect |
| `H2_CONSOLE_ENABLED` | `false` | Expose the H2 console at `/h2-console` (local debugging only) |
| `SHOW_SQL` | `false` | Log SQL statements |
| `LOG_PATH` | `./logs` | Directory for the rolling log file |

Using a database other than H2 also needs its JDBC driver added to `pom.xml`.

## API endpoints

Base URL: `http://localhost:8080`

| Method | Path | Description | Body / params |
| --- | --- | --- | --- |
| `POST` | `/api/users/register` | Register a user (doctor) | `name`*, `email`*, `password`*, `phoneNumber`, `role` |
| `POST` | `/api/users/login` | Log in; returns the user | `email`*, `password`* |
| `GET` | `/api/users/{id}` | Get a user by id | — |
| `POST` | `/api/patients` | Create a patient | `name`*, `assignedDoctorId`*, `status`*, `dateOfBirth` (`YYYY-MM-DD`), `gender`, `medicalHistory`, `emergencyContact`, `deviceId` |
| `GET` | `/api/patients/{id}` | Get a patient by id | — |
| `GET` | `/api/patients/doctor/{doctorId}` | Patients assigned to a doctor | — |
| `GET` | `/api/patients/status/{status}` | Patients with a status (exact match, e.g. `ADMITTED`) | — |
| `POST` | `/api/heart-rate` | Record a heart-rate reading | `patientId`*, `heartRate`* (1–300), `timestamp` (ISO date-time), `bloodPressureSystolic`, `bloodPressureDiastolic`, `oxygenSaturation`, `notes` |
| `GET` | `/api/heart-rate/patient/{patientId}` | All readings for a patient | — |
| `GET` | `/api/heart-rate/patient/{patientId}/between` | Readings in a time window | query `startTime`, `endTime` (ISO date-time) |
| `GET` | `/api/heart-rate/status/{status}` | Readings with status `NORMAL` / `WARNING` / `CRITICAL` | — |

\* required

### Example session

```bash
# register a doctor
curl -X POST localhost:8080/api/users/register -H 'Content-Type: application/json' \
  -d '{"name":"Dr. John Doe","email":"john.doe@example.com","password":"secure123","role":"DOCTOR"}'

# create a patient assigned to doctor 1
curl -X POST localhost:8080/api/patients -H 'Content-Type: application/json' \
  -d '{"name":"Jane Smith","dateOfBirth":"1990-01-01","assignedDoctorId":1,"status":"ADMITTED"}'

# record a reading for patient 1
curl -X POST localhost:8080/api/heart-rate -H 'Content-Type: application/json' \
  -d '{"patientId":1,"heartRate":130,"oxygenSaturation":98}'

# readings for patient 1 in a time window
curl 'localhost:8080/api/heart-rate/patient/1/between?startTime=2024-01-01T00:00:00&endTime=2030-01-01T00:00:00'
```

Example response (`POST /api/heart-rate`, trimmed):

```json
{
  "success": true,
  "message": "Heart rate data recorded successfully",
  "data": { "id": 1, "heartRate": 130, "status": "CRITICAL", "patient": { "id": 1, "name": "Jane Smith" } }
}
```

## Testing

```bash
./mvnw test
```

- `UserServiceTest`, `PatientServiceTest`, `HeartRateServiceTest`: Mockito unit tests for the services
  (password hashing, duplicate email, invalid login, unknown doctor/patient, queries).
- `ApiIntegrationTest`: boots the app against in-memory H2 and drives every endpoint through MockMvc,
  covering the full register → login → patient → readings flow, the status classification, and the
  400 / 401 / 404 / 409 error responses.

## Project structure

```
src/main/java/com/janitri/backend/
├── JanitriBackendApplication.java   # Spring Boot entry point
├── controllers/                     # UserController, PatientController, HeartRateController
├── dto/                             # request DTOs with validation + ApiResponse wrapper
├── exceptions/                      # custom exceptions + GlobalExceptionHandler
├── models/                          # JPA entities: User, Patient, HeartRateData
├── repositories/                    # Spring Data JPA repositories
└── services/                        # business logic
src/main/resources/
├── application.properties           # env-overridable configuration
└── logback-spring.xml               # console + rolling file logging
src/test/java/com/janitri/backend/   # unit and integration tests
```

## Design notes and limitations

- One doctor has many patients; one patient has many heart-rate readings. A patient has exactly one
  assigned doctor.
- To avoid infinite JSON recursion, a user's patient list and a patient's reading list are not
  embedded in responses; use the `/doctor/{id}` and `/heart-rate/patient/{id}` endpoints instead.
- Login only checks credentials and returns the user. There is no session, token or authorization
  on the other endpoints, so this is not production-ready as-is; adding Spring Security with JWT
  would be the next step.
- Schema is managed by Hibernate (`ddl-auto=update`); there are no migrations.
