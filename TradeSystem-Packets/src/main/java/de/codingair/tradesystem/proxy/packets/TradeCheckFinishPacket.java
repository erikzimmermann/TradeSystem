package de.codingair.tradesystem.proxy.packets;

import de.codingair.packetmanagement.packets.RequestPacket;
import de.codingair.packetmanagement.packets.impl.SuccessPacket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TradeCheckFinishPacket implements RequestPacket<SuccessPacket> {
    private String sender, recipient;

    public TradeCheckFinishPacket() {
    }

    public TradeCheckFinishPacket(String sender, String recipient) {
        this.sender = sender;
        this.recipient = recipient;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(this.sender);
        out.writeUTF(this.recipient);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        this.sender = in.readUTF();
        this.recipient = in.readUTF();
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }
}
