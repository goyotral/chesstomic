package com.chesstomic.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlayerDistanceDTO {
    private String username;
    private Integer elo;
    private Double distanceInMeters;
}