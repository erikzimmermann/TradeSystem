package de.codingair.tradesystem.spigot.trade.managers;

import de.codingair.codingapi.player.chat.ChatButton;
import de.codingair.codingapi.player.chat.SimpleMessage;
import de.codingair.tradesystem.proxy.packets.TradeInvitePacket;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.events.TradeRequestEvent;
import de.codingair.tradesystem.spigot.utils.Lang;
import de.codingair.tradesystem.spigot.utils.Permissions;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

public class RuleManager {

    public static boolean isViolatingRules(Player p) {
        if (Permissions.PERMISSION != null && !p.hasPermission(Permissions.PERMISSION)) {
            p.sendMessage(Lang.getPrefix() + "§c" + Lang.get("Not_Able_To_Trade", p));
            return true;
        }

        if (TradeSystem.getInstance().getTradeManager().isOffline(p)) {
            notifyOfflinePlayer(p);
            return true;
        }

        if (TradeSystem.getInstance().getTradeManager().isBlockedWorld(p.getWorld())) {
            p.sendMessage(Lang.getPrefix() + "§c" + Lang.get("Cannot_trade_in_world", p));
            return true;
        }

        if (!TradeSystem.getInstance().getTradeManager().getAllowedGameModes().contains(p.getGameMode().name())) {
            p.sendMessage(Lang.getPrefix() + Lang.get("Cannot_trade_in_that_GameMode", p));
            return true;
        }

        if (p.isSleeping()) {
            p.sendMessage(Lang.getPrefix() + Lang.get("Cannot_trade_in_bed", p));
            return true;
        }

        return false;
    }

    public static void handle(Player player, String other, TradeInvitePacket.ResultPacket result) {
        if (result.getResult() == TradeInvitePacket.Result.INVITED) {
            InvitationManager.registerExpiration(player, player.getName(), null, other);
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

        if (TradeSystem.man().isTrading(other)) {
            return TradeInvitePacket.Result.IS_ALREADY_TRADING;
        }

        return TradeInvitePacket.Result.INVITED;
    }

    public static boolean isViolatingRules(Player p, Player other) {
        if (isViolatingRules(p)) return true;

        //pre rules
        if (other == null) {
            p.sendMessage(Lang.getPrefix() + Lang.get("Player_Not_Online", p));
            return true;
        }

        if (other.equals(p)) {
            p.sendMessage(Lang.getPrefix() + Lang.get("Cannot_Trade_With_Yourself", p));
            return true;
        }

        //main rules
        if (Permissions.PERMISSION != null && !other.hasPermission(Permissions.PERMISSION)) {
            message(p, other.getName(), TradeInvitePacket.Result.NO_PERMISSION, null);
            return true;
        }

        if (TradeSystem.getInstance().getTradeManager().isOffline(other)) {
            message(p, other.getName(), TradeInvitePacket.Result.MARKED_AS_OFFLINE, null);
            return true;
        }

        if (TradeSystem.getInstance().getTradeManager().isBlockedWorld(other.getWorld())) {
            message(p, other.getName(), TradeInvitePacket.Result.BLOCKED_WORLD, null);
            return true;
        }

        if (!TradeSystem.getInstance().getTradeManager().getAllowedGameModes().contains(other.getGameMode().name())) {
            message(p, other.getName(), TradeInvitePacket.Result.GAME_MODE, null);
            return true;
        }

        if (other.isSleeping()) {
            message(p, other.getName(), TradeInvitePacket.Result.SLEEPING, null);
            return true;
        }

        //post rules
        if (!other.canSee(p)) {
            p.sendMessage(Lang.getPrefix() + Lang.get("Cannot_trade_while_invisible", p));
            return true;
        }

        if (!p.canSee(other)) {
            p.sendMessage(Lang.getPrefix() + Lang.get("Player_Not_Online", p));
            return true;
        }

        if (TradeSystem.getInstance().getTradeManager().getDistance() > 0) {
            if (!p.getWorld().equals(other.getWorld()) || p.getLocation().distance(other.getLocation()) > TradeSystem.getInstance().getTradeManager().getDistance()) {
                p.sendMessage(Lang.getPrefix() + "§c" + Lang.get("Player_is_not_in_range", p).replace("%player%", other.getName()));
                return true;
            }
        }

        return false;
    }

    public static void message(Player player, String other, TradeInvitePacket.Result result, @Nullable String server) {
        switch (result) {
            case NO_PERMISSION:
                player.sendMessage(Lang.getPrefix() + "§c" + Lang.get("Player_Is_Not_Able_Trade", player));
                break;

            case NOT_ONLINE:
            case MARKED_AS_OFFLINE:
            case SLEEPING:
                player.sendMessage(Lang.getPrefix() + Lang.get("Trade_Partner_is_Offline", player));
                break;

            case OTHER_GROUP:
            case INCOMPATIBLE:
                assert server != null;
                TradeSystem.getInstance().getLogger().log(Level.WARNING,
                        "\"" + player.getName() + "\" tried to trade with \"" + other + "\" on server \"" + server + "\" but the trade configurations from both servers are incompatible.\n\n" +
                                "You have two options to solve this:\n" +
                                "1. Use the group function in the trade-configuration file on your proxy to separate both servers from each other or\n" +
                                "2. Copy the Config.yml from one server to the other server"
                );
                player.sendMessage(Lang.getPrefix() + Lang.get("Trade_Partner_is_Offline", player));
                break;

            case BLOCKED_WORLD:
                player.sendMessage(Lang.getPrefix() + "§c" + Lang.get("Other_cannot_trade_in_world", player));
                break;

            case GAME_MODE:
                player.sendMessage(Lang.getPrefix() + Lang.get("Other_cannot_trade_in_that_GameMode", player));
                break;

            case INVITED:
                player.sendMessage(Lang.getPrefix() + Lang.get("Player_Is_Invited", player).replace("%player%", other));
                break;

            case IS_ALREADY_TRADING:
                player.sendMessage(Lang.getPrefix() + Lang.get("Other_is_already_trading", player));
                break;
        }
    }

    private static void notifyOfflinePlayer(Player p) {
        String[] a = Lang.get("Trade_You_are_Offline", p).split("%command%", -1);

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
