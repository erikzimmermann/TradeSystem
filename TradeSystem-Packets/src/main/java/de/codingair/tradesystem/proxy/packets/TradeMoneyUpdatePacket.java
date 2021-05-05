package de.codingair.tradesystem.proxy.packets;

import de.codingair.packetmanagement.packets.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TradeMoneyUpdatePacket implements Packet {
    private String sender, recipient;
    private double money;

    public TradeMoneyUpdatePacket() {
    }

    public TradeMoneyUpdatePacket(String sender, String recipient, double money) {
        this.sender = sender;
        this.recipient = recipient;
        this.money = money;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(this.sender);
        out.writeUTF(this.recipient);
        out.writeDouble(this.money);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        this.sender = in.readUTF();
        this.recipient = in.readUTF();
        this.money = in.readDouble();
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public double getMoney() {
        return money;
    }
}
