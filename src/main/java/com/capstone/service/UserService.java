package com.capstone.service;

import com.capstone.models.User;
import com.capstone.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(); 

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ResponseEntity<?> registerUser(String name, String email, String password) {
        if (!isValidEmail(email)) {
            return ResponseEntity.badRequest().body("Invalid email format");
        }

        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body("Email is already in use");
        }

        if (!isValidPassword(password)) {
            return ResponseEntity.badRequest().body("Password must be at least 8 characters long");
        }

        String hashedPassword = passwordEncoder.encode(password);
        User user = new User(name, email, hashedPassword);
        
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 8;
    }
}