package de.codingair.tradesystem.proxy.packets;

import de.codingair.packetmanagement.packets.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Used on proxy to re-synchronize with the sending backend server and on the backend server to clear the player list.
 */
public class SynchronizePlayersPacket implements Packet {
    @Override
    public void write(DataOutputStream out) throws IOException {

    }

    @Override
    public void read(DataInputStream in) throws IOException {

    }
}
