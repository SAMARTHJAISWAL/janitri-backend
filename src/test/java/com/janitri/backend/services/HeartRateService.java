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

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for managing heart rate data operations.
 * Handles recording and retrieving patient heart rate measurements.
 */
@Service
public class HeartRateService {
    private static final Logger logger = LoggerFactory.getLogger(HeartRateService.class);
    
    // Constants for heart rate thresholds
    private static final int MIN_HEART_RATE_WARNING = 60;
    private static final int MAX_HEART_RATE_WARNING = 100;
    private static final int MIN_HEART_RATE_CRITICAL = 50;
    private static final int MAX_HEART_RATE_CRITICAL = 120;

    @Autowired
    private HeartRateRepository heartRateRepository;

    @Autowired
    private PatientRepository patientRepository;

    /**
     * Records new heart rate data for a patient.
     *
     * @param heartRateDTO Data transfer object containing heart rate information
     * @return The created heart rate data entity
     * @throws ResourceNotFoundException if patient not found
     */
    @Transactional
    public HeartRateData recordHeartRate(HeartRateDTO heartRateDTO) {
        logger.info("Recording heart rate data for patient ID: {}", heartRateDTO.getPatientId());
        
        // Validate heart rate value
        validateHeartRate(heartRateDTO.getHeartRate());

        Patient patient = patientRepository.findById(heartRateDTO.getPatientId())
                .orElseThrow(() -> {
                    logger.error("Patient not found with ID: {}", heartRateDTO.getPatientId());
                    return new ResourceNotFoundException("Patient not found");
                });

        HeartRateData heartRateData = new HeartRateData();
        heartRateData.setPatient(patient);
        heartRateData.setHeartRate(heartRateDTO.getHeartRate());
        heartRateData.setTimestamp(heartRateDTO.getTimestamp() != null ? 
                                 heartRateDTO.getTimestamp() : LocalDateTime.now());
        heartRateData.setBloodPressureSystolic(heartRateDTO.getBloodPressureSystolic());
        heartRateData.setBloodPressureDiastolic(heartRateDTO.getBloodPressureDiastolic());
        heartRateData.setOxygenSaturation(heartRateDTO.getOxygenSaturation());
        heartRateData.setNotes(heartRateDTO.getNotes());

        // Set status based on heart rate value
        setHeartRateStatus(heartRateData);

        // Check for critical conditions
        checkCriticalConditions(heartRateData);

        HeartRateData savedData = heartRateRepository.save(heartRateData);
        logger.info("Successfully recorded heart rate data with ID: {}", savedData.getId());
        return savedData;
    }

    /**
     * Retrieves heart rate data for a specific patient.
     *
     * @param patientId The ID of the patient
     * @return List of heart rate data entries
     */
    public List<HeartRateData> getPatientHeartRateData(Long patientId) {
        logger.debug("Fetching heart rate data for patient ID: {}", patientId);
        return heartRateRepository.findByPatientId(patientId);
    }

    /**
     * Retrieves heart rate data for a patient within a specific time range.
     *
     * @param patientId The ID of the patient
     * @param startTime Start of the time range
     * @param endTime End of the time range
     * @return List of heart rate data entries within the specified range
     */
    public List<HeartRateData> getPatientHeartRateDataBetween(
            Long patientId, LocalDateTime startTime, LocalDateTime endTime) {
        logger.debug("Fetching heart rate data for patient ID: {} between {} and {}", 
                    patientId, startTime, endTime);
        return heartRateRepository.findByPatientIdAndTimestampBetween(patientId, startTime, endTime);
    }

    /**
     * Retrieves heart rate data by status.
     *
     * @param status The status to filter by (NORMAL, WARNING, CRITICAL)
     * @return List of heart rate data entries with the specified status
     */
    public List<HeartRateData> getHeartRateDataByStatus(String status) {
        logger.debug("Fetching heart rate data with status: {}", status);
        return heartRateRepository.findByStatus(status);
    }

    /**
     * Validates heart rate value.
     *
     * @param heartRate The heart rate value to validate
     * @throws IllegalArgumentException if heart rate is invalid
     */
    private void validateHeartRate(Integer heartRate) {
        if (heartRate == null || heartRate <= 0 || heartRate > 300) {
            logger.error("Invalid heart rate value: {}", heartRate);
            throw new IllegalArgumentException("Invalid heart rate value");
        }
    }

    /**
     * Sets the status of heart rate data based on the value.
     *
     * @param heartRateData The heart rate data entity
     */
    private void setHeartRateStatus(HeartRateData heartRateData) {
        int heartRate = heartRateData.getHeartRate();
        if (heartRate < MIN_HEART_RATE_WARNING || heartRate > MAX_HEART_RATE_WARNING) {
            if (heartRate < MIN_HEART_RATE_CRITICAL || heartRate > MAX_HEART_RATE_CRITICAL) {
                heartRateData.setStatus("CRITICAL");
            } else {
                heartRateData.setStatus("WARNING");
            }
        } else {
            heartRateData.setStatus("NORMAL");
        }
    }

    /**
     * Checks for critical conditions and logs appropriate warnings.
     *
     * @param heartRateData The heart rate data entity
     */
    private void checkCriticalConditions(HeartRateData heartRateData) {
        if ("CRITICAL".equals(heartRateData.getStatus())) {
            logger.warn("CRITICAL heart rate detected for patient ID: {}. Value: {}", 
                       heartRateData.getPatient().getId(), 
                       heartRateData.getHeartRate());
        }
    }
}