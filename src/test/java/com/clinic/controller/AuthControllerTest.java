package com.clinic.controller;

import com.clinic.config.SecurityConfig;
import com.clinic.dto.request.LoginRequest;
import com.clinic.dto.response.AuthResponse;
import com.clinic.exception.GlobalExceptionHandler;
import com.clinic.repository.TokenBlacklistRepository;
import com.clinic.security.JwtAuthFilter;
import com.clinic.security.JwtUtil;
import com.clinic.service.impl.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    {AuthController.class, GlobalExceptionHandler.class}
)
@Import(SecurityConfig.class)              // import so PUBLIC_URLS rule is applied
@ActiveProfiles("test")
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AuthService authService;
    @MockBean private JwtUtil jwtUtil;
    @MockBean private UserDetailsService userDetailsService;
    @MockBean private TokenBlacklistRepository tokenBlacklistRepository;
    @MockBean private JwtAuthFilter jwtAuthFilter;
    @MockBean private AuthenticationManager authenticationManager;

    @Test
    @DisplayName("POST /auth/login - should return JWT on valid credentials")
    void shouldLoginSuccessfully() throws Exception {
        // No @WithMockUser needed — /api/v1/auth/** is public in SecurityConfig
        LoginRequest request = new LoginRequest("admin", "Admin@123");

        AuthResponse mockResponse = AuthResponse.builder()
            .accessToken("mock.jwt.token")
            .tokenType("Bearer")
            .expiresIn(86400L)
            .username("admin")
            .role("ADMIN")
            .build();

        when(authService.login(any())).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.accessToken").value("mock.jwt.token"))
            .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("POST /auth/login - should return 401 on bad credentials")
    void shouldReturn401OnBadCredentials() throws Exception {
        LoginRequest request = new LoginRequest("admin", "wrongpassword");

        when(authService.login(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /auth/login - should return 400 when fields are blank")
    void shouldReturn400WhenFieldsBlank() throws Exception {
        LoginRequest request = new LoginRequest("", "");

        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }
}
