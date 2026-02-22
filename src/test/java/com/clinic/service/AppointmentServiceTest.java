package com.clinic.service;

import com.clinic.dto.request.AppointmentRequest;
import com.clinic.dto.response.AppointmentResponse;
import com.clinic.entity.Appointment;
import com.clinic.entity.Doctor;
import com.clinic.entity.Patient;
import com.clinic.exception.AppointmentConflictException;
import com.clinic.exception.ResourceNotFoundException;
import com.clinic.repository.AppointmentRepository;
import com.clinic.repository.DoctorRepository;
import com.clinic.repository.PatientRepository;
import com.clinic.service.impl.AppointmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentService Tests")
class AppointmentServiceTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private DoctorRepository doctorRepository;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private Patient patient;
    private Doctor doctor;
    private AppointmentRequest request;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        patient = Patient.builder().id(1L).fullNameEn("Jane Smith").email("jane@example.com")
            .dateOfBirth(LocalDate.of(1985, 5, 20)).nationalId("9876543").build();

        doctor = Doctor.builder().id(1L).nameEn("Dr. Ahmed").nameAr("د. أحمد")
            .specialty("Cardiology").yearsOfExperience(10)
            .consultationDurationMinutes(30).active(true).build();

        request = AppointmentRequest.builder()
            .patientId(1L).doctorId(1L)
            .appointmentDateTime(LocalDateTime.now().plusDays(1))
            .notes("Initial consultation")
            .build();

        appointment = Appointment.builder()
            .id(1L).patient(patient).doctor(doctor)
            .appointmentDateTime(request.getAppointmentDateTime())
            .status(Appointment.AppointmentStatus.SCHEDULED)
            .notes("Initial consultation")
            .build();
    }

    @Test
    @DisplayName("Should schedule appointment when no conflicts")
    void shouldScheduleAppointment() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findDoctorAppointmentsInRange(any(), any(), any()))
            .thenReturn(Collections.emptyList());
        when(appointmentRepository.save(any())).thenReturn(appointment);

        AppointmentResponse response = appointmentService.scheduleAppointment(request);

        assertThat(response).isNotNull();
        assertThat(response.getDoctorNameEn()).isEqualTo("Dr. Ahmed");
        assertThat(response.getStatus()).isEqualTo(Appointment.AppointmentStatus.SCHEDULED);
        verify(appointmentRepository).save(any());
    }

    @Test
    @DisplayName("Should throw AppointmentConflictException when doctor is busy")
    void shouldThrowOnConflict() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findDoctorAppointmentsInRange(any(), any(), any()))
            .thenReturn(List.of(appointment));  // Conflict!

        assertThatThrownBy(() -> appointmentService.scheduleAppointment(request))
            .isInstanceOf(AppointmentConflictException.class)
            .hasMessageContaining("not available");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when patient not found")
    void shouldThrowWhenPatientNotFound() {
        when(patientRepository.findById(999L)).thenReturn(Optional.empty());
        request.setPatientId(999L);

        assertThatThrownBy(() -> appointmentService.scheduleAppointment(request))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}