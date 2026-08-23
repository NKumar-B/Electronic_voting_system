package com.evoting.app.model;

import jakarta.persistence.*;

@Entity
@Table(name = "candidates")
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long positionId;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String partyName;

    @Column(length = 1000)
    private String manifesto;

    private String imageUrl;

    private int voteCount = 0;

    public Candidate() {}

    public Candidate(Long positionId, String fullName, String partyName, String manifesto, String imageUrl) {
        this.positionId = positionId;
        this.fullName = fullName;
        this.partyName = partyName;
        this.manifesto = manifesto;
        this.imageUrl = imageUrl;
        this.voteCount = 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPositionId() { return positionId; }
    public void setPositionId(Long positionId) { this.positionId = positionId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPartyName() { return partyName; }
    public void setPartyName(String partyName) { this.partyName = partyName; }

    public String getManifesto() { return manifesto; }
    public void setManifesto(String manifesto) { this.manifesto = manifesto; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getVoteCount() { return voteCount; }
    public void setVoteCount(int voteCount) { this.voteCount = voteCount; }
}
