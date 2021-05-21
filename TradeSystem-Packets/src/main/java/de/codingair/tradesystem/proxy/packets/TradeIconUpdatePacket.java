package de.codingair.tradesystem.proxy.packets;

import de.codingair.packetmanagement.packets.impl.StringPacket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TradeIconUpdatePacket extends StringPacket {
    private String sender, recipient;
    private int slot;

    public TradeIconUpdatePacket() {
    }

    public TradeIconUpdatePacket(int slot, byte[] out) {
        super(new String(out));
        this.slot = slot;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        super.write(out);
        out.writeUTF(this.sender);
        out.writeUTF(this.recipient);
        out.writeByte(this.slot);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        super.read(in);
        this.sender = in.readUTF();
        this.recipient = in.readUTF();
        this.slot = in.readUnsignedByte();
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
}
