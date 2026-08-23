package com.evoting.app.repository;

import com.evoting.app.model.VoteLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteLedgerRepository extends JpaRepository<VoteLedger, Long> {
    List<VoteLedger> findByElectionId(Long electionId);
    boolean existsByElectionIdAndPositionIdAndVoterHash(Long electionId, Long positionId, String voterHash);
    Optional<VoteLedger> findByReceiptCode(String receiptCode);
    Optional<VoteLedger> findTopByOrderByIdDesc();
    long countByElectionId(Long electionId);
}
