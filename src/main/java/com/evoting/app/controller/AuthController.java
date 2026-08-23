package com.evoting.app.controller;

import com.evoting.app.model.User;
import com.evoting.app.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String voterIdOrEmail = request.get("voterIdOrEmail");
        String password = request.get("password");

        if (voterIdOrEmail == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Voter ID/Email and password are required."));
        }

        Optional<User> userOpt = authService.authenticate(voterIdOrEmail, password);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("voterId", user.getVoterId());
            response.put("fullName", user.getFullName());
            response.put("email", user.getEmail());
            response.put("role", user.getRole());
            response.put("state", user.getState());
            response.put("constituency", user.getConstituency());
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(401).body(Map.of("success", false, "message", "Invalid credentials. Please check your Voter ID and password."));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            User registered = authService.registerUser(user);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Voter registration successful!",
                    "voterId", registered.getVoterId()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }
}
