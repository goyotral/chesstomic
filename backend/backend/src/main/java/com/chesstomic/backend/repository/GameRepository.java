package com.chesstomic.backend.repository;

import com.chesstomic.backend.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {
    Optional<Game> findByGameId(String gameId);
}