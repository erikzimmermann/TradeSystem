package de.codingair.tradesystem.proxy.packets;

import de.codingair.packetmanagement.packets.Packet;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PublishSkinPacket implements Packet {
    private String player;
    private String skinId;

    public PublishSkinPacket() {
    }

    public PublishSkinPacket(@NotNull String player, @NotNull String skinId) {
        this.player = player;
        this.skinId = skinId;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(player);
        out.writeUTF(skinId);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        player = in.readUTF();
        skinId = in.readUTF();
    }

    @NotNull
    public String getPlayer() {
        return player;
    }

    @NotNull
    public String getSkinId() {
        return skinId;
    }
}
