package com.clinic.controller;

import com.clinic.dto.request.PatientRequest;
import com.clinic.dto.response.PatientResponse;
import com.clinic.repository.TokenBlacklistRepository;
import com.clinic.security.JwtAuthFilter;
import com.clinic.security.JwtUtil;
import com.clinic.service.PatientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = PatientController.class,
    excludeFilters = @ComponentScan.Filter(   // exclude JwtAuthFilter from being loaded
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthFilter.class
    )
)
@ActiveProfiles("test")
@DisplayName("PatientController Tests")
class PatientControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private PatientService patientService;
    @MockBean private JwtUtil jwtUtil;
    @MockBean private UserDetailsService userDetailsService;
    @MockBean private TokenBlacklistRepository tokenBlacklistRepository;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/patients - should register patient and return 201")
    void shouldRegisterPatient() throws Exception {
        PatientRequest request = PatientRequest.builder()
            .fullNameEn("Test Patient")
            .fullNameAr("مريض اختبار")
            .email("test@example.com")
            .mobileNumber("+96512345678")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .nationalId("1234567890")
            .city("Kuwait City")
            .region("Hawalli")
            .build();

        PatientResponse mockResponse = PatientResponse.builder()
            .id(1L)
            .fullNameEn("Test Patient")
            .email("test@example.com")
            .build();

        when(patientService.registerPatient(any())).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/patients")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/patients - should return 400 on invalid input")
    void shouldReturn400OnInvalidInput() throws Exception {
        PatientRequest invalidRequest = PatientRequest.builder()
            .fullNameEn("")           // blank - invalid
            .email("not-an-email")    // invalid email
            .build();

        mockMvc.perform(post("/api/v1/patients")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }
}
