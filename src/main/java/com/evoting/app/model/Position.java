package com.evoting.app.model;

import jakarta.persistence.*;

@Entity
@Table(name = "positions")
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long electionId;

    @Column(nullable = false)
    private String title; // e.g., President, Vice President, Secretary

    private int maxChoices = 1;

    public Position() {}

    public Position(Long electionId, String title, int maxChoices) {
        this.electionId = electionId;
        this.title = title;
        this.maxChoices = maxChoices;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getElectionId() { return electionId; }
    public void setElectionId(Long electionId) { this.electionId = electionId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getMaxChoices() { return maxChoices; }
    public void setMaxChoices(int maxChoices) { this.maxChoices = maxChoices; }
}
