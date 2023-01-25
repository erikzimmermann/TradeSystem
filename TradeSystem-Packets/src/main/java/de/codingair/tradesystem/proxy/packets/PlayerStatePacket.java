package de.codingair.tradesystem.proxy.packets;

import de.codingair.packetmanagement.packets.Packet;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PlayerStatePacket implements Packet {
    private String player;
    private boolean online;

    public PlayerStatePacket() {
    }

    public PlayerStatePacket(@NotNull String player, boolean online) {
        this.player = player;
        this.online = online;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(player);
        out.writeBoolean(online);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        player = in.readUTF();
        online = in.readBoolean();
    }

    @NotNull
    public String getPlayer() {
        return player;
    }

    public boolean isOnline() {
        return online;
    }
}
