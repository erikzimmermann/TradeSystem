package de.codingair.tradesystem.spigot.trade.layout.types.impl.economy;

import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.extras.tradelog.TradeLogMessages;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.layout.types.InputIcon;
import de.codingair.tradesystem.spigot.trade.layout.types.TradeIcon;
import de.codingair.tradesystem.spigot.trade.layout.types.Transition;
import de.codingair.tradesystem.spigot.trade.layout.types.feedback.FinishResult;
import de.codingair.tradesystem.spigot.trade.layout.types.feedback.IconResult;
import de.codingair.tradesystem.spigot.utils.Lang;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

public abstract class EconomyIcon<T extends Transition.Consumer<Integer> & TradeIcon> extends InputIcon<Integer> implements Transition<T, Integer> {
    private final String nameSingular;
    private final String namePlural;
    private final TradeLogMessages give;
    private final TradeLogMessages receive;
    private int value = 0;

    public EconomyIcon(@NotNull ItemStack itemStack, @NotNull String nameSingular, @NotNull String namePlural, @NotNull TradeLogMessages give, @NotNull TradeLogMessages receive) {
        super(itemStack);
        this.nameSingular = nameSingular;
        this.namePlural = namePlural;
        this.give = give;
        this.receive = receive;
    }

    @Override
    public boolean isClickable(@NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName) {
        if (getPlayerValue(player) <= 0) {
            player.sendMessage(Lang.getPrefix() + Lang.get("Too_Little_Exp", player));
            return false;
        }

        return true;
    }

    @Override
    public @Nullable Integer convertInput(@NotNull String input) {
        if (input.isEmpty()) return 0;

        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @Override
    public IconResult processInput(@NotNull Trade trade, @NotNull Player player, @Nullable Integer input, @NotNull String origin) {
        //reopen
        if (input == null) return IconResult.GUI;

        if (input < 0) {
            player.sendMessage(Lang.getPrefix() + Lang.get("Enter_Correct_Amount", player));
        } else {
            int max = getPlayerValue(player);
            if (input > max) {
                String s = Lang.get("Only_X_Amount")
                        .replace("%amount%", TradeSystem.getInstance().getTradeManager().makeAmountFancy(max))
                        .replace("%type%", getName(player, max == 1));

                player.sendMessage(Lang.getPrefix() + s);
                return IconResult.GUI;
            }
        }

        this.value = input;
        return IconResult.UPDATE;
    }

    @Override
    public @NotNull String makeString(@Nullable Integer current) {
        if (current == null) return "";
        return TradeSystem.getInstance().getTradeManager().makeAmountFancy(current);
    }

    @Override
    public @Nullable Integer getValue() {
        return value;
    }

    @Override
    public @NotNull ItemBuilder prepareItemStack(@NotNull ItemBuilder layout, @NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName) {
        layout.setName("§e" + getName(player, false) + ": §7" + TradeSystem.getInstance().getTradeManager().makeAmountFancy(value));

        layout.addLore("", "§7» " + Lang.get("Click_To_Change", player));
        if (value > 0) layout.addEnchantment(Enchantment.DAMAGE_ALL, 1).setHideEnchantments(true);

        return layout;
    }

    @NotNull
    private String getName(@NotNull Player player, boolean singular) {
        return Lang.get(singular ? nameSingular : namePlural, player);
    }

    @Override
    public @NotNull FinishResult tryFinish(@NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName, boolean initiationServer) {
        if (getPlayerValue(player) < value) {
            return FinishResult.ERROR_ECONOMY;
        }

        return FinishResult.PASS;
    }

    @Override
    public void onFinish(@NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName, boolean initiationServer) {
        int id = trade.getId(player);
        T show = trade.getLayout()[id].getIcon(getTargetClass());
        int diff = show.getValue() - value;

        NumberFormat format = NumberFormat.getNumberInstance(Locale.ENGLISH);
        String fancyDiff = format.format(diff);

        if (diff < 0) {
            setPlayerValue(player, getPlayerValue(player) + diff);
            log(trade, give, player.getName(), fancyDiff);
        } else if (diff > 0) {
            setPlayerValue(player, getPlayerValue(player) + diff);
            log(trade, receive, player.getName(), fancyDiff);
        }
    }

    public abstract int getPlayerValue(Player player);

    public abstract void setPlayerValue(Player player, int value);

    @Override
    public boolean isEmpty() {
        return this.value == 0;
    }

    @Override
    public void serialize(@NotNull DataOutputStream out) throws IOException {
        out.writeInt(this.value);
    }

    @Override
    public void deserialize(@NotNull DataInputStream in) throws IOException {
        this.value = in.readInt();
    }

    @Override
    public void inform(@NotNull T icon) {
        icon.applyTransition(this.value);
    }
}
