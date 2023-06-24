package de.codingair.tradesystem.proxy.packets;

import de.codingair.packetmanagement.packets.Packet;

public enum PacketType {
    PlayerJoinPacket(PlayerJoinPacket.class),
    PlayerQuitPacket(PlayerQuitPacket.class),

    TradeInvitePacket(TradeInvitePacket.class),
    TradeInvitePacket_ResultPacket(de.codingair.tradesystem.proxy.packets.TradeInvitePacket.ResultPacket.class),
    InviteResponsePacket(InviteResponsePacket.class),
    InviteResponsePacket_ResultPacket(InviteResponsePacket.ResultPacket.class),
    TradeItemUpdatePacket(TradeItemUpdatePacket.class),
    TradeStateUpdatePacket(TradeStateUpdatePacket.class),
    PlayerInventoryPacket(PlayerInventoryPacket.class),
    TradeCheckFinishPacket(TradeCheckFinishPacket.class),
    SynchronizePlayersPacket(SynchronizePlayersPacket.class),
    TradeIconUpdatePacket(TradeIconUpdatePacket.class),
    PlayerStatePacket(PlayerStatePacket.class),
    PublishSkinPacket(PublishSkinPacket.class),
    ;

    private final Class<? extends Packet> packetClass;

    PacketType(Class<? extends Packet> packetClass) {
        this.packetClass = packetClass;
    }

    public Class<? extends Packet> getPacketClass() {
        return packetClass;
    }
}
