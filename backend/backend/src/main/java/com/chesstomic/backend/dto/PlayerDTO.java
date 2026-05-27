package com.chesstomic.backend.dto;


public class PlayerDTO {
    private String username;
    private String password; 
    private int elo;
    private Double latitude;  
    private Double longitude; 

    public PlayerDTO() {}


    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; } 
    public void setPassword(String password) { this.password = password; } 

    public int getElo() { return elo; }
    public void setElo(int elo) { this.elo = elo; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}