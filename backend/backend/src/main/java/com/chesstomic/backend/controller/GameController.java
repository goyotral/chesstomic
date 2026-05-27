package com.chesstomic.backend.controller;

import com.chesstomic.backend.config.EloCalculator;
import com.chesstomic.backend.model.ChatMessage;
import com.chesstomic.backend.model.ChessMove;
import com.chesstomic.backend.model.Friendship;
import com.chesstomic.backend.model.Game;
import com.chesstomic.backend.model.Player;
import com.chesstomic.backend.repository.ChatMessageRepository;
import com.chesstomic.backend.repository.FriendshipRepository;
import com.chesstomic.backend.repository.GameRepository;
import com.chesstomic.backend.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class GameController {

    private final SimpMessagingTemplate messagingTemplate;
    private final FriendshipRepository friendshipRepository;
    private final PlayerRepository playerRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final GameRepository gameRepository;
    private final EloCalculator eloCalculator;

    @Autowired
    public GameController(SimpMessagingTemplate messagingTemplate, 
                          FriendshipRepository friendshipRepository,
                          PlayerRepository playerRepository,
                          ChatMessageRepository chatMessageRepository,
                          GameRepository gameRepository,
                          EloCalculator eloCalculator) {
        this.messagingTemplate = messagingTemplate;
        this.friendshipRepository = friendshipRepository;
        this.playerRepository = playerRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.gameRepository = gameRepository;
        this.eloCalculator = new EloCalculator();
    }


@MessageMapping("/move/{gameId}")
@SendTo("/topic/game/{gameId}") 
public ChessMove processMove(@DestinationVariable String gameId, ChessMove move) {
    gameRepository.findByGameId(gameId).ifPresent(game -> {
        if (move.getCurrentFen() != null) {
            game.setCurrentFen(move.getCurrentFen());
            gameRepository.save(game);
        }
    });

    return move; 
}

    @MessageMapping("/accept-challenge")
    public void acceptChallenge(@Payload Map<String, String> payload) {
        String challengerName = payload.get("challenger");
        String opponentName = payload.get("opponent");
        String gameId = UUID.randomUUID().toString();

        Player white = playerRepository.findByUsername(challengerName).orElse(null);
        Player black = playerRepository.findByUsername(opponentName).orElse(null);

        if (white != null && black != null) {
            Game newGame = new Game();
            newGame.setGameId(gameId);
            newGame.setWhitePlayer(white);
            newGame.setBlackPlayer(black);
            newGame.setCurrentFen("start");
            newGame.setStatus("IN_PROGRESS");
            gameRepository.save(newGame);
        }

        Map<String, String> gameData = Map.of(
            "type", "START_GAME",
            "gameId", gameId,
            "white", challengerName,
            "black", opponentName
        );

        messagingTemplate.convertAndSendToUser(challengerName, "/queue/messages", gameData);
        messagingTemplate.convertAndSendToUser(opponentName, "/queue/messages", gameData);
    }

    @GetMapping("/game/{gameId}")
    public String showBoard(@PathVariable String gameId, @RequestParam String color, Model model, Principal principal) {
        model.addAttribute("gameId", gameId);
        model.addAttribute("color", color);

        gameRepository.findByGameId(gameId).ifPresent(game -> {
            boolean isWhite = game.getWhitePlayer().getUsername().equals(principal.getName());
            Player me = isWhite ? game.getWhitePlayer() : game.getBlackPlayer();
            Player opponent = isWhite ? game.getBlackPlayer() : game.getWhitePlayer();
            
            model.addAttribute("opponentName", opponent.getUsername());
            model.addAttribute("opponentElo", opponent.getElo());
            model.addAttribute("playerElo", me.getElo());
        });

        return "board";
    }

    @GetMapping("/api/game/{gameId}/players")
    @ResponseBody
    public Map<String, Object> getGamePlayers(@PathVariable String gameId, Principal principal) {
        Game game = gameRepository.findByGameId(gameId) 
                .orElseThrow(() -> new RuntimeException("Partida no encontrada"));

        Player white = game.getWhitePlayer();
        Player black = game.getBlackPlayer();
        boolean isWhite = white.getUsername().equals(principal.getName());

        Player me = isWhite ? white : black;
        Player opponent = isWhite ? black : white;

        return Map.of(
            "me", Map.of("name", me.getUsername(), "elo", me.getElo()),
            "opponent", Map.of("name", opponent.getUsername(), "elo", opponent.getElo())
        );
    }


    @GetMapping("/api/friends")
    @ResponseBody
    public List<Map<String, String>> getFriends(Principal principal) {
        Player currentUser = playerRepository.findByUsername(principal.getName()).orElse(null);
        List<Friendship> friendships = friendshipRepository.findByUserOneOrUserTwo(currentUser, currentUser);
        
        return friendships.stream()
            .filter(Friendship::isAccepted)
            .map(f -> {
                Player friend = f.getUserOne().equals(currentUser) ? f.getUserTwo() : f.getUserOne();
                return Map.of(
                    "username", friend.getUsername(),
                    "elo", String.valueOf(friend.getElo()),
                    "id", String.valueOf(f.getId())
                );
            }).toList();
    }

    @MessageMapping("/social-action")
    public void handleSocialAction(Map<String, String> action) {
        String targetUsername = action.get("to");
        messagingTemplate.convertAndSendToUser(targetUsername, "/queue/notifications", action);
    }

    @MessageMapping("/accept-friend")
    public void acceptFriend(Map<String, String> action) {
        Player sender = playerRepository.findByUsername(action.get("from")).orElse(null);
        Player receiver = playerRepository.findByUsername(action.get("to")).orElse(null);

        if (sender != null && receiver != null) {
            Friendship friendship = new Friendship();
            friendship.setUserOne(sender);   
            friendship.setUserTwo(receiver); 
            friendship.setAccepted(true);
            friendshipRepository.save(friendship);

            messagingTemplate.convertAndSendToUser(sender.getUsername(), "/queue/notifications", Map.of(
                "type", "FRIEND_ACCEPTED",
                "from", receiver.getUsername()
            ));
        }
    }

    @DeleteMapping("/api/friends/{id}")
    @ResponseBody
    public void deleteFriend(@PathVariable Long id) {
        friendshipRepository.deleteById(id);
    }



    @MessageMapping("/private-message")
    public void sendPrivate(@Payload Map<String, String> payload) {
        Player from = playerRepository.findByUsername(payload.get("from")).orElse(null);
        Player to = playerRepository.findByUsername(payload.get("to")).orElse(null);

        if (from != null && to != null) {
            ChatMessage msg = new ChatMessage();
            msg.setSender(from);
            msg.setReceiver(to);
            msg.setContent(payload.get("text"));
            msg.setTimestamp(LocalDateTime.now());
            chatMessageRepository.save(msg);

            messagingTemplate.convertAndSendToUser(to.getUsername(), "/queue/messages", Map.of(
                "sender", from.getUsername(),
                "content", msg.getContent()
            ));
        }
    }

    @GetMapping("/api/chat/history/{withUser}")
    @ResponseBody
    public List<Map<String, Object>> getHistory(@PathVariable String withUser, Principal principal) {
        Player me = playerRepository.findByUsername(principal.getName()).orElseThrow();
        Player other = playerRepository.findByUsername(withUser).orElseThrow();

        return chatMessageRepository.findBySenderAndReceiverOrReceiverAndSenderOrderByTimestampAsc(me, other, me, other)
            .stream()
            .map(m -> { 
                Map<String, Object> data = new HashMap<>();
                data.put("sender", m.getSender().getUsername());
                data.put("content", m.getContent());
                data.put("timestamp", m.getTimestamp());
                return data;
            }).collect(Collectors.toList());
    }
    private String winner;

@MessageMapping("/game-over")
public void gameOver(Map<String, String> payload) {

    String gameId = payload.get("gameId");
    String winnerUsername = payload.get("winner");

    gameRepository.findByGameId(gameId).ifPresent(game -> {

        game.setStatus("FINISHED");
        game.setWinner(winnerUsername);
        game.setEndTime(LocalDateTime.now());

        Player white = game.getWhitePlayer();
        Player black = game.getBlackPlayer();

double resultadoWhite;
double resultadoBlack;

if (winnerUsername.equals("DRAW")) {

    resultadoWhite = 0.5;
    resultadoBlack = 0.5;

} else {

    boolean ganaBlancas = white.getUsername().equals(winnerUsername);

    resultadoWhite = ganaBlancas ? 1.0 : 0.0;
    resultadoBlack = ganaBlancas ? 0.0 : 1.0;
}

int nuevoEloWhite = eloCalculator.calcularElo(
        white.getElo(),
        black.getElo(),
        resultadoWhite
);

int nuevoEloBlack = eloCalculator.calcularElo(
        black.getElo(),
        white.getElo(),
        resultadoBlack
);

white.setElo(nuevoEloWhite);
black.setElo(nuevoEloBlack);

playerRepository.save(white);
playerRepository.save(black);

        gameRepository.save(game);

        messagingTemplate.convertAndSend(
                "/topic/game/" + gameId,
Map.of(
    "type", "GAME_OVER",
    "winner", winnerUsername,
    "white", white.getUsername(),
    "black", black.getUsername()
)
        );
    });
}
@MessageMapping("/game-over/{gameId}")
public void handleGameOver(@DestinationVariable String gameId, Map<String, String> payload) {

    gameRepository.findByGameId(gameId).ifPresent(game -> {

        chatMessageRepository.deleteAllChallengeMessages(
                game.getWhitePlayer().getUsername()
        );

        chatMessageRepository.deleteAllChallengeMessages(
                game.getBlackPlayer().getUsername()
        );

        String winnerUsername = payload.get("winner");

        game.setStatus("FINISHED");
        game.setWinner(winnerUsername);

        Player white = game.getWhitePlayer();
        Player black = game.getBlackPlayer();

        double resultadoWhite;
        double resultadoBlack;

        if (winnerUsername.equals("DRAW")) {

            resultadoWhite = 0.5;
            resultadoBlack = 0.5;

        } else {

            boolean ganaBlancas =
                    white.getUsername().equals(winnerUsername);

            resultadoWhite = ganaBlancas ? 1.0 : 0.0;
            resultadoBlack = ganaBlancas ? 0.0 : 1.0;
        }

        int nuevoEloWhite = eloCalculator.calcularElo(
                white.getElo(),
                black.getElo(),
                resultadoWhite
        );

        int nuevoEloBlack = eloCalculator.calcularElo(
                black.getElo(),
                white.getElo(),
                resultadoBlack
        );

        white.setElo(nuevoEloWhite);
        black.setElo(nuevoEloBlack);

        playerRepository.save(white);
        playerRepository.save(black);

        gameRepository.save(game);

        Map<String, String> response = new HashMap<>();
        response.put("type", "GAME_OVER");
        response.put("winner", game.getWinner());

        messagingTemplate.convertAndSend(
                "/topic/game/" + gameId,
                response
        );
    });
}

}