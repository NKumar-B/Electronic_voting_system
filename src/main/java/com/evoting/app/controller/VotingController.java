package com.evoting.app.controller;

import com.evoting.app.model.VoteLedger;
import com.evoting.app.service.VotingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/vote")
@CrossOrigin(origins = "*")
public class VotingController {

    @Autowired
    private VotingService votingService;

    @PostMapping("/cast")
    public ResponseEntity<?> castVote(@RequestBody Map<String, Object> payload) {
        try {
            String voterId = (String) payload.get("voterId");
            Long electionId = Long.valueOf(payload.get("electionId").toString());
            Long positionId = Long.valueOf(payload.get("positionId").toString());
            Long candidateId = Long.valueOf(payload.get("candidateId").toString());

            if (voterId == null || electionId == null || positionId == null || candidateId == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Missing required ballot parameters."));
            }

            VoteLedger block = votingService.castVote(voterId, electionId, positionId, candidateId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Vote recorded successfully in cryptographic chain!",
                    "receiptCode", block.getReceiptCode(),
                    "voteHash", block.getVoteHash(),
                    "previousHash", block.getPreviousHash(),
                    "timestamp", block.getTimestamp().toString()
            ));

        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Server error processing vote: " + e.getMessage()));
        }
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkVoted(
            @RequestParam String voterId,
            @RequestParam Long electionId,
            @RequestParam Long positionId) {

        boolean hasVoted = votingService.hasVoted(voterId, electionId, positionId);
        return ResponseEntity.ok(Map.of("voterId", voterId, "hasVoted", hasVoted));
    }

    @GetMapping("/verify-receipt/{receiptCode}")
    public ResponseEntity<?> verifyReceipt(@PathVariable String receiptCode) {
        Optional<VoteLedger> ledgerOpt = votingService.verifyReceipt(receiptCode);

        if (ledgerOpt.isPresent()) {
            VoteLedger block = ledgerOpt.get();
            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "receiptCode", block.getReceiptCode(),
                    "voteHash", block.getVoteHash(),
                    "previousHash", block.getPreviousHash(),
                    "electionId", block.getElectionId(),
                    "timestamp", block.getTimestamp().toString(),
                    "message", "Cryptographic receipt verified! Vote exists in chain."
            ));
        }

        return ResponseEntity.status(404).body(Map.of(
                "valid", false,
                "message", "Receipt code not found in cryptographic chain ledger."
        ));
    }
}
