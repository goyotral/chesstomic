package com.chesstomic.backend.repository;

import com.chesstomic.backend.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByUsername(String username);


@Query(value = "SELECT p.username, p.elo, ST_DistanceSphere(p.location, ST_MakePoint(:lon, :lat)) as distance " +
               "FROM player p " +
               "WHERE ST_DistanceSphere(p.location, ST_MakePoint(:lon, :lat)) <= :radius " +
               "ORDER BY distance ASC", nativeQuery = true)
List<Object[]> findNearbyPlayersWithDistance(@Param("lat") double lat, @Param("lon") double lon, @Param("radius") double radius);

@Query(value = "SELECT * FROM player p WHERE ST_DistanceSphere(p.location, ST_MakePoint(:lon, :lat)) <= :radius", nativeQuery = true)
List<Player> findNearbyPlayers(@Param("lat") double lat, @Param("lon") double lon, @Param("radius") double radius);

@Query(value = "SELECT p.username, p.elo, ST_DistanceSphere(p.location, ST_MakePoint(:lon, :lat)) as distance " +
               "FROM player p " +
               "WHERE ST_DistanceSphere(p.location, ST_MakePoint(:lon, :lat)) <= :radius " +
               "AND p.elo >= :minElo " + 
               "ORDER BY distance ASC", nativeQuery = true)
List<Object[]> findNearbyPlayersWithFilters(@Param("lat") double lat, @Param("lon") double lon, @Param("radius") double radius, @Param("minElo") int minElo);
}