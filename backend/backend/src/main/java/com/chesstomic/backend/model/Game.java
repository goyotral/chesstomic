package com.chesstomic.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "games")
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String gameId;

    @ManyToOne
    private Player whitePlayer;

    @ManyToOne
    private Player blackPlayer;

    @Column(columnDefinition = "TEXT")
    private String currentFen; 

    private String status; 
    private LocalDateTime endTime;
    private String winner; 

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getGameId() {
        return gameId;
    }
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }
    public Player getWhitePlayer() {
        return whitePlayer;
    }
    public void setWhitePlayer(Player whitePlayer) {
        this.whitePlayer = whitePlayer;
    }
    public Player getBlackPlayer() {
        return blackPlayer;
    }
    public void setBlackPlayer(Player blackPlayer) {
        this.blackPlayer = blackPlayer;
    }
    public String getCurrentFen() {
        return currentFen;
    }
    public void setCurrentFen(String currentFen) {
        this.currentFen = currentFen;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public LocalDateTime getEndTime() {
        return endTime;
    }
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    public String getWinner() {
        return winner;
    }
    public void setWinner(String winner) {
        this.winner = winner;
    }

    
}