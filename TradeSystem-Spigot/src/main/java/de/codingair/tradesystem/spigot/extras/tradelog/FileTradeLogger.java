package de.codingair.tradesystem.spigot.extras.tradelog;

import de.codingair.tradesystem.spigot.TradeSystem;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class FileTradeLogger {
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter LOG_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final int BUFFER_SIZE = 100;
    private static final long FLUSH_INTERVAL_MS = 1000; // Flush every second
    
    private static FileTradeLogger instance;
    private final File logsFolder;
    private final boolean enabled;
    private final LogFilters filters;
    private final BlockingQueue<LogEntry> logQueue;
    private final Thread writerThread;
    private volatile boolean running;

    private FileTradeLogger() {
        this.logsFolder = new File(TradeSystem.getInstance().getDataFolder(), "logs");
        FileConfiguration config = TradeSystem.getInstance().getFileManager()
                .getFile("Config").getConfig();
        
        this.enabled = config.getBoolean("TradeSystem.TradeLog.log-trade-files", false);
        this.filters = new LogFilters(config);
        this.logQueue = new LinkedBlockingQueue<>(BUFFER_SIZE);
        this.running = enabled;

        if (enabled) {
            if (!logsFolder.exists()) {
                if (!logsFolder.mkdirs()) {
                    TradeSystem.getInstance().getLogger().warning("Failed to create logs folder at: " + logsFolder.getAbsolutePath());
                }
            }
            
            // Start background writer thread
            this.writerThread = new Thread(this::processLogQueue, "TradeLog-Writer");
            this.writerThread.setDaemon(true);
            this.writerThread.start();
        } else {
            this.writerThread = null;
        }
    }

    public static FileTradeLogger getInstance() {
        if (instance == null) {
            instance = new FileTradeLogger();
        }
        return instance;
    }

    /**
     * Logs a trade message to a file asynchronously using a queue.
     *
     * @param player1 The first player involved in the trade
     * @param player2 The second player involved in the trade
     * @param message The message to log
     */
    public void log(@NotNull String player1, @NotNull String player2, @Nullable String message) {
        if (!enabled || message == null) return;
        
        // Check if this type of message should be logged
        if (!filters.shouldLog(message)) return;

        // Add to queue instead of writing immediately
        LogEntry entry = new LogEntry(player1, player2, message, LocalDateTime.now());
        
        if (!logQueue.offer(entry)) {
            // Queue is full, force flush
            TradeSystem.getInstance().getLogger().warning("TradeLog queue is full, some logs may be delayed");
        }
    }

    /**
     * Processes the log queue continuously in a background thread.
     */
    private void processLogQueue() {
        StringBuilder buffer = new StringBuilder();
        String currentFileName = null;
        long lastFlush = System.currentTimeMillis();

        while (running) {
            try {
                // Wait for logs with timeout to allow periodic flushing
                LogEntry entry = logQueue.poll(FLUSH_INTERVAL_MS, TimeUnit.MILLISECONDS);
                
                if (entry != null) {
                    String fileName = entry.timestamp.format(FILE_DATE_FORMAT) + ".txt";
                    
                    // If date changed, flush current buffer and switch file
                    if (currentFileName != null && !currentFileName.equals(fileName)) {
                        flushBuffer(currentFileName, buffer);
                        buffer.setLength(0);
                    }
                    
                    currentFileName = fileName;
                    buffer.append(formatLogEntry(entry)).append(System.lineSeparator());
                }
                
                // Flush buffer if interval elapsed or buffer is large
                long now = System.currentTimeMillis();
                if (buffer.length() > 0 && (now - lastFlush >= FLUSH_INTERVAL_MS || buffer.length() > 8192)) {
                    flushBuffer(currentFileName, buffer);
                    buffer.setLength(0);
                    lastFlush = now;
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        // Final flush on shutdown
        if (buffer.length() > 0 && currentFileName != null) {
            flushBuffer(currentFileName, buffer);
        }
    }

    /**
     * Flushes the buffer to the specified file.
     */
    private void flushBuffer(String fileName, StringBuilder buffer) {
        if (fileName == null || buffer.length() == 0) return;
        
        File logFile = new File(logsFolder, fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            writer.write(buffer.toString());
            writer.flush();
        } catch (IOException e) {
            TradeSystem.getInstance().getLogger().severe("Failed to write to trade log file: " + e.getMessage());
        }
    }

    /**
     * Shuts down the logger gracefully.
     */
    public void shutdown() {
        if (!enabled) return;
        
        running = false;
        if (writerThread != null) {
            try {
                writerThread.join(5000); // Wait up to 5 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Formats a log entry with timestamp and player information.
     *
     * @param entry The log entry to format
     * @return Formatted log entry
     */
    @NotNull
    private String formatLogEntry(@NotNull LogEntry entry) {
        String timestamp = entry.timestamp.format(LOG_TIMESTAMP_FORMAT);
        return String.format("[%s] [%s <-> %s] %s", timestamp, entry.player1, entry.player2, entry.message);
    }

    /**
     * Checks if file logging is enabled.
     *
     * @return true if file logging is enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Gets the logs folder.
     *
     * @return The logs folder
     */
    @NotNull
    public File getLogsFolder() {
        return logsFolder;
    }

    /**
     * Represents a log entry to be written.
     */
    private static class LogEntry {
        private final String player1;
        private final String player2;
        private final String message;
        private final LocalDateTime timestamp;

        public LogEntry(@NotNull String player1, @NotNull String player2, @NotNull String message, @NotNull LocalDateTime timestamp) {
            this.player1 = player1;
            this.player2 = player2;
            this.message = message;
            this.timestamp = timestamp;
        }
    }

    /**
     * Inner class to manage log filtering based on configuration.
     */
    private static class LogFilters {
        private final boolean logTradeStarted;
        private final boolean logTradeFinished;
        private final boolean logTradeCancelled;
        private final boolean logItemsOffered;
        private final boolean logItemsReceived;

        public LogFilters(@NotNull FileConfiguration config) {
            String basePath = "TradeSystem.TradeLog.log-filters.";
            this.logTradeStarted = config.getBoolean(basePath + "log-trade-started", false);
            this.logTradeFinished = config.getBoolean(basePath + "log-trade-finished", false);
            this.logTradeCancelled = config.getBoolean(basePath + "log-trade-cancelled", false);
            this.logItemsOffered = config.getBoolean(basePath + "log-items-offered", false);
            this.logItemsReceived = config.getBoolean(basePath + "log-items-received", false);
        }

        /**
         * Determines if a message should be logged based on its content.
         *
         * @param message The message to check
         * @return true if the message should be logged, false otherwise
         */
        public boolean shouldLog(@NotNull String message) {
            String lowerMessage = message.toLowerCase();

            // Check for trade started
            if (lowerMessage.contains("started")) {
                return logTradeStarted;
            }

            // Check for trade finished
            if (lowerMessage.contains("finished")) {
                return logTradeFinished;
            }

            // Check for trade cancelled
            if (lowerMessage.contains("cancelled")) {
                return logTradeCancelled;
            }

            // Check for items offered
            if (lowerMessage.contains("offered")) {
                return logItemsOffered;
            }

            // Check for items received
            if (lowerMessage.contains("received")) {
                return logItemsReceived;
            }

            // If no specific filter matches, log it by default
            return true;
        }
    }
}
