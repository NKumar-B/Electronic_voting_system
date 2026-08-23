package com.evoting.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String voterId;

    @Column(nullable = false)
    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role; // ADMIN, VOTER, AUDITOR

    private boolean verified = true;
    
    private String state;
    private String constituency;

    private LocalDateTime createdAt = LocalDateTime.now();

    public User() {}

    public User(String voterId, String fullName, String email, String password, String role, String state, String constituency) {
        this.voterId = voterId;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.state = state;
        this.constituency = constituency;
        this.verified = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getVoterId() { return voterId; }
    public void setVoterId(String voterId) { this.voterId = voterId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getConstituency() { return constituency; }
    public void setConstituency(String constituency) { this.constituency = constituency; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
