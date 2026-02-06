package com.taskflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.dto.AuthenticationRequest;
import com.taskflow.dto.AuthenticationResponse;
import com.taskflow.dto.RegisterRequest;
import com.taskflow.exception.UsernameAlreadyExistsException;
import com.taskflow.service.AuthenticationService;
import com.taskflow.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for this unit test
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private JwtService jwtService; // Add this mock to satisfy dependency injection for security config potential
                                   // scans

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_ShouldReturnOk_WhenRegistrationIsSuccessful() throws Exception {
        RegisterRequest request = new RegisterRequest("John", "Doe", "newuser", "password");

        AuthenticationResponse response = new AuthenticationResponse("jwtToken");
        when(authenticationService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwtToken"));
    }

    @Test
    void register_ShouldReturnConflict_WhenUsernameExists() throws Exception {
        RegisterRequest request = new RegisterRequest("John", "Doe", "existinguser", "password");

        when(authenticationService.register(any(RegisterRequest.class)))
                .thenThrow(new UsernameAlreadyExistsException("Username already exists"));

        // Note: This relies on the global exception handler logic you implemented
        // earlier
        // Ideally we should test the ControllerAdvice here too, but for unit test
        // scope:
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict()); // Assuming ExceptionHandler returns 409
    }

    @Test
    void authenticate_ShouldReturnOk_WhenCredentialsAreCorrect() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("user", "password");
        AuthenticationResponse response = new AuthenticationResponse("jwtToken");

        when(authenticationService.authenticate(any(AuthenticationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwtToken"));
    }
}
