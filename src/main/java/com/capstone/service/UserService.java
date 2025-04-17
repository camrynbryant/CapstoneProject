package com.capstone.service;

import com.capstone.models.User;
import com.capstone.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory; 
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public ResponseEntity<?> authenticateUser(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body("Invalid email or password");
        }
        User user = userOptional.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body("Invalid email or password");
        }
        String token = generateToken(user);
        return ResponseEntity.ok(Map.of(
            "token", token,
            "email", user.getEmail(),
            "userId", user.getId()
        ));
    }
    
    public ResponseEntity<?> registerUser(String name, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body("Email is already in use");
        }
        String hashedPassword = passwordEncoder.encode(password);
        User user = new User(name, email, hashedPassword);
        
        try {
            logger.info("Saving user with email: {}", email);
            User savedUser = userRepository.save(user);
            logger.info("User saved with ID: {}", savedUser.getId());
            return ResponseEntity.ok("User registered successfully");
        } catch (Exception e) {
            logger.error("Error saving user with email: {}", email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Failed to register user due to database error.");
        }
    }
    
    @Transactional
    public ResponseEntity<?> updateUserStudyInterests(String email, Set<String> newInterests) {
        try {
            Optional<User> optionalUser = userRepository.findByEmail(email);
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
            User user = optionalUser.get();
            user.setStudyInterests(newInterests);
            userRepository.save(user);
            return ResponseEntity.ok("Study interests updated");
        } catch (Exception e) {
            logger.error("Failed to update study interests for email: {}", email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating study interests");
        }
    }
    
    private String generateToken(User user) {
        Key key = getSigningKey();
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("name", user.getName())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    
    private Key getSigningKey() {
        byte[] keyBytes = java.util.Base64.getDecoder().decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
