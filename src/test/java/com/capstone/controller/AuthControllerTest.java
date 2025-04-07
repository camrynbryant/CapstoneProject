package com.capstone.controller;

import com.capstone.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerUser_whenSuccess_shouldReturnOk() throws Exception {
        Map<String, String> requestBody = Map.of(
                "name", "Test User",
                "email", "test@example.com",
                "password", "password123"
        );
        ResponseEntity<?> mockResponse = ResponseEntity.ok("User registered successfully");
        doReturn(mockResponse).when(userService).registerUser(anyString(), anyString(), anyString());

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully"));
    }

    @Test
    void registerUser_whenEmailExists_shouldReturnBadRequest() throws Exception {
        Map<String, String> requestBody = Map.of(
                "name", "Test User",
                "email", "test@example.com",
                "password", "password123"
        );
        ResponseEntity<?> mockResponse = ResponseEntity.badRequest().body("Email is already in use");
        doReturn(mockResponse).when(userService).registerUser(anyString(), anyString(), anyString());

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email is already in use"));
    }

     @Test
    void registerUser_whenServiceError_shouldReturnInternalServerError() throws Exception {
        Map<String, String> requestBody = Map.of(
                "name", "Test User",
                "email", "test@example.com",
                "password", "password123"
        );
        ResponseEntity<?> mockResponse = ResponseEntity.status(500).body("Database error");
        doReturn(mockResponse).when(userService).registerUser(anyString(), anyString(), anyString());

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isInternalServerError());
    }


    @Test
    void loginUser_whenSuccess_shouldReturnOkWithToken() throws Exception {
        Map<String, String> requestBody = Map.of(
                "email", "test@example.com",
                "password", "password123"
        );
        Map<String, String> responseMap = Map.of(
                "token", "dummy.jwt.token",
                "message", "Login successful"
        );
        ResponseEntity<?> mockResponse = ResponseEntity.ok(responseMap);
        doReturn(mockResponse).when(userService).authenticateUser(anyString(), anyString());

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("dummy.jwt.token"))
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    void loginUser_whenInvalidCredentials_shouldReturnUnauthorized() throws Exception {
        Map<String, String> requestBody = Map.of(
                "email", "test@example.com",
                "password", "wrongpassword"
        );
        ResponseEntity<?> mockResponse = ResponseEntity.status(401).body("Invalid email or password");
        doReturn(mockResponse).when(userService).authenticateUser(anyString(), anyString());

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid email or password"));
    }

     @Test
    void loginUser_whenServiceError_shouldReturnInternalServerError() throws Exception {
        Map<String, String> requestBody = Map.of(
                "email", "test@example.com",
                "password", "password123"
        );
        ResponseEntity<?> mockResponse = ResponseEntity.status(500).body("Authentication service error");
        doReturn(mockResponse).when(userService).authenticateUser(anyString(), anyString());

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isInternalServerError());
    }
}
