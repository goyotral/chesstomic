package com.chesstomic.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "friendships")
public class Friendship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @ManyToOne
    @JoinColumn(name = "user_one_id") 
    private Player userOne;

    @ManyToOne
    @JoinColumn(name = "user_two_id")
    private Player userTwo;

    private boolean accepted = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Player getUserOne() { return userOne; }
    public void setUserOne(Player userOne) { this.userOne = userOne; }
    public Player getUserTwo() { return userTwo; }
    public void setUserTwo(Player userTwo) { this.userTwo = userTwo; }
    public boolean isAccepted() { return accepted; }
    public void setAccepted(boolean accepted) { this.accepted = accepted; }
}