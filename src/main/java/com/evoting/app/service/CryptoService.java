package com.evoting.app.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
public class CryptoService {

    private static final String SYSTEM_SALT = "SECURE_VOTE_CRYPTOGRAPHIC_SALT_2026";

    /**
     * Generate SHA-256 Hash of string input
     */
    public String sha256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Compute salted hash for a voter to check double voting without linking voter identity to specific candidate choice.
     */
    public String generateVoterHash(String voterId, Long electionId, Long positionId) {
        return sha256(voterId + ":" + electionId + ":" + positionId + ":" + SYSTEM_SALT);
    }

    private static final java.time.format.DateTimeFormatter TIMESTAMP_FORMATTER = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String formatTimestamp(java.time.LocalDateTime timestamp) {
        return timestamp != null ? timestamp.format(TIMESTAMP_FORMATTER) : "";
    }

    /**
     * Compute cryptographic vote block hash in chain
     */
    public String computeBlockHash(String previousHash, Long electionId, Long positionId, Long candidateId, String voterHash, java.time.LocalDateTime timestamp) {
        String formattedTime = formatTimestamp(timestamp);
        String payload = previousHash + "|" + electionId + "|" + positionId + "|" + candidateId + "|" + voterHash + "|" + formattedTime;
        return sha256(payload);
    }

    /**
     * Generate verifiable digital receipt code for voter
     */
    public String generateReceiptCode() {
        return "VOTE-REC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() + "-" + System.currentTimeMillis() % 10000;
    }
}
