package com.clinic.dto.response;

import com.clinic.entity.Appointment;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentResponse {
    private Long id;
    private Long patientId;
    private String patientNameEn;
    private Long doctorId;
    private String doctorNameEn;
    private String doctorSpecialty;
    private LocalDateTime appointmentDateTime;
    private Appointment.AppointmentStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}