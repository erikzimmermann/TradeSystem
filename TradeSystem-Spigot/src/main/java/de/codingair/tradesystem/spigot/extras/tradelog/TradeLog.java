package de.codingair.tradesystem.spigot.extras.tradelog;

import de.codingair.codingapi.files.ConfigFile;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.events.TradeLogReceiveItemEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.MissingFormatArgumentException;
import java.util.Set;

public class TradeLog {
    private static FileConfiguration config;

    public static final Message STARTED = new Message("started", "Trade started", "§e");
    public static final Message CANCELLED_WITH_REASON = new Message("cancelled", "Trade cancelled: %s", "§c");
    public static final Message CANCELLED = new Message("cancelled", "Trade cancelled", "§c");
    public static final Message FINISHED = new Message("finished", "Trade finished", "§a");

    public static final Message OFFERED_AMOUNT = new Message("offered", "%s offered %s: %s", "§8");
    public static final Message RECEIVED = new Message("received", "%s received %s", "§8");
    public static final Message RECEIVED_AMOUNT = new Message("received", "%s received %s: %s", "§8");

    /**
     * Only used to identify colors for the trade log display. Adding external messages is optional but not necessarily required.
     */
    public static final Set<Message> MESSAGES = new HashSet<>();

    static {
        MESSAGES.add(STARTED);
        MESSAGES.add(CANCELLED_WITH_REASON);
        MESSAGES.add(CANCELLED);
        MESSAGES.add(FINISHED);
        MESSAGES.add(RECEIVED);
        MESSAGES.add(RECEIVED_AMOUNT);
    }

    /**
     * Calls an event for getting the message for the trade log.
     *
     * @param receiver  The {@link Player} who receives the item.
     * @param initiator Whether the receiving player initiated the trade.
     * @param trader    The name of the player who trades the item.
     * @param getting   The item being transferred.
     */
    public static void logItemReceive(@NotNull Player receiver, boolean initiator, @NotNull String trader, @NotNull ItemStack getting) {
        Player tradingPlayer = Bukkit.getPlayerExact(trader);
        TradeLogReceiveItemEvent e = tradingPlayer == null ? new TradeLogReceiveItemEvent(receiver, trader, getting) : new TradeLogReceiveItemEvent(receiver, tradingPlayer, getting);
        Bukkit.getPluginManager().callEvent(e);

        String message = e.getMessage();
        if (message == null) message = getting.getAmount() + "x " + getting.getType();

        TradeLogService.log(
                initiator ? receiver.getName() : trader,
                initiator ? trader : receiver.getName(),
                TradeLog.RECEIVED.get(receiver.getName(), message)
        );
    }

    public static boolean isEnabled() {
        return config().getBoolean("TradeSystem.TradeLog.Enabled", false);
    }

    private static ConfigurationSection config() {
        if (config == null) {
            ConfigFile file = TradeSystem.getInstance().getFileManager().getFile("Config");
            config = file.getConfig();
        }

        return config;
    }

    public static @NotNull String getColorByString(@NotNull String s) {
        s = s.toLowerCase();

        for (Message m : MESSAGES) {
            if (s.contains(m.searchTag)) return m.color;
        }

        return "§8";
    }

    public static class Message {
        private final String searchTag;
        private final String message;
        private final String color;

        /**
         * @param searchTag Something of the message that makes it unique to apply String#contains on a message to identify it.
         * @param message   The message that should be logged. Can contain placeholders like %s.
         * @param color     The color that should be used for the log display in /tradelog.
         */
        public Message(@NotNull String searchTag, @NotNull String message, @NotNull String color) {
            this.searchTag = searchTag.toLowerCase();
            this.message = message;
            this.color = color;
        }

        /**
         * @param replacements The replacements for the placeholders in the message.
         * @return The message that should be logged.
         */
        @NotNull
        public String get(@NotNull Object @NotNull ... replacements) {
            try {
                return String.format(this.message, replacements);
            } catch (MissingFormatArgumentException e) {
                throw new IllegalStateException("The number of placeholders in the message '" + this.message + "' does not match the number of replacements which are: " + Arrays.toString(replacements), e);
            }
        }
    }

    public static class Entry {
        private final String player1Name;
        private final String player2Name;
        private final String message;
        private final LocalDateTime timestamp;

        public Entry(String player1Name, String player2Name, String message, LocalDateTime timestamp) {
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
}
