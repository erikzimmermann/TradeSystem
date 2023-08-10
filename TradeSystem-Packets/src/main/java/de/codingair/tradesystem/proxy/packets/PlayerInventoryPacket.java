package de.codingair.tradesystem.proxy.packets;

import de.codingair.packetmanagement.packets.Packet;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PlayerInventoryPacket implements Packet {
    private String sender, recipient;
    private byte[] item;
    private int slot;

    public PlayerInventoryPacket() {
    }

    public PlayerInventoryPacket(String sender, String recipient, byte @Nullable [] item, int slot) throws IOException {
        this.sender = sender;
        this.recipient = recipient;
        this.item = item;
        this.slot = slot;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(this.sender);
        out.writeUTF(this.recipient);

        out.writeByte(this.slot);
        out.writeBoolean(item != null);
        if (item != null) {
            out.writeShort(item.length);
            out.write(item, 0, item.length);
        }
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        this.sender = in.readUTF();
        this.recipient = in.readUTF();

        this.slot = in.readUnsignedByte();
        boolean notNull = in.readBoolean();

        if (notNull) {
            int length = in.readUnsignedShort();
            this.item = new byte[length];
            in.readFully(this.item);
        }
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public byte @Nullable [] getItem() throws IOException {
        return item;
    }

    public int getSlot() {
        return slot;
    }
}
