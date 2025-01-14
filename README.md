# Janitri Backend - Patient Monitoring System

A Spring Boot backend application for managing patient heart rate monitoring devices. This system allows management of users (doctors), patients, and their heart rate data.

## Features

- User (Doctor) Management
  - Registration with email validation
  - Login functionality
  - User profile management

- Patient Management
  - Add new patients
  - Assign doctors to patients
  - Track patient status
  - View patient details

- Heart Rate Monitoring
  - Record real-time heart rate data
  - Retrieve patient heart rate history
  - Monitor heart rate status (NORMAL, WARNING, CRITICAL)
  - Time-based heart rate data queries

## Tech Stack

- Java 11
- Spring Boot 2.7.0
- Spring Data JPA
- H2 Database
- Maven
- JUnit 5 for testing

## Project Setup

### Prerequisites
- Java 11 or higher
- Maven 3.6 or higher
- Your favorite IDE (VS Code)

### Installation Steps

1. Clone the repository
```bash
git clone https://github.com/SAMARTHJAISWAL/janitri-backend.git
cd janitri-backend
```

2. Build the project
```bash
mvn clean install
```

3. Run the application
```bash
mvn spring-boot:run
```

The application will start on http://localhost:8080

## API Documentation

### User Management

#### Register New User
```http
POST /api/users/register
Content-Type: application/json

{
    "name": "Dr. John Doe",
    "email": "john.doe@example.com",
    "password": "secure123",
    "phoneNumber": "1234567890",
    "role": "DOCTOR"
}
```

#### User Login
```http
POST /api/users/login
Content-Type: application/json

{
    "email": "john.doe@example.com",
    "password": "secure123"
}
```

### Patient Management

#### Create Patient
```http
POST /api/patients
Content-Type: application/json

{
    "name": "Jane Smith",
    "dateOfBirth": "1990-01-01",
    "gender": "Female",
    "medicalHistory": "None",
    "emergencyContact": "1234567890",
    "assignedDoctorId": 1,
    "status": "ADMITTED"
}
```

#### Get Patient Details
```http
GET /api/patients/{id}
```

#### Get Patients by Doctor
```http
GET /api/patients/doctor/{doctorId}
```

### Heart Rate Data

#### Record Heart Rate
```http
POST /api/heart-rate
Content-Type: application/json

{
    "patientId": 1,
    "heartRate": 75,
    "bloodPressureSystolic": 120,
    "bloodPressureDiastolic": 80,
    "oxygenSaturation": 98
}
```

#### Get Patient Heart Rate History
```http
GET /api/heart-rate/patient/{patientId}
```

#### Get Heart Rate Data Between Dates
```http
GET /api/heart-rate/patient/{patientId}/between?startTime=2024-01-01T00:00:00&endTime=2024-01-02T00:00:00
```

## Design Decisions and Assumptions

1. Data Model Relationships:
   - One-to-Many relationship between Doctor and Patients
   - One-to-Many relationship between Patient and Heart Rate Data
   - Each patient can only have one assigned doctor at a time

2. Business Rules:
   - Heart rate status is automatically calculated:
     * NORMAL: 60-100 bpm
     * WARNING: <60 or >100 bpm
     * CRITICAL: <50 or >120 bpm

3. Validation Rules:
   - Email must be unique for users
   - Password is required for users
   - Patient must have an assigned doctor
   - Heart rate data must have a patient reference

4. Security Considerations:
   - Basic email/password validation implemented
   - No encryption for simplicity (would add in production)

## Testing

Run the tests using:
```bash
mvn test
```

The project includes unit tests for:
- User Service
- Patient Service
- Heart Rate Service
- Data validation
- Error handling

## Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request
