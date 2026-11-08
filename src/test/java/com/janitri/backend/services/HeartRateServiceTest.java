package com.janitri.backend.services;

import com.janitri.backend.dto.HeartRateDTO;
import com.janitri.backend.exceptions.ResourceNotFoundException;
import com.janitri.backend.models.HeartRateData;
import com.janitri.backend.models.Patient;
import com.janitri.backend.repositories.HeartRateRepository;
import com.janitri.backend.repositories.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HeartRateServiceTest {

    @Mock
    private HeartRateRepository heartRateRepository;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private HeartRateService heartRateService;

    private Patient patient;
    private HeartRateDTO dto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        patient = new Patient();
        patient.setId(1L);
        patient.setName("John Doe");
        patient.setStatus("ADMITTED");

        dto = new HeartRateDTO();
        dto.setPatientId(1L);
        dto.setHeartRate(75);
        dto.setBloodPressureSystolic(120);
        dto.setBloodPressureDiastolic(80);
        dto.setOxygenSaturation(98);
    }

    @Test
    void whenRecordHeartRate_thenReturnSavedData() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(heartRateRepository.save(any(HeartRateData.class))).thenAnswer(inv -> inv.getArgument(0));

        HeartRateData saved = heartRateService.recordHeartRate(dto);

        assertEquals(patient, saved.getPatient());
        assertEquals(75, saved.getHeartRate());
        assertNotNull(saved.getTimestamp(), "timestamp defaults to now when not supplied");
        verify(heartRateRepository).save(any(HeartRateData.class));
    }

    @Test
    void whenRecordHeartRateWithInvalidPatient_thenThrowException() {
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> heartRateService.recordHeartRate(dto));
        verify(heartRateRepository, never()).save(any(HeartRateData.class));
    }

    @Test
    void whenGetPatientHeartRateData_thenReturnDataList() {
        List<HeartRateData> data = Collections.singletonList(new HeartRateData());
        when(heartRateRepository.findByPatientId(1L)).thenReturn(data);

        assertEquals(1, heartRateService.getPatientHeartRateData(1L).size());
    }

    @Test
    void whenGetHeartRateDataBetween_thenReturnDataList() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 2, 0, 0);
        List<HeartRateData> data = Collections.singletonList(new HeartRateData());
        when(heartRateRepository.findByPatientIdAndTimestampBetween(1L, start, end)).thenReturn(data);

        assertEquals(1, heartRateService.getPatientHeartRateDataBetween(1L, start, end).size());
    }

    @Test
    void whenGetHeartRateDataByStatus_thenStatusIsCaseInsensitive() {
        List<HeartRateData> data = Collections.singletonList(new HeartRateData());
        when(heartRateRepository.findByStatus("CRITICAL")).thenReturn(data);

        assertEquals(1, heartRateService.getHeartRateDataByStatus("critical").size());
        verify(heartRateRepository).findByStatus("CRITICAL");
    }
}
