package com.capstone.service;

import com.capstone.models.User;
import com.capstone.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ResponseEntity<?> authenticateUser(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(401).body("Invalid email or password");
        }

        User user = userOptional.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(401).body("Invalid email or password");
        }

        String token = generateToken(user);

        return ResponseEntity.ok(Map.of("token", token, "message", "Login successful"));
    }

    public ResponseEntity<?> registerUser(String name, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body("Email is already in use");
        }

        String hashedPassword = passwordEncoder.encode(password);
        User user = new User(name, email, hashedPassword);

        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    private Key getSigningKey() {
        return Keys.secretKeyFor(SignatureAlgorithm.HS256); 
    }

    private String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("name", user.getName())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) 
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}
