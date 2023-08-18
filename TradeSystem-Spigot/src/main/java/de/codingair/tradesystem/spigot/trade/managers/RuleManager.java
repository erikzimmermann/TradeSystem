package de.codingair.tradesystem.spigot.trade.managers;

import de.codingair.codingapi.player.chat.ChatButton;
import de.codingair.codingapi.player.chat.SimpleMessage;
import de.codingair.tradesystem.proxy.packets.TradeInvitePacket;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.utils.Lang;
import de.codingair.tradesystem.spigot.utils.Permissions;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

public class RuleManager {

    public static boolean isViolatingRules(Player p) {
        if (Permissions.PERMISSION != null && !p.hasPermission(Permissions.PERMISSION)) {
            Lang.send(p, "§c", "Not_Able_To_Trade");
            return true;
        }

        if (TradeSystem.getInstance().getTradeManager().isOffline(p)) {
            notifyOfflinePlayer(p);
            return true;
        }

        if (TradeSystem.getInstance().getTradeManager().isBlockedWorld(p.getWorld())) {
            Lang.send(p, "§c", "Cannot_trade_in_world");
            return true;
        }

        if (!TradeSystem.getInstance().getTradeManager().getAllowedGameModes().contains(p.getGameMode().name())) {
            Lang.send(p, "Cannot_trade_in_that_GameMode");
            return true;
        }

        if (p.isSleeping()) {
            Lang.send(p, "Cannot_trade_in_bed");
            return true;
        }

        return false;
    }

    public static void handle(@NotNull Player player, @NotNull String other, @NotNull TradeInvitePacket.ResultPacket result) {
        if (result.getResult() == TradeInvitePacket.Result.INVITED) {
            InvitationManager.registerInvitation(player, player.getName(), result.getRecipientId(), null, other);
        }

        message(player, other, result.getResult(), result.getServer());
    }

    public static TradeInvitePacket.Result isOtherViolatingRules(@NotNull Player other) {
        if (Permissions.PERMISSION != null && !other.hasPermission(Permissions.PERMISSION)) {
            return TradeInvitePacket.Result.NO_PERMISSION;
        }

        if (TradeSystem.getInstance().getTradeManager().isOffline(other)) {
            return TradeInvitePacket.Result.MARKED_AS_OFFLINE;
        }

        if (TradeSystem.getInstance().getTradeManager().isBlockedWorld(other.getWorld())) {
            return TradeInvitePacket.Result.BLOCKED_WORLD;
        }

        if (!TradeSystem.getInstance().getTradeManager().getAllowedGameModes().contains(other.getGameMode().name())) {
            return TradeInvitePacket.Result.GAME_MODE;
        }

        if (other.isSleeping()) {
            return TradeInvitePacket.Result.SLEEPING;
        }

        if (TradeSystem.handler().isTrading(other)) {
            return TradeInvitePacket.Result.IS_ALREADY_TRADING;
        }

        return TradeInvitePacket.Result.INVITED;
    }

    public static boolean isViolatingRules(Player player, Player other) {
        if (isViolatingRules(player)) return true;

        //pre rules
        if (other == null || !player.canSee(other)) {
            Lang.send(player, "Player_Not_Online");
            return true;
        }

        if (other.equals(player)) {
            Lang.send(player, "Cannot_Trade_With_Yourself");
            return true;
        }

        //main rules
        if (Permissions.PERMISSION != null && !other.hasPermission(Permissions.PERMISSION)) {
            message(player, other.getName(), TradeInvitePacket.Result.NO_PERMISSION, null);
            return true;
        }

        if (TradeSystem.getInstance().getTradeManager().isOffline(other)) {
            message(player, other.getName(), TradeInvitePacket.Result.MARKED_AS_OFFLINE, null);
            return true;
        }

        if (TradeSystem.getInstance().getTradeManager().isBlockedWorld(other.getWorld())) {
            message(player, other.getName(), TradeInvitePacket.Result.BLOCKED_WORLD, null);
            return true;
        }

        if (!TradeSystem.getInstance().getTradeManager().getAllowedGameModes().contains(other.getGameMode().name())) {
            message(player, other.getName(), TradeInvitePacket.Result.GAME_MODE, null);
            return true;
        }

        if (other.isSleeping()) {
            message(player, other.getName(), TradeInvitePacket.Result.SLEEPING, null);
            return true;
        }

        //post rules
        if (!other.canSee(player)) {
            Lang.send(player, "Cannot_trade_while_invisible");
            return true;
        }

        if (TradeSystem.getInstance().getTradeManager().getDistance() > 0) {
            if (!player.getWorld().equals(other.getWorld()) || player.getLocation().distance(other.getLocation()) > TradeSystem.getInstance().getTradeManager().getDistance()) {
                Lang.send(player, "§c", "Player_is_not_in_range", new Lang.P("player", other.getName()));
                return true;
            }
        }

        return false;
    }

    public static void message(Player player, String other, TradeInvitePacket.Result result, @Nullable String server) {
        switch (result) {
            case NO_PERMISSION:
                Lang.send(player, "§c", "Player_Is_Not_Able_Trade");
                break;

            case NOT_ONLINE:
            case MARKED_AS_OFFLINE:
            case SLEEPING:
                Lang.send(player, "Trade_Partner_is_Offline");
                break;

            case OTHER_GROUP:
            case INCOMPATIBLE:
                assert server != null;
                TradeSystem.getInstance().getLogger().log(Level.WARNING,
                        "\"" + player.getName() + "\" tried to trade with \"" + other + "\" on server \"" + server + "\" but the trade configurations/versions from both servers are incompatible.\n\n" +
                                "You have two options to solve this:\n" +
                                "1. Use the group function in the trade-configuration file on your proxy to separate both servers from each other or\n" +
                                "2. Copy the Config.yml from one server to the other server"
                );
                Lang.send(player, "Trade_Partner_is_Offline");
                break;

            case BLOCKED_WORLD:
                Lang.send(player, "§c", "Other_cannot_trade_in_world");
                break;

            case GAME_MODE:
                Lang.send(player, "Other_cannot_trade_in_that_GameMode");
                break;

            case INVITED:
                Lang.send(player, "Player_Is_Invited", new Lang.P("player", other));
                break;

            case IS_ALREADY_TRADING:
                Lang.send(player, "Other_is_already_trading");
                break;
        }
    }

    private static void notifyOfflinePlayer(Player p) {
        String text = Lang.get("Trade_You_are_Offline", p);
        if (text.isEmpty()) return;

        String[] a = text.split("%command%", -1);

        if (a.length != 3) {
            p.sendMessage(Lang.getPrefix() + text);
            return;
        }

        String s0 = a[0];
        String s = a[1];
        String s1 = a[2];

        SimpleMessage message = new SimpleMessage(Lang.getPrefix() + s0, TradeSystem.getInstance());
        message.add(new ChatButton(s, Lang.get("Want_To_Trade_Hover", p)) {
            @Override
            public void onClick(Player player) {
                p.performCommand("trade toggle");
                message.destroy();
            }
        }.setType("TRADE_TOGGLE"));

        message.setTimeOut(10);
        message.add(new TextComponent(s1));
        message.send(p);
    }
}
