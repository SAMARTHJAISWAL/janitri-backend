package com.janitri.backend.services;

import com.janitri.backend.models.HeartRateData;
import com.janitri.backend.models.Patient;
import com.janitri.backend.repositories.HeartRateRepository;
import com.janitri.backend.repositories.PatientRepository;
import com.janitri.backend.dto.HeartRateDTO;
import com.janitri.backend.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;
import java.time.LocalDateTime;

/**
 * Records and queries heart rate readings. The NORMAL / WARNING / CRITICAL status is
 * calculated by {@link HeartRateData} itself when the reading is saved.
 */
@Service
public class HeartRateService {
    private static final Logger logger = LoggerFactory.getLogger(HeartRateService.class);

    @Autowired
    private HeartRateRepository heartRateRepository;
    
    @Autowired
    private PatientRepository patientRepository;

    @Transactional
    public HeartRateData recordHeartRate(HeartRateDTO heartRateDTO) {
        logger.info("Recording heart rate for patient ID: {}", heartRateDTO.getPatientId());
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

        HeartRateData saved = heartRateRepository.save(heartRateData);
        if ("CRITICAL".equals(saved.getStatus())) {
            logger.warn("CRITICAL heart rate for patient ID: {}. Value: {}",
                    patient.getId(), saved.getHeartRate());
        }
        return saved;
    }

    public List<HeartRateData> getPatientHeartRateData(Long patientId) {
        return heartRateRepository.findByPatientId(patientId);
    }

    public List<HeartRateData> getPatientHeartRateDataBetween(
            Long patientId, LocalDateTime startTime, LocalDateTime endTime) {
        return heartRateRepository.findByPatientIdAndTimestampBetween(patientId, startTime, endTime);
    }

    public List<HeartRateData> getHeartRateDataByStatus(String status) {
        // Statuses are stored upper-case (NORMAL, WARNING, CRITICAL); accept any case in the URL
        return heartRateRepository.findByStatus(status.toUpperCase(Locale.ROOT));
    }
}