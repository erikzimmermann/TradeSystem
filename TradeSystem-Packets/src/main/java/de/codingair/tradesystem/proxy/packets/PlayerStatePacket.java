package de.codingair.tradesystem.proxy.packets;

import de.codingair.packetmanagement.packets.Packet;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public class PlayerStatePacket implements Packet {
    private UUID playerId;
    private String playerName;
    private boolean online;

    public PlayerStatePacket() {
    }

    public PlayerStatePacket(@NotNull UUID playerId, @NotNull String playerName, boolean online) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.online = online;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeLong(playerId.getMostSignificantBits());
        out.writeLong(playerId.getLeastSignificantBits());
        out.writeUTF(playerName);
        out.writeBoolean(online);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        playerId = new UUID(in.readLong(), in.readLong());
        playerName = in.readUTF();
        online = in.readBoolean();
    }

    @NotNull
    public UUID getPlayerId() {
        return playerId;
    }

    @NotNull
    public String getPlayerName() {
        return playerName;
    }

    public boolean isOnline() {
        return online;
    }
}
