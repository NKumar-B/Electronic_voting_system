package com.evoting.app.service;

import com.evoting.app.model.Candidate;
import com.evoting.app.model.Election;
import com.evoting.app.model.VoteLedger;
import com.evoting.app.repository.CandidateRepository;
import com.evoting.app.repository.ElectionRepository;
import com.evoting.app.repository.VoteLedgerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class VotingService {

    @Autowired
    private VoteLedgerRepository voteLedgerRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private ElectionRepository electionRepository;

    @Autowired
    private CryptoService cryptoService;

    @Autowired
    private AuditService auditService;

    /**
     * Cast vote securely and atomically in SHA-256 chain
     */
    @Transactional
    public synchronized VoteLedger castVote(String voterId, Long electionId, Long positionId, Long candidateId) {
        // 1. Verify election status
        Election election = electionRepository.findById(electionId)
                .orElseThrow(() -> new IllegalArgumentException("Election not found with ID: " + electionId));

        if (!"ACTIVE".equalsIgnoreCase(election.getStatus())) {
            throw new IllegalStateException("Election is not currently ACTIVE for voting.");
        }

        // 2. Check candidate existence
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found with ID: " + candidateId));

        // 3. Compute salted voter hash to check double voting without identity leakage
        String voterHash = cryptoService.generateVoterHash(voterId, electionId, positionId);

        boolean alreadyVoted = voteLedgerRepository.existsByElectionIdAndPositionIdAndVoterHash(electionId, positionId, voterHash);
        if (alreadyVoted) {
            auditService.logAction(voterId, "DOUBLE_VOTE_ATTEMPT", "Attempted duplicate vote for election " + electionId, "127.0.0.1");
            throw new IllegalStateException("Voter has already cast a ballot for this position in this election.");
        }

        // 4. Fetch previous block hash from ledger chain
        Optional<VoteLedger> lastBlockOpt = voteLedgerRepository.findTopByOrderByIdDesc();
        String previousHash = lastBlockOpt.map(VoteLedger::getVoteHash)
                .orElse("0000000000000000000000000000000000000000000000000000000000000000");

        // 5. Generate unique digital receipt code
        String receiptCode = cryptoService.generateReceiptCode();
        LocalDateTime now = LocalDateTime.now();

        // 6. Calculate new block SHA-256 hash
        String voteHash = cryptoService.computeBlockHash(
                previousHash,
                electionId,
                positionId,
                candidateId,
                voterHash,
                now
        );

        // 7. Save Vote Ledger Block
        VoteLedger block = new VoteLedger(electionId, positionId, candidateId, voterHash, previousHash, voteHash, receiptCode);
        block.setTimestamp(now);
        VoteLedger savedBlock = voteLedgerRepository.save(block);

        // 8. Increment candidate vote count
        candidate.setVoteCount(candidate.getVoteCount() + 1);
        candidateRepository.save(candidate);

        // 9. Log audit action
        auditService.logAction("ANONYMOUS_VOTER", "VOTE_CAST_SUCCESS", "Vote cast successfully under receipt: " + receiptCode, "127.0.0.1");

        return savedBlock;
    }

    /**
     * Check if voter has voted for a position
     */
    public boolean hasVoted(String voterId, Long electionId, Long positionId) {
        String voterHash = cryptoService.generateVoterHash(voterId, electionId, positionId);
        return voteLedgerRepository.existsByElectionIdAndPositionIdAndVoterHash(electionId, positionId, voterHash);
    }

    /**
     * Verify voter receipt code
     */
    public Optional<VoteLedger> verifyReceipt(String receiptCode) {
        return voteLedgerRepository.findByReceiptCode(receiptCode);
    }
}
