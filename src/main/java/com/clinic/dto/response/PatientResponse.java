package com.clinic.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientResponse {
    private Long id;
    private String fullNameEn;
    private String fullNameAr;
    private String email;
    private String mobileNumber;
    private LocalDate dateOfBirth;
    private String nationalId;
    private String street;
    private String city;
    private String region;
    private LocalDateTime createdAt;
    private List<AppointmentResponse> appointments;
}