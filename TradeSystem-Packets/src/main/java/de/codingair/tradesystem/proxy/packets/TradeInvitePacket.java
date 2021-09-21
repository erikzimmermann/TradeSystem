package de.codingair.tradesystem.proxy.packets;

import de.codingair.packetmanagement.packets.RequestPacket;
import de.codingair.packetmanagement.packets.ResponsePacket;
import de.codingair.packetmanagement.utils.ByteMask;
import org.jetbrains.annotations.Nullable;

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
        IS_ALREADY_TRADING,
        START_TRADING,
        PLUGIN
    }

    public static class ResultPacket implements ResponsePacket {
        private Result result;
        private @Nullable String server;

        public ResultPacket() {
        }

        public ResultPacket(Result result) {
            this.result = result;
        }

        public ResultPacket(Result result, @Nullable String server) {
            this.result = result;
            this.server = server;
        }

        @Override
        public void write(DataOutputStream out) throws IOException {
            ByteMask mask = new ByteMask((byte) result.ordinal());

            //use negation bit for server indication
            mask.setBit(7, this.server != null);
            mask.write(out);

            if (this.server != null) out.writeUTF(this.server);
        }

        @Override
        public void read(DataInputStream in) throws IOException {
            ByteMask mask = new ByteMask();
            mask.read(in);

            boolean hasServer = mask.getBit(7);
            mask.setBit(7, false);

            result = Result.values()[mask.getByte()];
            if (hasServer) this.server = in.readUTF();
        }

        public Result getResult() {
            return result;
        }

        public @Nullable String getServer() {
            return server;
        }
    }
}
