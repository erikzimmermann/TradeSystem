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
 * Packet to respond to an open invitation of a player on another server.
 */
public class InviteResponsePacket implements RequestPacket<InviteResponsePacket.ResultPacket> {
    private String inviter;
    private String responding;
    private UUID respondingId;
    private String respondingServer;
    private String respondingWorld;
    private boolean accept;
    private boolean expire; //silently (e.g. quitting the network)

    public InviteResponsePacket() {
    }

    public InviteResponsePacket(@NotNull String inviter, @NotNull String responding, @NotNull UUID respondingId, @NotNull String respondingWorld, boolean accept, boolean expire) {
        this.inviter = inviter;
        this.responding = responding;
        this.respondingId = respondingId;
        this.respondingWorld = respondingWorld;
        this.accept = accept;
        this.expire = expire;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(this.inviter);
        out.writeUTF(this.responding);
        out.writeLong(this.respondingId.getMostSignificantBits());
        out.writeLong(this.respondingId.getLeastSignificantBits());

        ByteMask mask = new ByteMask();
        mask.setBit(0, accept);
        mask.setBit(1, expire);
        mask.setBit(2, respondingServer != null);
        mask.write(out);

        if (respondingServer != null) out.writeUTF(respondingServer);
        out.writeUTF(respondingWorld);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        this.inviter = in.readUTF();
        this.responding = in.readUTF();
        this.respondingId = new UUID(in.readLong(), in.readLong());

        ByteMask mask = new ByteMask();
        mask.read(in);
        this.accept = mask.getBit(0);
        this.expire = mask.getBit(1);

        if (mask.getBit(2)) this.respondingServer = in.readUTF();
        this.respondingWorld = in.readUTF();
    }

    @NotNull
    public String getInviter() {
        return inviter;
    }

    @NotNull
    public String getResponding() {
        return responding;
    }

    @NotNull
    public UUID getRespondingId() {
        return respondingId;
    }

    public boolean isAccept() {
        return accept;
    }

    @NotNull
    public String getRespondingServer() {
        if (respondingServer == null) throw new IllegalStateException("TradeProxy did not set responding server!");
        return respondingServer;
    }

    public void setRespondingServer(@NotNull String respondingServer) {
        this.respondingServer = respondingServer;
    }

    @NotNull
    public String getRespondingWorld() {
        return respondingWorld;
    }

    public InviteResponsePacket setAccept(boolean accept) {
        this.accept = accept;
        return this;
    }

    public boolean isExpire() {
        return expire;
    }

    public InviteResponsePacket setExpire(boolean expire) {
        this.expire = expire;
        return this;
    }

    public enum Result {
        SUCCESS,
        NOT_ONLINE,
        OTHER_GROUP,
    }

    public static class ResultPacket implements ResponsePacket {
        private Result result;
        private @Nullable String server;
        private @Nullable String world;

        public ResultPacket() {
        }

        public ResultPacket(@NotNull Result result, @Nullable String world) {
            this.result = result;
            this.world = world;
        }

        @Override
        public void write(DataOutputStream out) throws IOException {
            out.writeByte(result.ordinal());

            ByteMask flags = new ByteMask();
            flags.setBit(0, server != null);
            flags.setBit(1, world != null);
            flags.write(out);

            if (server != null) out.writeUTF(server);
            if (world != null) out.writeUTF(world);
        }

        @Override
        public void read(DataInputStream in) throws IOException {
            result = Result.values()[in.readByte()];

            ByteMask flags = new ByteMask();
            flags.read(in);

            if (flags.getBit(0)) server = in.readUTF();
            if (flags.getBit(1)) world = in.readUTF();
        }

        public Result getResult() {
            return result;
        }

        @NotNull
        public String getServer() {
            if (server == null) throw new IllegalStateException("Server is null");
            return server;
        }

        @NotNull
        public ResultPacket setServer(@NotNull String server) {
            this.server = server;
            return this;
        }

        @NotNull
        public String getWorld() {
            if (world == null) throw new IllegalStateException("World is null");
            return world;
        }
    }
}
