package com.evoting.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vote_ledger")
public class VoteLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long electionId;

    @Column(nullable = false)
    private Long positionId;

    @Column(nullable = false)
    private Long candidateId;

    @Column(nullable = false)
    private String voterHash; // SHA-256 (voterId + salt) to check single vote anonymously

    @Column(nullable = false)
    private String previousHash; // Previous block hash in immutable SHA-256 vote chain

    @Column(nullable = false)
    private String voteHash; // SHA-256 (previousHash + electionId + positionId + candidateId + voterHash + timestamp)

    @Column(nullable = false, unique = true)
    private String receiptCode; // Verifiable code for voter receipt

    private LocalDateTime timestamp = LocalDateTime.now();

    public VoteLedger() {}

    public VoteLedger(Long electionId, Long positionId, Long candidateId, String voterHash, String previousHash, String voteHash, String receiptCode) {
        this.electionId = electionId;
        this.positionId = positionId;
        this.candidateId = candidateId;
        this.voterHash = voterHash;
        this.previousHash = previousHash;
        this.voteHash = voteHash;
        this.receiptCode = receiptCode;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getElectionId() { return electionId; }
    public void setElectionId(Long electionId) { this.electionId = electionId; }

    public Long getPositionId() { return positionId; }
    public void setPositionId(Long positionId) { this.positionId = positionId; }

    public Long getCandidateId() { return candidateId; }
    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }

    public String getVoterHash() { return voterHash; }
    public void setVoterHash(String voterHash) { this.voterHash = voterHash; }

    public String getPreviousHash() { return previousHash; }
    public void setPreviousHash(String previousHash) { this.previousHash = previousHash; }

    public String getVoteHash() { return voteHash; }
    public void setVoteHash(String voteHash) { this.voteHash = voteHash; }

    public String getReceiptCode() { return receiptCode; }
    public void setReceiptCode(String receiptCode) { this.receiptCode = receiptCode; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
