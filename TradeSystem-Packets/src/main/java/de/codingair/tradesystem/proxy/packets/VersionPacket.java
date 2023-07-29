package de.codingair.tradesystem.proxy.packets;

import de.codingair.packetmanagement.packets.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class VersionPacket implements Packet {
    private String version;

    public VersionPacket() {
    }

    public VersionPacket(String version) {
        this.version = version;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(version);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        version = in.readUTF();
    }

    public String getVersion() {
        return version;
    }
}
