package de.codingair.tradesystem.proxy.packets;

import de.codingair.packetmanagement.packets.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TradeIconUpdatePacket implements Packet {
    private String sender, recipient;
    private int slot;
    private byte[] data;

    public TradeIconUpdatePacket() {
    }

    public TradeIconUpdatePacket(String sender, String recipient, int slot, byte[] data) {
        this.sender = sender;
        this.recipient = recipient;
        this.slot = slot;
        this.data = data;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(this.sender);
        out.writeUTF(this.recipient);
        out.writeByte(this.slot);
        out.writeUTF(new String(this.data));
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        this.sender = in.readUTF();
        this.recipient = in.readUTF();
        this.slot = in.readUnsignedByte();
        this.data = in.readUTF().getBytes();
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public int getSlot() {
        return slot;
    }

    public byte[] getData() {
        return data;
    }
}
