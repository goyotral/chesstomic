package com.chesstomic.backend.controller;

import com.chesstomic.backend.dto.PlayerDTO;
import com.chesstomic.backend.dto.PlayerDistanceDTO;
import com.chesstomic.backend.model.Player;
import com.chesstomic.backend.repository.PlayerRepository;
import com.chesstomic.backend.service.PlayerService;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller; 
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller 
@RequestMapping("/api/players")
public class PlayerController {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private GeometryFactory geometryFactory;

    @Autowired
    private PlayerService playerService;


    
    @GetMapping
    @ResponseBody
    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    @GetMapping("/nearby")
    @ResponseBody
    public List<PlayerDistanceDTO> getNearby(
            @RequestParam double lat, 
            @RequestParam double lon, 
            @RequestParam double radius,
            @RequestParam(defaultValue = "0") int minElo) {
        return playerService.getNearbyPlayers(lat, lon, radius, minElo);
    }


    @PostMapping("/register")
    public String registerPlayer(@ModelAttribute PlayerDTO playerDTO) {
        try {
            Player player = new Player();
            player.setUsername(playerDTO.getUsername());

            if (playerDTO.getPassword() != null && !playerDTO.getPassword().isEmpty()) {
                player.setPassword(passwordEncoder.encode(playerDTO.getPassword()));
            } else {
                player.setPassword(passwordEncoder.encode("123456")); 
            }
            
            player.setElo(playerDTO.getElo());

            if (playerDTO.getLatitude() != null && playerDTO.getLongitude() != null) {
                player.setLocation(geometryFactory.createPoint(
                    new Coordinate(playerDTO.getLongitude(), playerDTO.getLatitude())
                ));
            }

            playerRepository.save(player);
            System.out.println("✅ Usuario nuevo guardado: " + playerDTO.getUsername());
            
            return "redirect:/login?success";
        } catch (Exception e) {
            System.out.println("❌ Error en registro: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/register?error";
        }
    }


}