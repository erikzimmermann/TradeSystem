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
    private String inviterWorld;
    private String inviterServer;

    public TradeInvitePacket() {
    }

    public TradeInvitePacket(@NotNull String inviter, @NotNull UUID inviterId, @NotNull String recipient, int tradeHash, @NotNull String inviterWorld) {
        this.inviter = inviter;
        this.inviterId = inviterId;
        this.recipient = recipient;
        this.tradeHash = tradeHash;
        this.inviterWorld = inviterWorld;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(this.inviter);
        out.writeLong(this.inviterId.getMostSignificantBits());
        out.writeLong(this.inviterId.getLeastSignificantBits());
        out.writeUTF(this.recipient);
        out.writeInt(this.tradeHash);
        out.writeUTF(this.inviterWorld);
        out.writeBoolean(this.inviterServer != null);
        if (this.inviterServer != null) out.writeUTF(this.inviterServer);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        this.inviter = in.readUTF();
        this.inviterId = new UUID(in.readLong(), in.readLong());
        this.recipient = in.readUTF();
        this.tradeHash = in.readInt();
        this.inviterWorld = in.readUTF();
        if (in.readBoolean()) this.inviterServer = in.readUTF();
    }

    @NotNull
    public String getInviter() {
        return inviter;
    }

    @NotNull
    public UUID getInviterId() {
        return inviterId;
    }

    @NotNull
    public String getRecipient() {
        return recipient;
    }

    public int getTradeHash() {
        return tradeHash;
    }

    @NotNull
    public String getInviterWorld() {
        return inviterWorld;
    }

    public void setInviterServer(@NotNull String inviterServer) {
        this.inviterServer = inviterServer;
    }

    @NotNull
    public String getInviterServer() {
        if (inviterServer == null) throw new IllegalStateException("TradeProxy did not set the inviter server!");
        return inviterServer;
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
        private @Nullable String world;
        private @Nullable String server;

        public ResultPacket() {
        }

        public ResultPacket(@NotNull Result result, @Nullable UUID recipientId, @Nullable String recipientWorld) {
            this.result = result;
            this.recipientId = recipientId;
            this.world = recipientWorld;
        }

        @Override
        public void write(DataOutputStream out) throws IOException {
            ByteMask resultMask = new ByteMask((byte) result.ordinal());
            resultMask.write(out);

            ByteMask flags = new ByteMask();
            flags.setBit(0, this.server != null);
            flags.setBit(1, this.world != null);
            flags.setBit(2, this.recipientId != null);

            if (this.server != null) out.writeUTF(this.server);
            if (this.recipientId != null) {
                out.writeLong(this.recipientId.getMostSignificantBits());
                out.writeLong(this.recipientId.getLeastSignificantBits());
            }
        }

        @Override
        public void read(DataInputStream in) throws IOException {
            ByteMask resultMask = new ByteMask();
            resultMask.read(in);
            result = Result.values()[resultMask.getByte()];

            ByteMask flags = new ByteMask();
            flags.read(in);

            boolean hasServer = flags.getBit(0);
            boolean hasWorld = flags.getBit(1);
            boolean hasUUID = flags.getBit(2);

            if (hasServer) this.server = in.readUTF();
            if (hasWorld) this.world = in.readUTF();
            if (hasUUID) this.recipientId = new UUID(in.readLong(), in.readLong());
        }

        @NotNull
        public Result getResult() {
            return result;
        }

        @NotNull
        public UUID getRecipientId() {
            if (recipientId == null) throw new IllegalStateException("RecipientId is null");
            return recipientId;
        }

        @NotNull
        public String getServer() {
            if (server == null) throw new IllegalStateException("Server is null");
            return server;
        }

        public void setServer(@NotNull String server) {
            this.server = server;
        }

        @NotNull
        public String getWorld() {
            if (world == null) throw new IllegalStateException("World is null");
            return world;
        }
    }
}
