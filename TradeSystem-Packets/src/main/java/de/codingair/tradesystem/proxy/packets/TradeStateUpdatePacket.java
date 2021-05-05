package de.codingair.tradesystem.proxy.packets;

import de.codingair.packetmanagement.packets.Packet;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TradeStateUpdatePacket implements Packet {
    private String sender, recipient;
    private State state;

    /**
     * Used for error messages in case of State.CANCELLED
     */
    private String extra;

    public TradeStateUpdatePacket() {
    }

    public TradeStateUpdatePacket(String sender, String recipient, State state, @Nullable String extra) {
        this.sender = sender;
        this.recipient = recipient;
        this.state = state;
        this.extra = extra;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(this.sender);
        out.writeUTF(this.recipient);
        out.writeByte(this.state.ordinal());

        out.writeBoolean(this.extra != null);
        if (this.extra != null) out.writeUTF(this.extra);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        this.sender = in.readUTF();
        this.recipient = in.readUTF();
        this.state = State.values()[in.readUnsignedByte()];

        boolean notNull = in.readBoolean();
        if (notNull) this.extra = in.readUTF();
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public State getState() {
        return state;
    }

    public String getExtra() {
        return extra;
    }

    public enum State {
        READY,
        NOT_READY,
        CANCELLED
    }
}
