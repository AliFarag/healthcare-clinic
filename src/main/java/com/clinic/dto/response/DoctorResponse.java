package com.clinic.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorResponse {
    private Long id;
    private String nameEn;
    private String nameAr;
    private String specialty;
    private Integer yearsOfExperience;
    private Integer consultationDurationMinutes;
    private Boolean active;
}