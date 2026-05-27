package com.chesstomic.backend.model;

import org.locationtech.jts.geom.Point;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data 
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private Integer elo;

    private String password;

    private String role;


    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point location;


    public Player(Long id, String username, Integer elo, String password, String role, Point location) {
        this.id = id;
        this.username = username;
        this.elo = elo;
        this.password = password;
        this.role = role;
        this.location = location;
    }


    public Player() {
    }
    
}