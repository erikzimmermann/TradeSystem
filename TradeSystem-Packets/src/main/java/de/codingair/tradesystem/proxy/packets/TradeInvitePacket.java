package de.codingair.tradesystem.proxy.packets;

import de.codingair.packetmanagement.packets.RequestPacket;
import de.codingair.packetmanagement.packets.ResponsePacket;
import de.codingair.packetmanagement.utils.ByteMask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Packet to invite a player on another server.
 */
public class TradeInvitePacket implements RequestPacket<TradeInvitePacket.ResultPacket> {
    private String inviter;
    private UUID inviterId;
    private String recipient;
    private int tradeHash;

    public TradeInvitePacket() {
    }

    public TradeInvitePacket(String inviter, UUID inviterId, String recipient, int tradeHash) {
        this.inviter = inviter;
        this.inviterId = inviterId;
        this.recipient = recipient;
        this.tradeHash = tradeHash;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(this.inviter);
        out.writeLong(this.inviterId.getMostSignificantBits());
        out.writeLong(this.inviterId.getLeastSignificantBits());
        out.writeUTF(this.recipient);
        out.writeInt(this.tradeHash);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        this.inviter = in.readUTF();
        this.inviterId = new UUID(in.readLong(), in.readLong());
        this.recipient = in.readUTF();
        this.tradeHash = in.readInt();
    }

    public String getInviter() {
        return inviter;
    }

    public UUID getInviterId() {
        return inviterId;
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
        private @Nullable UUID recipientId;
        private @Nullable String server;

        public ResultPacket() {
        }

        public ResultPacket(Result result, @Nullable UUID recipientId) {
            this.result = result;
            this.recipientId = recipientId;
        }

        public ResultPacket(Result result, @Nullable UUID recipientId, @Nullable String server) {
            this.result = result;
            this.recipientId = recipientId;
            this.server = server;
        }

        @Override
        public void write(DataOutputStream out) throws IOException {
            ByteMask mask = new ByteMask((byte) result.ordinal());

            //use negation bit for server indication
            mask.setBit(7, this.server != null);
            mask.write(out);

            if (this.server != null) out.writeUTF(this.server);

            out.writeBoolean(this.recipientId != null);
            if (this.recipientId != null) {
                out.writeLong(this.recipientId.getMostSignificantBits());
                out.writeLong(this.recipientId.getLeastSignificantBits());
            }
        }

        @Override
        public void read(DataInputStream in) throws IOException {
            ByteMask mask = new ByteMask();
            mask.read(in);

            boolean hasServer = mask.getBit(7);
            mask.setBit(7, false);

            result = Result.values()[mask.getByte()];
            if (hasServer) this.server = in.readUTF();

            if (in.readBoolean()) this.recipientId = new UUID(in.readLong(), in.readLong());
        }

        @NotNull
        public Result getResult() {
            return result;
        }

        @Nullable
        public UUID getRecipientId() {
            return recipientId;
        }

        @Nullable
        public String getServer() {
            return server;
        }
    }
}
