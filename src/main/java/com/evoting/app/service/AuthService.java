package com.evoting.app.service;

import com.evoting.app.model.User;
import com.evoting.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CryptoService cryptoService;

    @Autowired
    private AuditService auditService;

    public User registerUser(User user) {
        if (userRepository.existsByVoterId(user.getVoterId())) {
            throw new IllegalArgumentException("Voter ID already registered: " + user.getVoterId());
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + user.getEmail());
        }

        // Hash password with SHA-256 for demo storage
        user.setPassword(cryptoService.sha256(user.getPassword()));
        User saved = userRepository.save(user);

        auditService.logAction(user.getVoterId(), "USER_REGISTER", "Registered new user with role: " + user.getRole(), "127.0.0.1");
        return saved;
    }

    public Optional<User> authenticate(String voterIdOrEmail, String rawPassword) {
        String hashedPassword = cryptoService.sha256(rawPassword);
        
        Optional<User> userOpt = userRepository.findByVoterId(voterIdOrEmail);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(voterIdOrEmail);
        }

        if (userOpt.isPresent() && userOpt.get().getPassword().equals(hashedPassword)) {
            auditService.logAction(userOpt.get().getVoterId(), "USER_LOGIN_SUCCESS", "User logged in successfully", "127.0.0.1");
            return userOpt;
        }

        auditService.logAction("GUEST", "USER_LOGIN_FAILED", "Failed login attempt for: " + voterIdOrEmail, "127.0.0.1");
        return Optional.empty();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findByVoterId(String voterId) {
        return userRepository.findByVoterId(voterId);
    }
}
