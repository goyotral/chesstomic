package com.chesstomic.backend.repository;

import com.chesstomic.backend.model.Friendship;
import com.chesstomic.backend.model.Player; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    Friendship findByUserOneAndUserTwo(Player u1, Player u2);
    List<Friendship> findByUserOneOrUserTwo(Player p1, Player p2);
}