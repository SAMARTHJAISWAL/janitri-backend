package com.janitri.backend.services;

import com.janitri.backend.models.HeartRateData;
import com.janitri.backend.models.Patient;
import com.janitri.backend.repositories.HeartRateRepository;
import com.janitri.backend.repositories.PatientRepository;
import com.janitri.backend.dto.HeartRateDTO;
import com.janitri.backend.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.time.LocalDateTime;

@Service
public class HeartRateService {
    @Autowired
    private HeartRateRepository heartRateRepository;
    
    @Autowired
    private PatientRepository patientRepository;

    public HeartRateData recordHeartRate(HeartRateDTO heartRateDTO) {
        Patient patient = patientRepository.findById(heartRateDTO.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        HeartRateData heartRateData = new HeartRateData();
        heartRateData.setPatient(patient);
        heartRateData.setHeartRate(heartRateDTO.getHeartRate());
        heartRateData.setTimestamp(heartRateDTO.getTimestamp() != null ? 
                                 heartRateDTO.getTimestamp() : LocalDateTime.now());
        heartRateData.setBloodPressureSystolic(heartRateDTO.getBloodPressureSystolic());
        heartRateData.setBloodPressureDiastolic(heartRateDTO.getBloodPressureDiastolic());
        heartRateData.setOxygenSaturation(heartRateDTO.getOxygenSaturation());
        heartRateData.setNotes(heartRateDTO.getNotes());

        return heartRateRepository.save(heartRateData);
    }

    public List<HeartRateData> getPatientHeartRateData(Long patientId) {
        return heartRateRepository.findByPatientId(patientId);
    }

    public List<HeartRateData> getPatientHeartRateDataBetween(
            Long patientId, LocalDateTime startTime, LocalDateTime endTime) {
        return heartRateRepository.findByPatientIdAndTimestampBetween(patientId, startTime, endTime);
    }

    public List<HeartRateData> getHeartRateDataByStatus(String status) {
        return heartRateRepository.findByStatus(status);
    }
}