package com.clinic.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientRequest {

    @NotBlank(message = "Full name (English) is required")
    @Size(min = 2, max = 150, message = "Full name must be between 2 and 150 characters")
    private String fullNameEn;

    @NotBlank(message = "Full name (Arabic) is required")
    @Size(min = 2, max = 150, message = "Full name must be between 2 and 150 characters")
    private String fullNameAr;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{7,14}$", message = "Invalid mobile number format")
    private String mobileNumber;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotBlank(message = "National ID / Civil ID is required")
    @Size(min = 5, max = 20, message = "National ID must be between 5 and 20 characters")
    private String nationalId;

    private String street;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String region;
}