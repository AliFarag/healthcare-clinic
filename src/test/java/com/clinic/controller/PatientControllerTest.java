package com.clinic.controller;

import com.clinic.dto.request.AppointmentRequest;
import com.clinic.dto.response.AppointmentResponse;
import com.clinic.entity.Appointment;
import com.clinic.repository.TokenBlacklistRepository;
import com.clinic.security.JwtAuthFilter;
import com.clinic.security.JwtUtil;
import com.clinic.service.AppointmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = AppointmentController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthFilter.class
    )
)
@ActiveProfiles("test")
@DisplayName("AppointmentController Tests")
class AppointmentControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private AppointmentService appointmentService;
    @MockBean private JwtUtil jwtUtil;
    @MockBean private UserDetailsService userDetailsService;
    @MockBean private TokenBlacklistRepository tokenBlacklistRepository;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /appointments - should schedule appointment and return 201")
    void shouldScheduleAppointment() throws Exception {
        AppointmentRequest request = AppointmentRequest.builder()
            .patientId(1L)
            .doctorId(1L)
            .appointmentDateTime(LocalDateTime.now().plusDays(3))
            .notes("Follow-up visit")
            .build();

        AppointmentResponse mockResponse = AppointmentResponse.builder()
            .id(1L)
            .patientId(1L)
            .doctorId(1L)
            .status(Appointment.AppointmentStatus.SCHEDULED)
            .build();

        when(appointmentService.scheduleAppointment(any())).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/appointments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.status").value("SCHEDULED"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /appointments - should return 400 when patientId is null")
    void shouldReturn400WhenPatientIdNull() throws Exception {
        AppointmentRequest request = AppointmentRequest.builder()
            .doctorId(1L)
            .appointmentDateTime(LocalDateTime.now().plusDays(1))
            // patientId intentionally missing → @NotNull should trigger
            .build();

        mockMvc.perform(post("/api/v1/appointments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }
}
