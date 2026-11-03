package com.janitri.backend.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end test of the REST API against the in-memory H2 database:
 * register a doctor, log in, create a patient, record readings and query them back.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ApiIntegrationTest {

    @Autowired
    private MockMvc mvc;

    private static final String DOCTOR = "{\"name\":\"Dr. John Doe\",\"email\":\"john.doe@example.com\","
            + "\"password\":\"secure123\",\"phoneNumber\":\"1234567890\",\"role\":\"DOCTOR\"}";

    private long registerDoctor() throws Exception {
        MvcResult result = mvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON).content(DOCTOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.patients").doesNotExist())
                .andReturn();
        return Long.parseLong(result.getResponse().getContentAsString().replaceAll(".*\"data\":\\{\"id\":(\\d+).*", "$1"));
    }

    private long createPatient(long doctorId) throws Exception {
        String patient = "{\"name\":\"Jane Smith\",\"dateOfBirth\":\"1990-01-01\",\"gender\":\"Female\","
                + "\"medicalHistory\":\"None\",\"emergencyContact\":\"1234567890\","
                + "\"assignedDoctorId\":" + doctorId + ",\"status\":\"ADMITTED\"}";
        MvcResult result = mvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON).content(patient))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Jane Smith"))
                .andExpect(jsonPath("$.data.assignedDoctor.id").value((int) doctorId))
                .andExpect(jsonPath("$.data.assignedDoctor.password").doesNotExist())
                .andExpect(jsonPath("$.data.heartRateData").doesNotExist())
                .andReturn();
        return Long.parseLong(result.getResponse().getContentAsString().replaceAll(".*\"data\":\\{\"id\":(\\d+).*", "$1"));
    }

    private void recordHeartRate(long patientId, int bpm, String timestamp, String expectedStatus) throws Exception {
        mvc.perform(post("/api/heart-rate").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"patientId\":" + patientId + ",\"heartRate\":" + bpm
                                + ",\"timestamp\":\"" + timestamp + "\",\"oxygenSaturation\":98}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Heart rate data recorded successfully"))
                .andExpect(jsonPath("$.data.heartRate").value(bpm))
                .andExpect(jsonPath("$.data.status").value(expectedStatus));
    }

    @Test
    void fullFlow_registerLoginPatientHeartRate() throws Exception {
        long doctorId = registerDoctor();

        mvc.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"john.doe@example.com\",\"password\":\"secure123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value((int) doctorId))
                .andExpect(jsonPath("$.data.password").doesNotExist());

        mvc.perform(get("/api/users/" + doctorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Dr. John Doe"));

        long patientId = createPatient(doctorId);

        mvc.perform(get("/api/patients/" + patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ADMITTED"));
        mvc.perform(get("/api/patients/doctor/" + doctorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));
        mvc.perform(get("/api/patients/status/ADMITTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));

        recordHeartRate(patientId, 75, "2024-01-01T10:00:00", "NORMAL");
        recordHeartRate(patientId, 110, "2024-01-01T11:00:00", "WARNING");
        recordHeartRate(patientId, 130, "2024-01-03T10:00:00", "CRITICAL");

        mvc.perform(get("/api/heart-rate/patient/" + patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(jsonPath("$.data[0].patient.id").value((int) patientId));
        mvc.perform(get("/api/heart-rate/patient/" + patientId + "/between")
                        .param("startTime", "2024-01-01T00:00:00")
                        .param("endTime", "2024-01-02T00:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
        mvc.perform(get("/api/heart-rate/status/critical"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].heartRate").value(130));
    }

    @Test
    void errorResponses_haveMeaningfulStatusCodes() throws Exception {
        long doctorId = registerDoctor();

        // duplicate email -> 409
        mvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON).content(DOCTOR))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
        // wrong password -> 401
        mvc.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"john.doe@example.com\",\"password\":\"nope\"}"))
                .andExpect(status().isUnauthorized());
        // missing doctor / status -> 400 with field errors
        mvc.perform(post("/api/patients").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"No Doctor\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.assignedDoctorId").exists())
                .andExpect(jsonPath("$.data.status").exists());
        // unknown doctor -> 404
        mvc.perform(post("/api/patients").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\",\"assignedDoctorId\":999,\"status\":\"ADMITTED\"}"))
                .andExpect(status().isNotFound());
        // unknown patient -> 404
        mvc.perform(post("/api/heart-rate").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"patientId\":999,\"heartRate\":80}"))
                .andExpect(status().isNotFound());
        // out-of-range heart rate -> 400
        long patientId = createPatient(doctorId);
        mvc.perform(post("/api/heart-rate").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"patientId\":" + patientId + ",\"heartRate\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.heartRate").exists());
        // malformed JSON -> 400
        mvc.perform(post("/api/heart-rate").contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isBadRequest());
        // missing user -> 404
        mvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("not found")));
    }
}
