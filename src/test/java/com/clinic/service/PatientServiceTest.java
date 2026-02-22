package com.clinic.service;

import com.clinic.dto.request.PatientRequest;
import com.clinic.dto.response.PatientResponse;
import com.clinic.entity.Patient;
import com.clinic.exception.DuplicateResourceException;
import com.clinic.exception.ResourceNotFoundException;
import com.clinic.repository.PatientRepository;
import com.clinic.service.impl.PatientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PatientService Tests")
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientServiceImpl patientService;

    private PatientRequest validRequest;
    private Patient savedPatient;

    @BeforeEach
    void setUp() {
        validRequest = PatientRequest.builder()
            .fullNameEn("John Doe")
            .fullNameAr("جون دو")
            .email("john@example.com")
            .mobileNumber("+96512345678")
            .dateOfBirth(LocalDate.of(1990, 1, 15))
            .nationalId("123456789")
            .city("Kuwait City")
            .region("Salmiya")
            .build();

        savedPatient = Patient.builder()
            .id(1L)
            .fullNameEn("John Doe")
            .fullNameAr("جون دو")
            .email("john@example.com")
            .mobileNumber("+96512345678")
            .dateOfBirth(LocalDate.of(1990, 1, 15))
            .nationalId("123456789")
            .city("Kuwait City")
            .region("Salmiya")
            .build();
    }

    @Test
    @DisplayName("Should register patient successfully")
    void shouldRegisterPatientSuccessfully() {
        when(patientRepository.existsByEmail(any())).thenReturn(false);
        when(patientRepository.existsByNationalId(any())).thenReturn(false);
        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);

        PatientResponse response = patientService.registerPatient(validRequest);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getFullNameEn()).isEqualTo("John Doe");
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when email already exists")
    void shouldThrowOnDuplicateEmail() {
        when(patientRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> patientService.registerPatient(validRequest))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("Email already registered");

        verify(patientRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when National ID already exists")
    void shouldThrowOnDuplicateNationalId() {
        when(patientRepository.existsByEmail(any())).thenReturn(false);
        when(patientRepository.existsByNationalId("123456789")).thenReturn(true);

        assertThatThrownBy(() -> patientService.registerPatient(validRequest))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("National ID already registered");
    }

    @Test
    @DisplayName("Should return patient by ID")
    void shouldGetPatientById() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(savedPatient));

        PatientResponse response = patientService.getPatientById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when patient not found")
    void shouldThrowWhenPatientNotFound() {
        when(patientRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.getPatientById(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Patient not found");
    }

    @Test
    @DisplayName("Should soft-delete patient successfully")
    void shouldSoftDeletePatient() {
        when(patientRepository.existsById(1L)).thenReturn(true);

        assertThatCode(() -> patientService.deletePatient(1L)).doesNotThrowAnyException();

        verify(patientRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw when deleting non-existent patient")
    void shouldThrowWhenDeletingNonExistentPatient() {
        when(patientRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> patientService.deletePatient(999L))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}