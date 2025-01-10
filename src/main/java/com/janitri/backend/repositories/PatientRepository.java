package com.janitri.backend.repositories;

import com.janitri.backend.models.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    List<Patient> findByAssignedDoctorId(Long doctorId);
    List<Patient> findByStatus(String status);
}