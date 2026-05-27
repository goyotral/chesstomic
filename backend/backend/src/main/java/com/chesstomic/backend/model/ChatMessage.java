package com.chesstomic.backend.model;


import org.locationtech.jts.geom.Point;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data

public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private Player sender; 

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private Player receiver;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime timestamp;
}
