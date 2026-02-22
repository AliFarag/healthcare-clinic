package com.clinic.service;

import com.clinic.dto.request.PatientRequest;
import com.clinic.dto.response.PatientResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.concurrent.CompletableFuture;

public interface PatientService {

    PatientResponse registerPatient(PatientRequest request);

    PatientResponse getPatientById(Long id);

    Page<PatientResponse> getAllPatients(Pageable pageable);

    // Async version - fetches all patients with their appointments
    CompletableFuture<Page<PatientResponse>> getAllPatientsWithAppointmentsAsync(Pageable pageable);

    PatientResponse updatePatient(Long id, PatientRequest request);

    void deletePatient(Long id);  // soft delete
}