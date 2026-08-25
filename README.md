# 🛡️ SecureVote - Cryptographic Full-Stack Java Electronic Voting System

[![Java Version](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Database](https://img.shields.io/badge/Database-H2%20In--Memory-blue.svg)](https://www.h2database.com/)
[![License](https://img.shields.io/badge/License-MIT-purple.svg)](#license)

**SecureVote** is an enterprise-grade full-stack Java web application designed for secure, transparent, and tamper-proof electronic elections. The system utilizes an **anonymous SHA-256 vote chain ledger** (blockchain-inspired) to guarantee absolute vote secrecy, single-ballot enforcement per voter, verifiable digital receipts, real-time result analytics, and automated cryptographic audit verification.

---
 
## 🌟 Key Features

### 🔒 1. SHA-256 Cryptographic Chain Ledger
* **Immutable Block Sequence**: Each cast vote is cryptographically linked to the previous vote block hash (`previousHash` -> `voteHash`), forming a tamper-proof chain.
* **Salted Voter Anonymization**: Voters produce a salted hash `sha256(voterId + electionId + positionId + salt)` to prevent double voting while ensuring candidate choices remain completely unlinked from voter identities.
* **Verifiable Digital Receipts**: Voters receive a unique receipt code (e.g., `VOTE-REC-D46D519D-9389`) to publicly verify that their vote block exists in the ledger without exposing their ballot choices.

### 🔬 2. Auditor & Security Oversight Center
* **Block-by-Block Audit Engine**: Scans every ledger block to verify cryptographic parent links and payload hashes.
* **Tamper Detection**: If database records are illegally altered, the Auditor Portal immediately flags the broken block index and alerts security personnel.
* **System Activity Logs**: Logs system-wide actions with timestamps, actor IDs, actions, and IP addresses.

### 🗳️ 3. Digital Voting Booth (Interactive Wizard)
* **4-Step Guided Voting**:
  1. *Voter Verification*: Authorization & constituency check.
  2. *Ballot Selection*: Interactive candidate cards with party manifestos and photos.
  3. *Review & Cryptographic Sign*: Final selection review before ledger signing.
  4. *Cryptographic Receipt Ticket*: Printable digital receipt with block hash details.

### 📊 4. Real-Time Results Analytics
* **Dynamic Chart.js Integration**: Live visual bar charts showing vote shares per candidate.
* **Metrics Panel**: Instant updates for total votes cast, turnout percentage, and leading candidate indicators.
* **Candidate Tally Table**: Detailed percentage breakdown per office.

### ⚙️ 5. Multi-Role Management
* **Voter**: View elections, cast ballots in active elections, verify receipt codes.
* **Administrator**: Create elections, add positions/candidates, toggle election states (`UPCOMING`, `ACTIVE`, `PAUSED`, `CLOSED`).
* **Auditor**: Execute SHA-256 cryptographic chain audits and review security logs.

---

## 🏗️ System Architecture

```
                          ┌──────────────────────────────────────┐
                          │    SecureVote Web Portal (Frontend)  │
                          │ HTML5 / Glassmorphism CSS / App.js   │
                          └──────────────────┬───────────────────┘
                                             │ REST API (JSON)
                          ┌──────────────────▼───────────────────┐
                          │       Spring Boot 3 Web Application  │
                          │ ┌──────────────────────────────────┐ │
                          │ │ Controllers (Auth, Election, Vote)│ │
                          │ ├──────────────────────────────────┤ │
                          │ │ Services (Crypto, Ledger, Audit) │ │
                          │ ├──────────────────────────────────┤ │
                          │ │ Repositories (Spring Data JPA)   │ │
                          │ └──────────────────────────────────┘ │
                          └──────────────────┬───────────────────┘
                                             │ H2 Persistence
                          ┌──────────────────▼───────────────────┐
                          │   H2 Embedded Database / Ledger DB   │
                          └──────────────────────────────────────┘
```

---

## 💻 Tech Stack

* **Backend**: Java 21, Spring Boot 3.3.4, Spring Web, Spring Data JPA
* **Database**: H2 In-Memory Database (with H2 Web Console)
* **Frontend**: HTML5, Vanilla CSS3 (Custom Glassmorphism Design System), JavaScript (ES6+), Chart.js (CDN)
* **Build Tool**: Apache Maven

---

## 📁 Project Structure

```
ElectronicVotingSystem/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/evoting/app/
    │   │   ├── ElectronicVotingSystemApplication.java
    │   │   ├── controller/
    │   │   │   ├── AdminController.java
    │   │   │   ├── AuditController.java
    │   │   │   ├── AuthController.java
    │   │   │   ├── ElectionController.java
    │   │   │   └── VotingController.java
    │   │   ├── model/
    │   │   │   ├── AuditLog.java
    │   │   │   ├── Candidate.java
    │   │   │   ├── Election.java
    │   │   │   ├── Position.java
    │   │   │   ├── User.java
    │   │   │   └── VoteLedger.java
    │   │   ├── repository/
    │   │   │   ├── AuditLogRepository.java
    │   │   │   ├── CandidateRepository.java
    │   │   │   ├── ElectionRepository.java
    │   │   │   ├── PositionRepository.java
    │   │   │   ├── UserRepository.java
    │   │   │   └── VoteLedgerRepository.java
    │   │   └── service/
    │   │       ├── AuditService.java
    │   │       ├── AuthService.java
    │   │       ├── CryptoService.java
    │   │       ├── DataInitializer.java
    │   │       ├── ElectionService.java
    │   │       └── VotingService.java
    │   └── resources/
    │       ├── application.properties
    │       └── static/
    │           ├── css/
    │           │   └── styles.css
    │           ├── js/
    │           │   └── app.js
    │           └── index.html
```

---

## 🚀 Getting Started

### Prerequisites
* **Java Development Kit (JDK 21 or higher)**
* **Apache Maven 3.8+**

### 1. Clone & Build the Application
```bash
git clone https://github.com/NKumar-B/Electronic_voting_system.git
cd Electronic_voting_system

# Compile project using Maven
mvn clean compile
```

### 2. Run the Application
```bash
mvn spring-boot:run
```

Once started, access the Web Portal in your browser:
* **Web Portal**: [http://localhost:8080](http://localhost:8080)
* **H2 Database Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  * **JDBC URL**: `jdbc:h2:mem:evotingdb`
  * **Username**: `sa`
  * **Password**: *(leave blank)*

---

## 🔑 Pre-Loaded Seed Credentials

The application auto-seeds initial demo users, active elections, candidates, and initial cryptographic vote blocks on startup:

| Role | Voter ID | Email | Password | Access Rights |
| :--- | :--- | :--- | :--- | :--- |
| **Voter 1** | `VOTER-1001` | `alice@example.com` | `voter123` | Digital Voting Booth, Receipt Verification |
| **Voter 2** | `VOTER-1002` | `bob@example.com` | `voter123` | Digital Voting Booth, Receipt Verification |
| **Admin** | `VOTER-ADMIN-001` | `admin@securevote.gov` | `admin123` | Create Elections, Manage Candidates, Toggle Status |
| **Auditor** | `VOTER-AUDIT-001` | `auditor@securevote.gov` | `audit123` | Run SHA-256 Chain Verification, Inspect Audit Logs |

---

## 📡 REST API Documentation

### Authentication (`/api/auth`)
* `POST /api/auth/login`: Authenticate user session.
* `POST /api/auth/register`: Register new voter.
* `GET /api/auth/users`: List registered users.

### Elections (`/api/elections`)
* `GET /api/elections`: Fetch all system elections.
* `GET /api/elections/{id}/ballot`: Fetch positions, candidates, and total votes cast for an election.
* `POST /api/elections`: Create new election *(Admin)*.
* `PUT /api/elections/{id}/status`: Toggle election status (`ACTIVE`, `CLOSED`, `PAUSED`) *(Admin)*.
* `POST /api/elections/positions/{positionId}/candidates`: Add candidate to position *(Admin)*.

### Voting (`/api/vote`)
* `POST /api/vote/cast`: Atomically record vote in SHA-256 chain ledger.
* `GET /api/vote/check`: Check if voter has cast ballot for position.
* `GET /api/vote/verify-receipt/{receiptCode}`: Verify receipt code in cryptographic chain.

### Audit & Security (`/api/audit`)
* `GET /api/audit/verify-chain`: Trigger full SHA-256 block chain verification.
* `GET /api/audit/logs`: Retrieve system activity audit logs.

---

## 🛡️ Security & Integrity Design

```
Block N-1: [Hash: 6f8c4e...] <───┐
                                  │ Parent Link
Block N:   [Prev: 6f8c4e... | Election: 1 | VoterHash: a3b... | Candidate: 2 | Timestamp]
           └──► Block Hash (SHA-256): a01f37...
```

1. **Chain Integrity**: Changing any field in a past block alters its `voteHash`, causing all subsequent blocks to fail verification.
2. **Double-Voting Prevention**: The `existsByElectionIdAndPositionIdAndVoterHash` check prevents duplicate votes without identifying the voter's candidate selection.
3. **Concurrency Safety**: `VotingService.castVote()` uses synchronized transactions to prevent race conditions during vote registration.

---

## 📜 License

This project is open-source and available under the [MIT License](LICENSE).
