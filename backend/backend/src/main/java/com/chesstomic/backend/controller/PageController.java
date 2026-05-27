package com.chesstomic.backend.controller;

import com.chesstomic.backend.dto.PlayerDTO; 
import com.chesstomic.backend.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List; 
import java.util.stream.Collectors; 

@Controller
public class PageController {

    @Autowired
    private PlayerRepository playerRepository;

    @GetMapping("/")
    public String index(Model model) {
        List<PlayerDTO> playerDTOs = playerRepository.findAll().stream().map(p -> {
            PlayerDTO dto = new PlayerDTO();
            dto.setUsername(p.getUsername());
            dto.setElo(p.getElo());
            dto.setLatitude(p.getLocation().getY());
            dto.setLongitude(p.getLocation().getX());
            return dto;
        }).collect(Collectors.toList());

        model.addAttribute("players", playerDTOs);
        return "index";
    }

    @GetMapping("/play")
    public String play() {
        return "play"; 
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}