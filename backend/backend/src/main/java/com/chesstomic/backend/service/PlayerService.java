package com.chesstomic.backend.service;

import com.chesstomic.backend.dto.PlayerDistanceDTO;
import com.chesstomic.backend.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlayerService {

    @Autowired
    private PlayerRepository playerRepository;

    public List<PlayerDistanceDTO> getNearbyPlayers(double lat, double lon, double radius) {
        List<Object[]> results = playerRepository.findNearbyPlayersWithDistance(lat, lon, radius);
        
        return results.stream().map(result -> new PlayerDistanceDTO(
                (String) result[0],
                (Integer) result[1],
                (Double) result[2]
        )).collect(Collectors.toList());
    }
    public List<PlayerDistanceDTO> getNearbyPlayers(double lat, double lon, double radius, int minElo) {
    List<Object[]> results = playerRepository.findNearbyPlayersWithFilters(lat, lon, radius, minElo);
        return results.stream().map(result -> new PlayerDistanceDTO(
                (String) result[0],
                (Integer) result[1],
                (Double) result[2]
        )).collect(Collectors.toList());}
}