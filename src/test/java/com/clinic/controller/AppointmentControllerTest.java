package com.clinic.controller;

import com.clinic.dto.request.PatientRequest;
import com.clinic.dto.response.PatientResponse;
import com.clinic.service.PatientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("PatientController Tests")
class PatientControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private PatientService patientService;

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
