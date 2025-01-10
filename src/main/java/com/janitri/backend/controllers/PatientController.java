package com.janitri.backend.controllers;

import com.janitri.backend.dto.ApiResponse;
import com.janitri.backend.dto.PatientDTO;
import com.janitri.backend.models.Patient;
import com.janitri.backend.services.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @PostMapping
    public ResponseEntity<ApiResponse<Patient>> createPatient(@Valid @RequestBody PatientDTO patientDTO) {
        Patient patient = patientService.createPatient(patientDTO);
        return ResponseEntity.ok(ApiResponse.success("Patient created successfully", patient));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Patient>> getPatientById(@PathVariable Long id) {
        Patient patient = patientService.getPatientById(id);
        return ResponseEntity.ok(ApiResponse.success(patient));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<ApiResponse<List<Patient>>> getPatientsByDoctor(@PathVariable Long doctorId) {
        List<Patient> patients = patientService.getPatientsByDoctor(doctorId);
        return ResponseEntity.ok(ApiResponse.success(patients));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<Patient>>> getPatientsByStatus(@PathVariable String status) {
        List<Patient> patients = patientService.getPatientsByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(patients));
    }
}