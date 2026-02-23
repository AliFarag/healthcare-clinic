package com.clinic.controller;

import com.clinic.dto.request.AppointmentRequest;
import com.clinic.dto.response.AppointmentResponse;
import com.clinic.entity.Appointment;
import com.clinic.service.AppointmentService;
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

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AppointmentController Tests")
class AppointmentControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AppointmentService appointmentService;

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
