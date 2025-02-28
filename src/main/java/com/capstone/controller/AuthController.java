package com.capstone.controller;

import com.capstone.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String email = request.get("email");
        String password = request.get("password");

        return userService.registerUser(name, email, password);
    }

    @PostMapping("/login")  
public ResponseEntity<?> loginUser(@RequestBody Map<String, String> request) {
    String email = request.get("email");
    String password = request.get("password");

    return userService.authenticateUser(email, password);
}

}
