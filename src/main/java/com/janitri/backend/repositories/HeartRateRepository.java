package com.janitri.backend.repositories;

import com.janitri.backend.models.HeartRateData;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface HeartRateRepository extends JpaRepository<HeartRateData, Long> {
    List<HeartRateData> findByPatientId(Long patientId);
    List<HeartRateData> findByPatientIdAndTimestampBetween(
        Long patientId, LocalDateTime startTime, LocalDateTime endTime);
    List<HeartRateData> findByStatus(String status);
}