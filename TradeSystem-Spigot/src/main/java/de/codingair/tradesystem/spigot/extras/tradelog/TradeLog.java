package de.codingair.tradesystem.spigot.extras.tradelog;

import java.time.LocalDateTime;

public class TradeLog {
    private final String player1Name;
    private final String player2Name;
    private final String message;
    private final LocalDateTime timestamp;

    public TradeLog(String player1Name, String player2Name, String message, LocalDateTime timestamp) {
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getPlayer1Name() {
        return player1Name;
    }

    public String getPlayer2Name() {
        return player2Name;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
