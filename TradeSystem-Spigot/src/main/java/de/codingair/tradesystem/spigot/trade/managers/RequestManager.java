package de.codingair.tradesystem.spigot.trade.managers;

import de.codingair.codingapi.player.chat.SimpleMessage;
import de.codingair.tradesystem.proxy.packets.TradeInvitePacket;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.events.TradeRequestEvent;
import de.codingair.tradesystem.spigot.utils.Lang;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RequestManager {

    public static void request(@NotNull Player sender, @NotNull String receiver) {
        Player other = Bukkit.getPlayer(receiver);

        boolean proxy = other == null && TradeSystem.proxy().isOnline(receiver);

        if (proxy) {
            //proxy!
            if (RuleManager.isViolatingRules(sender)) return;

            String invited = TradeSystem.proxy().getCaseSensitive(receiver);

            //call request event
            TradeRequestEvent event = new TradeRequestEvent(sender, receiver, TradeSystem.man().getRequestExpirationTime());
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) return;

            if (InvitationManager.registerInvitation(sender, null, invited)) return;
            sendRequest(sender, invited);
        } else {
            request(sender, other);
        }
    }

    private static void sendRequest(Player sender, String invited) {
        TradeSystem.proxyHandler().send(new TradeInvitePacket(sender.getName(), invited, TradeSystem.proxy().getTradeHash()), sender)
                .whenComplete((result, t) -> {
                    if (t != null) t.printStackTrace();
                    else RuleManager.handle(sender, invited, result);
                });
    }

    public static void request(@NotNull Player sender, @Nullable Player other) {
        if (other != null && !other.isOnline()) return; //npc

        if (RuleManager.isViolatingRules(sender, other)) return;
        assert other != null; //already sent a message if other == null

        //call event
        TradeRequestEvent event = new TradeRequestEvent(sender, other, TradeSystem.man().getRequestExpirationTime());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        requestFinalTrade(sender, other);
    }

    private static void requestFinalTrade(@NotNull Player player, @NotNull Player recipient) {
        if (InvitationManager.registerInvitation(player, recipient, recipient.getName())) return;
        sendRequest(player.getName(), recipient);
    }

    @SuppressWarnings ("deprecation")
    public static void sendRequest(String player, Player recipient) {
        TextComponent base = new TextComponent(TextComponent.fromLegacyText(Lang.getPrefix() + Lang.get("Want_To_Trade", recipient).replace("%player%", player)));
        base.setColor(ChatColor.GRAY);

        String commandTrade = TradeSystem.getInstance().getCommandManager().getTradeAliases()[0];
        String commandAccept = "/" + commandTrade + " " + TradeSystem.getInstance().getCommandManager().getAcceptAliases()[0];
        String commandDeny = "/" + commandTrade + " " + TradeSystem.getInstance().getCommandManager().getDenyAliases()[0];

        SimpleMessage message = new SimpleMessage(recipient, base, TradeSystem.getInstance());

        TextComponent accept = new TextComponent(Lang.get("Want_To_Trade_Accept", recipient));
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, commandAccept + " " + player));
        accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new net.md_5.bungee.api.chat.BaseComponent[] {new TextComponent(Lang.get("Want_To_Trade_Hover", recipient))}));

        TextComponent deny = new TextComponent(Lang.get("Want_To_Trade_Deny", recipient));
        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, commandDeny + " " + player));
        deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new net.md_5.bungee.api.chat.BaseComponent[] {new TextComponent(Lang.get("Want_To_Trade_Hover", recipient))}));

        message.replace("%accept%", accept);
        message.replace("%deny%", deny);

        message.send();
        TradeSystem.getInstance().getTradeManager().playRequestSound(recipient);
    }
}
