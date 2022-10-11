package de.codingair.tradesystem.proxy.packets;

import de.codingair.packetmanagement.packets.Packet;
import de.codingair.packetmanagement.utils.SerializedGeneric;
import org.jetbrains.annotations.NotNull;
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

    public TradeItemUpdatePacket(@NotNull String sender, @NotNull String recipient, @Nullable Map<String, Object> item, byte slotId) throws IOException {
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

    @NotNull
    public String getSender() {
        return sender;
    }

    @NotNull
    public String getRecipient() {
        return recipient;
    }

    @Nullable
    public Map<String, Object> getItem() throws IOException {
        try {
            return item == null ? null : item.getObject();
        } catch (Exception e) {
            throw new RuntimeException(String.format("Error while reading item in slotId %d (the id of the slot; only counts the trading slots). Please forward this error including the information about the traded item of %s to %s.", slotId, sender, recipient), e);
        }
    }

    public int getSlotId() {
        return slotId;
    }
}
