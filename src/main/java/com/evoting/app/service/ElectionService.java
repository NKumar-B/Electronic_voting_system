package com.evoting.app.service;

import com.evoting.app.model.Candidate;
import com.evoting.app.model.Election;
import com.evoting.app.model.Position;
import com.evoting.app.repository.CandidateRepository;
import com.evoting.app.repository.ElectionRepository;
import com.evoting.app.repository.PositionRepository;
import com.evoting.app.repository.VoteLedgerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ElectionService {

    @Autowired
    private ElectionRepository electionRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private VoteLedgerRepository voteLedgerRepository;

    @Autowired
    private AuditService auditService;

    public List<Election> getAllElections() {
        return electionRepository.findAll();
    }

    public Optional<Election> getElectionById(Long id) {
        return electionRepository.findById(id);
    }

    public List<Election> getElectionsByStatus(String status) {
        return electionRepository.findByStatus(status);
    }

    public Election saveElection(Election election) {
        Election saved = electionRepository.save(election);
        auditService.logAction("ADMIN", "ELECTION_CREATE", "Created election: " + election.getTitle(), "127.0.0.1");
        return saved;
    }

    public Election updateElectionStatus(Long id, String status) {
        Election election = electionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Election not found with ID: " + id));
        election.setStatus(status.toUpperCase());
        Election saved = electionRepository.save(election);
        auditService.logAction("ADMIN", "ELECTION_STATUS_CHANGE", "Updated election " + id + " status to " + status, "127.0.0.1");
        return saved;
    }

    public Position addPosition(Position position) {
        return positionRepository.save(position);
    }

    public List<Position> getPositionsByElection(Long electionId) {
        return positionRepository.findByElectionId(electionId);
    }

    public Candidate addCandidate(Candidate candidate) {
        return candidateRepository.save(candidate);
    }

    public List<Candidate> getCandidatesByPosition(Long positionId) {
        return candidateRepository.findByPositionId(positionId);
    }

    /**
     * Get detailed ballot view for an election (positions with candidates)
     */
    public Map<String, Object> getElectionBallot(Long electionId) {
        Election election = electionRepository.findById(electionId)
                .orElseThrow(() -> new IllegalArgumentException("Election not found with ID: " + electionId));

        List<Position> positions = positionRepository.findByElectionId(electionId);
        List<Map<String, Object>> positionDetails = new ArrayList<>();

        for (Position pos : positions) {
            Map<String, Object> posMap = new HashMap<>();
            posMap.put("id", pos.getId());
            posMap.put("title", pos.getTitle());
            posMap.put("maxChoices", pos.getMaxChoices());
            posMap.put("candidates", candidateRepository.findByPositionId(pos.getId()));
            positionDetails.add(posMap);
        }

        Map<String, Object> ballot = new HashMap<>();
        ballot.put("election", election);
        ballot.put("positions", positionDetails);
        ballot.put("totalVotesCast", voteLedgerRepository.countByElectionId(electionId));

        return ballot;
    }
}
