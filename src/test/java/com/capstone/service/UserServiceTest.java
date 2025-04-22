package com.capstone.service;

import com.capstone.models.User;
import com.capstone.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Spy
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private AutoCloseable closeable;

@BeforeEach
void setup() {
    closeable = MockitoAnnotations.openMocks(this);
    userService = new UserService(userRepository);
    String longSecret = "thisisanincrediblylongandsecuresecretkey12345678";
    TestUtils.setField(userService, "jwtSecret", Base64.getEncoder().encodeToString(longSecret.getBytes()));
}

    @Test
    void testAuthenticateUser_validCredentials_returnsToken() {
        String email = "user@example.com";
        String password = "password123";
        String hashedPassword = passwordEncoder.encode(password);
        User user = new User("Test User", email, hashedPassword);
        user.setId("1");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        ResponseEntity<?> response = userService.authenticateUser(email, password);
        assertEquals(200, response.getStatusCodeValue());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertTrue(responseBody.containsKey("token"));
        assertEquals(email, responseBody.get("email"));
    }

    @Test
    void testAuthenticateUser_invalidPassword_returnsUnauthorized() {
        String email = "user@example.com";
        User user = new User("Test User", email, passwordEncoder.encode("correctpassword"));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        ResponseEntity<?> response = userService.authenticateUser(email, "wrongpassword");
        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Invalid email or password", response.getBody());
    }

    @Test
    void testRegisterUser_emailExists_returnsBadRequest() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        ResponseEntity<?> response = userService.registerUser("Name", "user@example.com", "password123");

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Email is already in use", response.getBody());
    }

    @Test
    void testRegisterUser_success_returnsOk() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);

        User user = new User("Name", "user@example.com", "encodedPassword");
        user.setId("1");
        when(userRepository.save(any(User.class))).thenReturn(user);

        ResponseEntity<?> response = userService.registerUser("Name", "user@example.com", "password123");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("User registered successfully", response.getBody());
    }

    @Test
    void testUpdateUserStudyInterests_success() {
        String email = "user@example.com";
        Set<String> interests = Set.of("Math", "Science");
        User user = new User("Name", email, "password");
        user.setId("1");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        ResponseEntity<?> response = userService.updateUserStudyInterests(email, interests);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Study interests updated", response.getBody());
    }

    @Test
    void testUpdateUserStudyInterests_userNotFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        ResponseEntity<?> response = userService.updateUserStudyInterests("missing@example.com", Set.of("Tech"));
        assertEquals(404, response.getStatusCodeValue());
        assertEquals("User not found", response.getBody());
    }
}
