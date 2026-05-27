package com.chesstomic.backend.repository;
import com.chesstomic.backend.model.ChatMessage;
import com.chesstomic.backend.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySenderAndReceiverOrReceiverAndSenderOrderByTimestampAsc(
        Player s1, Player r1, Player s2, Player r2);
@Modifying
@Transactional
@Query("""
    DELETE FROM ChatMessage m
    WHERE m.sender.username = :challenger
    AND m.receiver.username = :receiver
    AND m.content = '__CHALLENGE_INVITE__'
""")
void deleteChallengeInvite(@Param("challenger") String challenger,
                           @Param("receiver") String receiver);
                           @Modifying
@Transactional
@Query("""
    DELETE FROM ChatMessage m
    WHERE m.content = '__CHALLENGE_INVITE__'
    AND (
        m.sender.username = :username
        OR m.receiver.username = :username
    )
""")
void deleteAllChallengeMessages(@Param("username") String username);



}