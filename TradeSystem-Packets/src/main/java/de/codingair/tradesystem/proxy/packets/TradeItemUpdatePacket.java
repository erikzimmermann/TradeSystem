package de.codingair.tradesystem.proxy.packets;

import de.codingair.packetmanagement.packets.Packet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TradeItemUpdatePacket implements Packet {
    private String sender, recipient;
    private byte[] item;
    private int slotId; //own slotId

    public TradeItemUpdatePacket() {
    }

    public TradeItemUpdatePacket(@NotNull String sender, @NotNull String recipient, byte @Nullable [] item, byte slotId) throws IOException {
        this.sender = sender;
        this.recipient = recipient;
        this.item = item;
        this.slotId = slotId;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(this.sender);
        out.writeUTF(this.recipient);

        out.writeBoolean(this.item != null);
        if (item != null) {
            out.writeShort(item.length);
            out.write(item, 0, item.length);
        }

        out.writeByte(this.slotId);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        this.sender = in.readUTF();
        this.recipient = in.readUTF();

        boolean notNull = in.readBoolean();
        if (notNull) {
            int length = in.readUnsignedShort();
            this.item = new byte[length];
            in.readFully(this.item);
        }

        this.slotId = in.readUnsignedByte();
    }

    @NotNull
    public String getSender() {
        return sender;
    }

    @NotNull
    public String getRecipient() {
        return recipient;
    }

    public byte @Nullable [] getItem() throws IOException {
        return item;
    }

    public int getSlotId() {
        return slotId;
    }
}
