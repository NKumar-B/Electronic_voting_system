package com.evoting.app.controller;

import com.evoting.app.model.Candidate;
import com.evoting.app.model.Election;
import com.evoting.app.model.Position;
import com.evoting.app.service.ElectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/elections")
@CrossOrigin(origins = "*")
public class ElectionController {

    @Autowired
    private ElectionService electionService;

    @GetMapping
    public ResponseEntity<List<Election>> getAllElections() {
        return ResponseEntity.ok(electionService.getAllElections());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getElectionById(@PathVariable Long id) {
        return electionService.getElectionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/ballot")
    public ResponseEntity<?> getElectionBallot(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(electionService.getElectionBallot(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<Election> createElection(@RequestBody Election election) {
        return ResponseEntity.ok(electionService.saveElection(election));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Status property is required."));
        }
        try {
            Election updated = electionService.updateElectionStatus(id, status);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{electionId}/positions")
    public ResponseEntity<Position> addPosition(@PathVariable Long electionId, @RequestBody Position position) {
        position.setElectionId(electionId);
        return ResponseEntity.ok(electionService.addPosition(position));
    }

    @PostMapping("/positions/{positionId}/candidates")
    public ResponseEntity<Candidate> addCandidate(@PathVariable Long positionId, @RequestBody Candidate candidate) {
        candidate.setPositionId(positionId);
        return ResponseEntity.ok(electionService.addCandidate(candidate));
    }
}
