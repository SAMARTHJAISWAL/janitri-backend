package com.janitri.backend.controllers;

import com.janitri.backend.dto.ApiResponse;
import com.janitri.backend.dto.HeartRateDTO;
import com.janitri.backend.models.HeartRateData;
import com.janitri.backend.services.HeartRateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/heart-rate")
public class HeartRateController {

    @Autowired
    private HeartRateService heartRateService;

    @PostMapping
    public ResponseEntity<Object> recordHeartRate(@Valid @RequestBody HeartRateDTO heartRateDTO) {
        HeartRateData heartRateData = heartRateService.recordHeartRate(heartRateDTO);
        return ResponseEntity.ok(ApiResponse.success(heartRateData, "Heart rate data recorded successfully"));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<List<HeartRateData>>> getPatientHeartRateData(@PathVariable Long patientId) {
        List<HeartRateData> heartRateData = heartRateService.getPatientHeartRateData(patientId);
        return ResponseEntity.ok(ApiResponse.success(heartRateData));
    }

    @GetMapping("/patient/{patientId}/between")
    public ResponseEntity<ApiResponse<List<HeartRateData>>> getPatientHeartRateDataBetween(
            @PathVariable Long patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        List<HeartRateData> heartRateData = heartRateService.getPatientHeartRateDataBetween(
                patientId, startTime, endTime);
        return ResponseEntity.ok(ApiResponse.success(heartRateData));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<HeartRateData>>> getHeartRateDataByStatus(@PathVariable String status) {
        List<HeartRateData> heartRateData = heartRateService.getHeartRateDataByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(heartRateData));
    }
}