package com.chesstomic.backend.config;

import com.chesstomic.backend.repository.ChatMessageRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class ChallengeSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatMessageRepository messageRepository;

    @MessageMapping("/reject-challenge")
    public void rejectChallenge(Map<String, String> payload) {

        String from = payload.get("from");
        String to = payload.get("to");


        messageRepository.deleteChallengeInvite(to, from);


        messagingTemplate.convertAndSendToUser(
                to,
                "/queue/notifications",
                Map.of(
                        "type", "CHALLENGE_REJECTED",
                        "from", from
                )
        );
    }

}