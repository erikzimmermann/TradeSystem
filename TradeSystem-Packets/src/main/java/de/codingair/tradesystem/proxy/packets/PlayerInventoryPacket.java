package de.codingair.tradesystem.proxy.packets;

import de.codingair.packetmanagement.packets.Packet;
import de.codingair.packetmanagement.utils.SerializedGeneric;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

public class PlayerInventoryPacket implements Packet {
    private String sender, recipient;
    private SerializedGeneric item;
    private int slot;

    public PlayerInventoryPacket() {
    }

    public PlayerInventoryPacket(String sender, String recipient, @Nullable Map<String, Object> item, int slot) throws IOException {
        this.sender = sender;
        this.recipient = recipient;
        this.item = item == null ? null : new SerializedGeneric(item);
        this.slot = slot;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(this.sender);
        out.writeUTF(this.recipient);

        out.writeByte(this.slot);
        out.writeBoolean(item != null);
        if (item != null) item.write(out);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        this.sender = in.readUTF();
        this.recipient = in.readUTF();

        this.slot = in.readUnsignedByte();
        boolean notNull = in.readBoolean();

        if (notNull) {
            this.item = new SerializedGeneric();
            item.read(in);
        }
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    @Nullable
    public Map<String, Object> getItem() throws IOException {
        //noinspection unchecked
        return (Map<String, Object>) item.getObject();
    }

    public int getSlot() {
        return slot;
    }
}
