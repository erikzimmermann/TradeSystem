package de.codingair.tradesystem.proxy.packets;

import de.codingair.packetmanagement.packets.Packet;
import de.codingair.packetmanagement.utils.SerializedGeneric;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

public class PlayerInventoryPacket implements Packet {
    private String sender, recipient;
    private Map<?, ?>[] items;

    public PlayerInventoryPacket() {
    }

    public PlayerInventoryPacket(String sender, String recipient, @NotNull Map<String, Object>[] items) {
        this.sender = sender;
        this.recipient = recipient;
        this.items = items;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(this.sender);
        out.writeUTF(this.recipient);

        out.writeShort(items.length);
        for (Map<?, ?> item : this.items) {
            out.writeBoolean(item != null);

            if (item != null) {
                SerializedGeneric generic = new SerializedGeneric(item);
                generic.write(out);
            }
        }
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        this.sender = in.readUTF();
        this.recipient = in.readUTF();

        int size = in.readUnsignedShort();
        this.items = new Map[size];
        for (int i = 0; i < size; i++) {
            boolean notNull = in.readBoolean();

            if (notNull) {
                SerializedGeneric item = new SerializedGeneric();
                item.read(in);
                this.items[i] = (Map<?, ?>) item.getObject();
            }
        }
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public Map<?, ?>[] getItems() {
        return items;
    }
}
