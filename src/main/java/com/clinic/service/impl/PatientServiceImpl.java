package com.clinic.service.impl;

import com.clinic.dto.request.PatientRequest;
import com.clinic.dto.response.AppointmentResponse;
import com.clinic.dto.response.PatientResponse;
import com.clinic.entity.Patient;
import com.clinic.exception.DuplicateResourceException;
import com.clinic.exception.ResourceNotFoundException;
import com.clinic.repository.PatientRepository;
import com.clinic.service.PatientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    @Override
    @CacheEvict(value = "patients", allEntries = true)
    public PatientResponse registerPatient(PatientRequest request) {
        log.info("Registering new patient with email: {}", request.getEmail());

        if (patientRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }
        if (patientRepository.existsByNationalId(request.getNationalId())) {
            throw new DuplicateResourceException("National ID already registered: " + request.getNationalId());
        }

        Patient patient = Patient.builder()
            .fullNameEn(request.getFullNameEn())
            .fullNameAr(request.getFullNameAr())
            .email(request.getEmail())
            .mobileNumber(request.getMobileNumber())
            .dateOfBirth(request.getDateOfBirth())
            .nationalId(request.getNationalId())
            .street(request.getStreet())
            .city(request.getCity())
            .region(request.getRegion())
            .build();

        patient = patientRepository.save(patient);
        log.info("Patient registered successfully with id: {}", patient.getId());
        return toResponse(patient);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "patients", key = "#id")
    public PatientResponse getPatientById(Long id) {
        log.debug("Fetching patient with id: {}", id);
        Patient patient = patientRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Patient", id));
        return toResponse(patient);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "patients", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<PatientResponse> getAllPatients(Pageable pageable) {
        return patientRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Async("taskExecutor")
    @Transactional(readOnly = true)
    public CompletableFuture<Page<PatientResponse>> getAllPatientsWithAppointmentsAsync(Pageable pageable) {
        log.debug("Async fetch of all patients with appointments");
        Page<PatientResponse> result = patientRepository.findAllWithAppointments(pageable).map(this::toResponseWithAppointments);
        return CompletableFuture.completedFuture(result);
    }

    @Override
    @CacheEvict(value = "patients", allEntries = true)
    public PatientResponse updatePatient(Long id, PatientRequest request) {
        Patient patient = patientRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Patient", id));

        // Check email uniqueness if changed
        if (!patient.getEmail().equals(request.getEmail()) &&
            patientRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already in use: " + request.getEmail());
        }

        patient.setFullNameEn(request.getFullNameEn());
        patient.setFullNameAr(request.getFullNameAr());
        patient.setEmail(request.getEmail());
        patient.setMobileNumber(request.getMobileNumber());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setStreet(request.getStreet());
        patient.setCity(request.getCity());
        patient.setRegion(request.getRegion());

        return toResponse(patientRepository.save(patient));
    }

    @Override
    @CacheEvict(value = "patients", allEntries = true)
    public void deletePatient(Long id) {
        if (!patientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Patient", id);
        }
        patientRepository.deleteById(id); // triggers @SQLDelete soft delete
        log.info("Patient soft-deleted with id: {}", id);
    }

    private PatientResponse toResponse(Patient patient) {
        return PatientResponse.builder()
            .id(patient.getId())
            .fullNameEn(patient.getFullNameEn())
            .fullNameAr(patient.getFullNameAr())
            .email(patient.getEmail())
            .mobileNumber(patient.getMobileNumber())
            .dateOfBirth(patient.getDateOfBirth())
            .nationalId(patient.getNationalId())
            .street(patient.getStreet())
            .city(patient.getCity())
            .region(patient.getRegion())
            .createdAt(patient.getCreatedAt())
            .build();
    }

    private PatientResponse toResponseWithAppointments(Patient patient) {
        PatientResponse response = toResponse(patient);
        List<AppointmentResponse> appointmentResponses = patient.getAppointments().stream()
            .map(a -> AppointmentResponse.builder()
                .id(a.getId())
                .doctorId(a.getDoctor().getId())
                .doctorNameEn(a.getDoctor().getNameEn())
                .doctorSpecialty(a.getDoctor().getSpecialty())
                .appointmentDateTime(a.getAppointmentDateTime())
                .status(a.getStatus())
                .notes(a.getNotes())
                .build())
            .collect(Collectors.toList());
        response.setAppointments(appointmentResponses);
        return response;
    }
}