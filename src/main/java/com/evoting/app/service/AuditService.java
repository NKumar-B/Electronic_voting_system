package com.evoting.app.service;

import com.evoting.app.model.AuditLog;
import com.evoting.app.model.VoteLedger;
import com.evoting.app.repository.AuditLogRepository;
import com.evoting.app.repository.VoteLedgerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private VoteLedgerRepository voteLedgerRepository;

    @Autowired
    private CryptoService cryptoService;

    public void logAction(String actor, String action, String details, String ipAddress) {
        AuditLog log = new AuditLog(actor, action, details, ipAddress != null ? ipAddress : "127.0.0.1");
        auditLogRepository.save(log);
    }

    public List<AuditLog> getAuditLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }

    /**
     * Run full SHA-256 blockchain vote ledger audit to detect tampering.
     */
    public Map<String, Object> verifyChainIntegrity() {
        Map<String, Object> result = new HashMap<>();
        List<VoteLedger> chain = voteLedgerRepository.findAll();

        if (chain.isEmpty()) {
            result.put("status", "VALID");
            result.put("totalBlocks", 0);
            result.put("message", "Ledger is currently empty. No vote blocks to verify.");
            result.put("tampered", false);
            return result;
        }

        String expectedPrevHash = "0000000000000000000000000000000000000000000000000000000000000000";
        int validCount = 0;

        for (VoteLedger block : chain) {
            // Verify previous hash link
            if (!block.getPreviousHash().equals(expectedPrevHash)) {
                result.put("status", "TAMPERED");
                result.put("tampered", true);
                result.put("failedBlockId", block.getId());
                result.put("receiptCode", block.getReceiptCode());
                result.put("reason", "Previous hash mismatch at Block #" + block.getId() + ". Chain broken!");
                logAction("AUDITOR", "CHAIN_VERIFICATION_FAILED", "Broken link at block ID " + block.getId(), "127.0.0.1");
                return result;
            }

            // Recalculate current block hash
            String calculatedHash = cryptoService.computeBlockHash(
                    block.getPreviousHash(),
                    block.getElectionId(),
                    block.getPositionId(),
                    block.getCandidateId(),
                    block.getVoterHash(),
                    block.getTimestamp()
            );

            if (!block.getVoteHash().equals(calculatedHash)) {
                result.put("status", "TAMPERED");
                result.put("tampered", true);
                result.put("failedBlockId", block.getId());
                result.put("receiptCode", block.getReceiptCode());
                result.put("reason", "Block hash integrity failed at Block #" + block.getId() + ". Payload modified!");
                logAction("AUDITOR", "CHAIN_VERIFICATION_FAILED", "Data hash mismatch at block ID " + block.getId(), "127.0.0.1");
                return result;
            }

            expectedPrevHash = block.getVoteHash();
            validCount++;
        }

        result.put("status", "VALID");
        result.put("tampered", false);
        result.put("totalBlocks", validCount);
        result.put("message", "All " + validCount + " vote block hashes verified successfully. Cryptographic chain intact!");

        logAction("AUDITOR", "CHAIN_VERIFICATION_PASSED", "Successfully verified " + validCount + " ledger blocks.", "127.0.0.1");
        return result;
    }
}
