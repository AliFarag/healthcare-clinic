package com.clinic.service;

import com.clinic.dto.response.DoctorResponse;
import com.clinic.entity.Doctor;
import com.clinic.exception.ResourceNotFoundException;
import com.clinic.repository.DoctorRepository;
import com.clinic.service.impl.DoctorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DoctorService Tests")
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorServiceImpl doctorService;

    private Doctor doctor1;
    private Doctor doctor2;

    @BeforeEach
    void setUp() {
        doctor1 = Doctor.builder()
            .id(1L).nameEn("Dr. Ahmed Al-Rashid").nameAr("د. أحمد الراشد")
            .specialty("Cardiology").yearsOfExperience(15)
            .consultationDurationMinutes(30).active(true).build();

        doctor2 = Doctor.builder()
            .id(2L).nameEn("Dr. Sarah Johnson").nameAr("د. سارة جونسون")
            .specialty("General Practice").yearsOfExperience(8)
            .consultationDurationMinutes(20).active(true).build();
    }

    @Test
    @DisplayName("Should return all active doctors")
    void shouldReturnAllActiveDoctors() {
        when(doctorRepository.findByActiveTrue()).thenReturn(List.of(doctor1, doctor2));

        List<DoctorResponse> result = doctorService.getAllActiveDoctors();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getNameEn()).isEqualTo("Dr. Ahmed Al-Rashid");
        assertThat(result.get(1).getSpecialty()).isEqualTo("General Practice");
        verify(doctorRepository).findByActiveTrue();
    }

    @Test
    @DisplayName("Should return doctor by ID")
    void shouldReturnDoctorById() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor1));

        DoctorResponse response = doctorService.getDoctorById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNameAr()).isEqualTo("د. أحمد الراشد");
        assertThat(response.getConsultationDurationMinutes()).isEqualTo(30);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when doctor not found")
    void shouldThrowWhenDoctorNotFound() {
        when(doctorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.getDoctorById(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Doctor not found");
    }
}