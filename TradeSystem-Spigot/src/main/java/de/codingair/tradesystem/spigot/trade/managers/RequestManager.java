package de.codingair.tradesystem.spigot.trade.managers;

import de.codingair.codingapi.player.chat.SimpleMessage;
import de.codingair.tradesystem.proxy.packets.TradeInvitePacket;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.utils.Lang;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class RequestManager {

    public static void request(Player sender, String receiver) {
        Player other = Bukkit.getPlayer(receiver);

        if (other == null && TradeSystem.proxy().isOnline(receiver)) {
            //proxy!
            if (RuleManager.isViolatingRules(sender)) return;

            String invited = TradeSystem.proxy().getCaseSensitive(receiver);

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

    public static void request(Player p, Player other) {
        if (!other.isOnline()) return; //npc

        if (RuleManager.isViolatingRules(p, other)) return;
        requestFinalTrade(p, other);
    }

    private static void requestFinalTrade(Player player, Player recipient) {
        if (InvitationManager.registerInvitation(player, recipient, recipient.getName())) return;
        sendRequest(player.getName(), recipient);
    }

    @SuppressWarnings ("deprecation")
    public static void sendRequest(String player, Player recipient) {
        TextComponent base = new TextComponent(TextComponent.fromLegacyText(Lang.getPrefix() + Lang.get("Want_To_Trade", recipient).replace("%player%", player)));
        base.setColor(ChatColor.GRAY);

        SimpleMessage message = new SimpleMessage(recipient, base, TradeSystem.getInstance());

        TextComponent accept = new TextComponent(Lang.get("Want_To_Trade_Accept", recipient));
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trade accept " + player));
        accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new net.md_5.bungee.api.chat.BaseComponent[] {new TextComponent(Lang.get("Want_To_Trade_Hover", recipient))}));

        TextComponent deny = new TextComponent(Lang.get("Want_To_Trade_Deny", recipient));
        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trade deny " + player));
        deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new net.md_5.bungee.api.chat.BaseComponent[] {new TextComponent(Lang.get("Want_To_Trade_Hover", recipient))}));

        message.replace("%accept%", accept);
        message.replace("%deny%", deny);

        message.send();
        TradeSystem.getInstance().getTradeManager().playRequestSound(recipient);
    }
}
