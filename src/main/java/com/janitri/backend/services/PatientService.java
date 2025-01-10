package com.janitri.backend.services;

import com.janitri.backend.models.Patient;
import com.janitri.backend.models.User;
import com.janitri.backend.repositories.PatientRepository;
import com.janitri.backend.repositories.UserRepository;
import com.janitri.backend.dto.PatientDTO;
import com.janitri.backend.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PatientService {
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private UserRepository userRepository;

    public Patient createPatient(PatientDTO patientDTO) {
        User doctor = userRepository.findById(patientDTO.getAssignedDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        Patient patient = new Patient();
        patient.setName(patientDTO.getName());
        patient.setDateOfBirth(patientDTO.getDateOfBirth());
        patient.setGender(patientDTO.getGender());
        patient.setMedicalHistory(patientDTO.getMedicalHistory());
        patient.setEmergencyContact(patientDTO.getEmergencyContact());
        patient.setAssignedDoctor(doctor);
        patient.setDeviceId(patientDTO.getDeviceId());
        patient.setStatus(patientDTO.getStatus());

        return patientRepository.save(patient);
    }

    public Patient getPatientById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
    }

    public List<Patient> getPatientsByDoctor(Long doctorId) {
        return patientRepository.findByAssignedDoctorId(doctorId);
    }

    public List<Patient> getPatientsByStatus(String status) {
        return patientRepository.findByStatus(status);
    }
}