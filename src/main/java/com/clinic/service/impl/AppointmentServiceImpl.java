package com.clinic.service.impl;

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
import com.clinic.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    @Override
    @CacheEvict(value = "appointments", allEntries = true)
    public AppointmentResponse scheduleAppointment(AppointmentRequest request) {
        log.info("Scheduling appointment for patient {} with doctor {}",
            request.getPatientId(), request.getDoctorId());

        Patient patient = patientRepository.findById(request.getPatientId())
            .orElseThrow(() -> new ResourceNotFoundException("Patient", request.getPatientId()));

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
            .orElseThrow(() -> new ResourceNotFoundException("Doctor", request.getDoctorId()));

        // Check doctor availability - no overlapping appointments
        LocalDateTime start = request.getAppointmentDateTime();
        LocalDateTime end = start.plusMinutes(doctor.getConsultationDurationMinutes());

        List<Appointment> conflicts = appointmentRepository.findDoctorAppointmentsInRange(
            doctor.getId(), start.minusMinutes(doctor.getConsultationDurationMinutes()), end);

        if (!conflicts.isEmpty()) {
            throw new AppointmentConflictException("Doctor is not available at the requested time slot");
        }

        Appointment appointment = Appointment.builder()
            .patient(patient)
            .doctor(doctor)
            .appointmentDateTime(request.getAppointmentDateTime())
            .notes(request.getNotes())
            .build();

        appointment = appointmentRepository.save(appointment);
        log.info("Appointment scheduled with id: {}", appointment.getId());
        return toResponse(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "appointments", key = "#id")
    public AppointmentResponse getAppointmentById(Long id) {
        return toResponse(appointmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment", id)));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "appointments", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<AppointmentResponse> getAllAppointments(Pageable pageable) {
        return appointmentRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @CacheEvict(value = "appointments", allEntries = true)
    public AppointmentResponse updateAppointment(Long id, AppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment", id));

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
            .orElseThrow(() -> new ResourceNotFoundException("Doctor", request.getDoctorId()));

        appointment.setDoctor(doctor);
        appointment.setAppointmentDateTime(request.getAppointmentDateTime());
        appointment.setNotes(request.getNotes());

        return toResponse(appointmentRepository.save(appointment));
    }

    @Override
    @CacheEvict(value = "appointments", allEntries = true)
    public AppointmentResponse updateAppointmentStatus(Long id, Appointment.AppointmentStatus status) {
        Appointment appointment = appointmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment", id));
        appointment.setStatus(status);
        return toResponse(appointmentRepository.save(appointment));
    }

    @Override
    @CacheEvict(value = "appointments", allEntries = true)
    public void cancelAppointment(Long id) {
        updateAppointmentStatus(id, Appointment.AppointmentStatus.CANCELLED);
    }

    private AppointmentResponse toResponse(Appointment a) {
        return AppointmentResponse.builder()
            .id(a.getId())
            .patientId(a.getPatient().getId())
            .patientNameEn(a.getPatient().getFullNameEn())
            .doctorId(a.getDoctor().getId())
            .doctorNameEn(a.getDoctor().getNameEn())
            .doctorSpecialty(a.getDoctor().getSpecialty())
            .appointmentDateTime(a.getAppointmentDateTime())
            .status(a.getStatus())
            .notes(a.getNotes())
            .createdAt(a.getCreatedAt())
            .updatedAt(a.getUpdatedAt())
            .build();
    }
}