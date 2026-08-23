package com.evoting.app.service;

import com.evoting.app.model.*;
import com.evoting.app.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ElectionRepository electionRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private VotingService votingService;

    @Autowired
    private AuditService auditService;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            return; // Data already initialized
        }

        System.out.println("\n [DataInitializer] Initializing SecureVote Seed Data & Cryptographic Chain...");

        // 1. Create Default Users
        User admin = new User("VOTER-ADMIN-001", "System Administrator", "admin@securevote.gov", "admin123", "ADMIN", "Capital Federal", "District 1");
        User auditor = new User("VOTER-AUDIT-001", "Chief Security Auditor", "auditor@securevote.gov", "audit123", "AUDITOR", "Capital Federal", "District 1");
        User voter1 = new User("VOTER-1001", "Alice Vance", "alice@example.com", "voter123", "VOTER", "California", "District 12");
        User voter2 = new User("VOTER-1002", "Bob Sterling", "bob@example.com", "voter123", "VOTER", "New York", "District 4");
        User voter3 = new User("VOTER-1003", "Carol Danvers", "carol@example.com", "voter123", "VOTER", "Texas", "District 7");
        User voter4 = new User("VOTER-1004", "David Miller", "david@example.com", "voter123", "VOTER", "Washington", "District 2");

        authService.registerUser(admin);
        authService.registerUser(auditor);
        authService.registerUser(voter1);
        authService.registerUser(voter2);
        authService.registerUser(voter3);
        authService.registerUser(voter4);

        // 2. Create Active National Election
        Election e1 = new Election(
                "2026 Presidential & General Assembly Election",
                "Official electronic election for National Executive leadership and Technology Audit Oversight.",
                "National",
                "ACTIVE",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(5)
        );
        e1 = electionRepository.save(e1);

        // Positions for Election 1
        Position p1 = positionRepository.save(new Position(e1.getId(), "President of the Federation", 1));
        Position p2 = positionRepository.save(new Position(e1.getId(), "Chief Technology Auditor", 1));

        // Candidates for Position 1 (President)
        Candidate c1 = candidateRepository.save(new Candidate(
                p1.getId(),
                "Dr. Elena Rostova",
                "Progressive Innovation Alliance",
                "Championing digital transparency, green energy transition, and tech-driven public infrastructure.",
                "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=150"
        ));
        Candidate c2 = candidateRepository.save(new Candidate(
                p1.getId(),
                "Marcus Vance",
                "National Unity Coalition",
                "Strengthening economic resilience, national security, and balanced fiscal management.",
                "https://images.unsplash.com/photo-1560250097-0b93528c311a?w=150"
        ));
        Candidate c3 = candidateRepository.save(new Candidate(
                p1.getId(),
                "Sophia Lin",
                "Future Forward Coalition",
                "Pioneering universal education reform, AI ethics oversight, and healthcare accessibility.",
                "https://images.unsplash.com/photo-1580489944761-15a19d654956?w=150"
        ));

        // Candidates for Position 2 (Tech Auditor)
        Candidate c4 = candidateRepository.save(new Candidate(
                p2.getId(),
                "Dr. Alexander Wright",
                "Cyber Integrity Alliance",
                "Auditing open source voting infrastructure and cryptographic proof systems.",
                "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150"
        ));
        Candidate c5 = candidateRepository.save(new Candidate(
                p2.getId(),
                "Maya Lin",
                "Tech Integrity Forum",
                "Enforcing zero-trust architecture across all municipal databases.",
                "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150"
        ));

        // 3. Create Active University Election
        Election e2 = new Election(
                "University Student Council General Election 2026",
                "Annual election for Student Body Governance and Campus Budget Allocation.",
                "University",
                "ACTIVE",
                LocalDateTime.now().minusHours(12),
                LocalDateTime.now().plusDays(2)
        );
        e2 = electionRepository.save(e2);

        Position p3 = positionRepository.save(new Position(e2.getId(), "Student Body President", 1));
        Candidate c6 = candidateRepository.save(new Candidate(
                p3.getId(),
                "Liam Henderson",
                "Campus Voice United",
                "Expanding campus dining options, 24/7 library access, and mental health support.",
                "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=150"
        ));
        Candidate c7 = candidateRepository.save(new Candidate(
                p3.getId(),
                "Ananya Sharma",
                "Innovate Campus",
                "Sustainable zero-waste campus initiative and tech incubator grants for students.",
                "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=150"
        ));

        // 4. Create Upcoming Election
        Election e3 = new Election(
                "Municipal Clean Energy & Transit Ballot 2026",
                "Public referendum on municipal solar grid investment and electric bus expansion.",
                "Local",
                "UPCOMING",
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(15)
        );
        electionRepository.save(e3);

        // 5. Pre-cast seed votes into SHA-256 ledger chain for realistic initial metrics
        System.out.println(" [DataInitializer] Seed casting cryptographic vote blocks...");
        votingService.castVote("VOTER-1001", e1.getId(), p1.getId(), c1.getId());
        votingService.castVote("VOTER-1002", e1.getId(), p1.getId(), c1.getId());
        votingService.castVote("VOTER-1003", e1.getId(), p1.getId(), c2.getId());
        votingService.castVote("VOTER-1004", e1.getId(), p1.getId(), c3.getId());

        votingService.castVote("VOTER-1001", e1.getId(), p2.getId(), c4.getId());
        votingService.castVote("VOTER-1002", e1.getId(), p2.getId(), c5.getId());

        votingService.castVote("VOTER-1001", e2.getId(), p3.getId(), c6.getId());

        auditService.logAction("SYSTEM", "INITIALIZATION_COMPLETE", "SecureVote initialized with default users, 3 elections, and SHA-256 vote chain ledger.", "127.0.0.1");

        System.out.println(" [DataInitializer] SecureVote Seed Data Initialized Successfully!\n");
    }
}
