package com.janitri.backend.services;

import com.janitri.backend.models.Patient;
import com.janitri.backend.models.User;
import com.janitri.backend.repositories.PatientRepository;
import com.janitri.backend.repositories.UserRepository;
import com.janitri.backend.dto.PatientDTO;
import com.janitri.backend.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PatientService patientService;

    private PatientDTO patientDTO;
    private Patient patient;
    private User doctor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        doctor = new User();
        doctor.setId(1L);
        doctor.setName("Dr. Smith");
        doctor.setEmail("dr.smith@example.com");
        doctor.setRole("DOCTOR");

        patientDTO = new PatientDTO();
        patientDTO.setName("John Doe");
        patientDTO.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patientDTO.setGender("Male");
        patientDTO.setMedicalHistory("None");
        patientDTO.setEmergencyContact("1234567890");
        patientDTO.setAssignedDoctorId(doctor.getId());
        patientDTO.setStatus("ADMITTED");

        patient = new Patient();
        patient.setId(1L);
        patient.setName(patientDTO.getName());
        patient.setDateOfBirth(patientDTO.getDateOfBirth());
        patient.setGender(patientDTO.getGender());
        patient.setMedicalHistory(patientDTO.getMedicalHistory());
        patient.setEmergencyContact(patientDTO.getEmergencyContact());
        patient.setAssignedDoctor(doctor);
        patient.setStatus(patientDTO.getStatus());
    }

    @Test
    void whenCreatePatient_thenReturnSavedPatient() {
        when(userRepository.findById(doctor.getId())).thenReturn(Optional.of(doctor));
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);

        Patient savedPatient = patientService.createPatient(patientDTO);

        assertNotNull(savedPatient);
        assertEquals(patientDTO.getName(), savedPatient.getName());
        assertEquals(patientDTO.getDateOfBirth(), savedPatient.getDateOfBirth());
        assertEquals(doctor.getId(), savedPatient.getAssignedDoctor().getId());
        
        verify(userRepository).findById(doctor.getId());
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void whenCreatePatientWithInvalidDoctor_thenThrowException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            patientService.createPatient(patientDTO);
        });

        verify(userRepository).findById(doctor.getId());
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void whenGetPatientsByDoctor_thenReturnPatientList() {
        List<Patient> patients = Arrays.asList(patient);
        when(patientRepository.findByAssignedDoctorId(doctor.getId())).thenReturn(patients);

        List<Patient> foundPatients = patientService.getPatientsByDoctor(doctor.getId());

        assertNotNull(foundPatients);
        assertFalse(foundPatients.isEmpty());
        assertEquals(1, foundPatients.size());
        assertEquals(patient.getName(), foundPatients.get(0).getName());
        
        verify(patientRepository).findByAssignedDoctorId(doctor.getId());
    }

    @Test
    void whenGetPatientsByStatus_thenReturnPatientList() {
        String status = "ADMITTED";
        List<Patient> patients = Arrays.asList(patient);
        when(patientRepository.findByStatus(status)).thenReturn(patients);

        List<Patient> foundPatients = patientService.getPatientsByStatus(status);

        assertNotNull(foundPatients);
        assertFalse(foundPatients.isEmpty());
        assertEquals(1, foundPatients.size());
        assertEquals(status, foundPatients.get(0).getStatus());
        
        verify(patientRepository).findByStatus(status);
    }
}