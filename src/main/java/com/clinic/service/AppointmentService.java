package com.clinic.service;

import com.clinic.dto.request.AppointmentRequest;
import com.clinic.dto.response.AppointmentResponse;
import com.clinic.entity.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AppointmentService {
    AppointmentResponse scheduleAppointment(AppointmentRequest request);
    AppointmentResponse getAppointmentById(Long id);
    Page<AppointmentResponse> getAllAppointments(Pageable pageable);
    AppointmentResponse updateAppointment(Long id, AppointmentRequest request);
    AppointmentResponse updateAppointmentStatus(Long id, Appointment.AppointmentStatus status);
    void cancelAppointment(Long id);
}