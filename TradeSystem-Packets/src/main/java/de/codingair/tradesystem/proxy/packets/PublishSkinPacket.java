package de.codingair.tradesystem.proxy.packets;

import de.codingair.packetmanagement.packets.Packet;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PublishSkinPacket implements Packet {
    private String skinId;

    public PublishSkinPacket() {
    }

    public PublishSkinPacket(@NotNull String skinId) {
        this.skinId = skinId;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(skinId);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        skinId = in.readUTF();
    }

    @NotNull
    public String getSkinId() {
        return skinId;
    }
}
