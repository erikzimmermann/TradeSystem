package de.codingair.tradesystem.proxy.packets;

import de.codingair.packetmanagement.packets.Packet;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public class PlayerJoinPacket implements Packet {
    private String player;
    private UUID playerId;

    public PlayerJoinPacket() {
    }

    public PlayerJoinPacket(@NotNull String player, @NotNull UUID playerId) {
        this.player = player;
        this.playerId = playerId;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(this.player);
        out.writeLong(this.playerId.getMostSignificantBits());
        out.writeLong(this.playerId.getLeastSignificantBits());
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        this.player = in.readUTF();
        this.playerId = new UUID(in.readLong(), in.readLong());
    }

    @NotNull
    public String getPlayer() {
        return player;
    }

    @NotNull
    public UUID getPlayerId() {
        return playerId;
    }
}
