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
    private Map<String, Object> item;
    private int slotId; //own slotId

    public TradeItemUpdatePacket() {
    }

    public TradeItemUpdatePacket(String sender, String recipient, @Nullable Map<String, Object> item, byte slotId) {
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
        if (this.item != null) {
            SerializedGeneric item = new SerializedGeneric(this.item);
            item.write(out);
        }

        out.writeByte(this.slotId);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        this.sender = in.readUTF();
        this.recipient = in.readUTF();

        boolean notNull = in.readBoolean();
        if (notNull) {
            SerializedGeneric item = new SerializedGeneric();
            item.read(in);
            //noinspection unchecked
            this.item = (Map<String, Object>) item.getObject();
        }

        this.slotId = in.readUnsignedByte();
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public Map<String, Object> getItem() {
        return item;
    }

    public int getSlotId() {
        return slotId;
    }
}
