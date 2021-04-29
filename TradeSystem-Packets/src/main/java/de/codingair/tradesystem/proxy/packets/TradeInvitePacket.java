package de.codingair.tradesystem.proxy.packets;

import de.codingair.packetmanagement.packets.RequestPacket;
import de.codingair.packetmanagement.packets.ResponsePacket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Packet to invite a player on another server.
 */
public class TradeInvitePacket implements RequestPacket<TradeInvitePacket.ResultPacket> {
    private String inviter;
    private String recipient;
    private int tradeHash;

    public TradeInvitePacket() {
    }

    public TradeInvitePacket(String inviter, String recipient, int tradeHash) {
        this.inviter = inviter;
        this.recipient = recipient;
        this.tradeHash = tradeHash;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(this.inviter);
        out.writeUTF(this.recipient);
        out.writeInt(this.tradeHash);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        this.inviter = in.readUTF();
        this.recipient = in.readUTF();
        this.tradeHash = in.readInt();
    }

    public String getInviter() {
        return inviter;
    }

    public String getRecipient() {
        return recipient;
    }

    public int getTradeHash() {
        return tradeHash;
    }

    public enum Result {
        INVITED,
        INCOMPATIBLE,
        OTHER_GROUP,
        NOT_ONLINE,

        NO_PERMISSION,
        MARKED_AS_OFFLINE,
        BLOCKED_WORLD,
        GAME_MODE,
        SLEEPING,
        IS_ALREADY_TRADING
    }

    public static class ResultPacket implements ResponsePacket {
        private Result result;

        public ResultPacket() {
        }

        public ResultPacket(Result result) {
            this.result = result;
        }

        @Override
        public void write(DataOutputStream out) throws IOException {
            out.writeByte(result.ordinal());
        }

        @Override
        public void read(DataInputStream in) throws IOException {
            result = Result.values()[in.readByte()];
        }

        public Result getResult() {
            return result;
        }
    }
}
