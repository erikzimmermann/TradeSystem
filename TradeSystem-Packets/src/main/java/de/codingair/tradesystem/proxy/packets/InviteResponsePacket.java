package de.codingair.tradesystem.proxy.packets;

import de.codingair.packetmanagement.packets.RequestPacket;
import de.codingair.packetmanagement.packets.ResponsePacket;
import de.codingair.packetmanagement.utils.ByteMask;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Packet to respond to an open invitation of a player on another server.
 */
public class InviteResponsePacket implements RequestPacket<InviteResponsePacket.ResultPacket> {
    private String inviter;
    private String responding;
    private boolean accept;
    private boolean expire; //silently (e.g. quitting the network)

    public InviteResponsePacket() {
    }

    public InviteResponsePacket(String inviter, String responding, boolean accept, boolean expire) {
        this.inviter = inviter;
        this.responding = responding;
        this.accept = accept;
        this.expire = expire;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(this.inviter);
        out.writeUTF(this.responding);

        ByteMask mask = new ByteMask();
        mask.setBit(0, accept);
        mask.setBit(1, expire);
        mask.write(out);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        this.inviter = in.readUTF();
        this.responding = in.readUTF();

        ByteMask mask = new ByteMask();
        mask.read(in);
        this.accept = mask.getBit(0);
        this.expire = mask.getBit(1);
    }

    public String getInviter() {
        return inviter;
    }

    public String getResponding() {
        return responding;
    }

    public boolean isAccept() {
        return accept;
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
