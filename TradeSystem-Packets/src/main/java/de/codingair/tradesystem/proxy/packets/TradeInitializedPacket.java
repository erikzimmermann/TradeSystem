package de.codingair.tradesystem.proxy.packets;

import de.codingair.packetmanagement.packets.Packet;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TradeInitializedPacket implements Packet {
    private String player;

    public TradeInitializedPacket(@NotNull String player) {
        this.player = player;
    }

    public TradeInitializedPacket() {
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(player);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        player = in.readUTF();
    }

    public String getPlayer() {
        return player;
    }
}
