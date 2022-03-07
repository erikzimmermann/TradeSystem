package de.codingair.tradesystem.proxy.packets;

import de.codingair.packetmanagement.packets.Packet;
import de.codingair.packetmanagement.utils.SerializedGeneric;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

public class TradeItemUpdatePacket implements Packet {
    private String sender, recipient;
    private SerializedGeneric item;
    private int slotId; //own slotId

    public TradeItemUpdatePacket() {
    }

    public TradeItemUpdatePacket(String sender, String recipient, @Nullable Map<String, Object> item, byte slotId) throws IOException {
        this.sender = sender;
        this.recipient = recipient;
        this.item = item == null ? null : new SerializedGeneric(item);
        this.slotId = slotId;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(this.sender);
        out.writeUTF(this.recipient);

        out.writeBoolean(this.item != null);
        if (this.item != null) item.write(out);

        out.writeByte(this.slotId);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        this.sender = in.readUTF();
        this.recipient = in.readUTF();

        boolean notNull = in.readBoolean();
        if (notNull) {
            this.item = new SerializedGeneric();
            item.read(in);
        }

        this.slotId = in.readUnsignedByte();
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public Map<String, Object> getItem() throws IOException {
        //noinspection unchecked
        return (Map<String, Object>) item.getObject();
    }

    public int getSlotId() {
        return slotId;
    }
}
