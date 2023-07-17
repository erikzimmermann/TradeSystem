package de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy;

import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.events.TradeReceiveEconomyEvent;
import de.codingair.tradesystem.spigot.extras.external.TypeCap;
import de.codingair.tradesystem.spigot.extras.tradelog.TradeLog;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.InputIcon;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.TradeIcon;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.Transition;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.feedback.FinishResult;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.feedback.IconResult;
import de.codingair.tradesystem.spigot.utils.Lang;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParsePosition;
import java.util.Map;
import java.util.Optional;

public abstract class EconomyIcon<T extends Transition.Consumer<BigDecimal> & TradeIcon> extends InputIcon<BigDecimal> implements Transition<T, BigDecimal> {
    private final String nameSingular;
    private final String namePlural;
    private final boolean decimal;
    private BigDecimal value = BigDecimal.ZERO;

    /**
     * @param itemStack    The item that will be used to show this icon.
     * @param nameSingular The name (singular) - The plugin checks the language file first if a language tag can be found by the given name. Otherwise, the pure name will be used.
     * @param namePlural   The name (plural) - The plugin checks the language file first if a language tag can be found by the given name. Otherwise, the pure name will be used.
     * @param decimal      Whether decimals are allowed or not.
     */
    public EconomyIcon(@NotNull ItemStack itemStack, @NotNull String nameSingular, @NotNull String namePlural, boolean decimal) {
        super(itemStack);
        this.nameSingular = nameSingular;
        this.namePlural = namePlural;
        this.decimal = decimal;
    }

    @Override
    public boolean isClickable(@NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName) {
        if (getBalance(player).signum() <= 0) {
            player.sendMessage(Lang.getPrefix() + Lang.get("Balance_limit_reached", player));
            return false;
        }

        return true;
    }

    @Override
    public @Nullable BigDecimal convertInput(@NotNull String input) {
        if (input.isEmpty()) return BigDecimal.ZERO;

        BigDecimal value = (BigDecimal) TradeSystem.man().getMoneyPattern().parse(input, new ParsePosition(0));
        if (value == null) return BigDecimal.ZERO;

        BigDecimal factor = TradeSystem.man().getMoneyShortcutFactor(input);
        if (factor != null) value = value.multiply(factor);

        return value;
    }

    @Override
    public IconResult processInput(@NotNull Trade trade, @NotNull Player player, @Nullable BigDecimal input, @NotNull String origin) {
        if (input == null || input.signum() == -1) {
            player.sendMessage(Lang.getPrefix() + Lang.get("Enter_Correct_Amount", player));
            return IconResult.GUI;
        }

        // make sure that we're only in a supported range of values
        input = getMaxSupportedValue().apply(input);

        // this might already be caught by converting the input to the max allowed value
        boolean isDecimal = input.remainder(BigDecimal.ONE).signum() != 0;
        if (!decimal && isDecimal) {
            player.sendMessage(Lang.getPrefix() + Lang.get("Enter_Correct_Amount", player));
            return IconResult.GUI;
        }

        BigDecimal max = getBalance(player);
        if (input.compareTo(max) > 0) {
            String s = Lang.get("Only_X_Amount")
                    .replace("%amount%", makeString(player, max))
                    .replace("%type%", getName(player, max.equals(BigDecimal.ONE)));

            player.sendMessage(Lang.getPrefix() + s);
            return IconResult.GUI;
        }

        this.value = checkLimit(trade, player, input);
        return IconResult.UPDATE;
    }

    @Override
    public @NotNull String makeString(@NotNull Player player, @Nullable BigDecimal current) {
        return makeFancyString(current, decimal);
    }

    public static @NotNull String makeFancyString(@Nullable BigDecimal current, boolean decimal) {
        if (current == null) return "";

        Map.Entry<String, BigDecimal> shortcut = TradeSystem.man().getApplicableMoneyShortcut(current);
        String appendix = "";
        if (shortcut != null) {
            current = current.divide(shortcut.getValue(), current.precision(), RoundingMode.FLOOR);
            appendix = shortcut.getKey().toUpperCase();
        }

        Number number = current;
        if (!decimal) number = number.intValue();
        return TradeSystem.man().getMoneyPattern().format(number) + appendix;
    }

    @Override
    public @Nullable BigDecimal getValue() {
        return value;
    }

    @Override
    public @NotNull ItemBuilder prepareItemStack(@NotNull ItemBuilder layout, @NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName) {
        layout.setName("§e" + getName(player, false) + ": §7" + makeString(player, value));

        layout.addLore("", "§7» " + Lang.get("Click_To_Change", player));
        if (value.signum() > 0) layout.addEnchantment(Enchantment.DAMAGE_ALL, 1).setHideEnchantments(true);

        return layout;
    }

    @NotNull
    public String getName(@NotNull Player player, boolean singular) {
        try {
            return Lang.get(singular ? nameSingular : namePlural, player);
        } catch (NullPointerException ex) {
            return singular ? nameSingular : namePlural;
        }
    }

    @Override
    public @NotNull FinishResult tryFinish(@NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName, boolean initiationServer) {
        if (value.signum() > 0 && getBalance(player).compareTo(value) < 0) {
            return FinishResult.ERROR_ECONOMY;
        }

        return FinishResult.PASS;
    }

    @Override
    public void onFinish(@NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName, boolean initiationServer) {
        int id = trade.getId(player);
        BigDecimal diff = getOverallDifference(trade, id);

        String fancyDiff = TradeSystem.man().getMoneyPattern().format(decimal ? diff : diff.toBigInteger());

        int sign = diff.signum();
        if (sign < 0) {
            withdraw(player, diff.negate());
            log(trade, TradeLog.OFFERED_AMOUNT, player.getName(), namePlural, fancyDiff);
        } else if (sign > 0) {
            deposit(player, diff);

            // call economy receive event for external logging purposes
            TradeReceiveEconomyEvent e = other != null ?
                    new TradeReceiveEconomyEvent(player, other, diff, nameSingular, namePlural) :
                    new TradeReceiveEconomyEvent(player, othersName, trade.getUniqueId(othersName), diff, nameSingular, namePlural);
            Bukkit.getPluginManager().callEvent(e);

            log(trade, TradeLog.RECEIVED_AMOUNT, player.getName(), namePlural, fancyDiff);
        }
    }

    /**
     * @param trade The trade instance.
     * @param id    The id of the player.
     * @return The computed difference comparing the offered amount of this player and the offered amount of the trade partner.
     */
    @NotNull
    public BigDecimal getOverallDifference(@NotNull Trade trade, int id) {
        T show = trade.getLayout()[id].getIcon(getTargetClass());
        return show.getValue().subtract(value);
    }

    @NotNull
    private BigDecimal checkLimit(@NotNull Trade trade, @NotNull Player player, @NotNull BigDecimal value) {
        Player other = trade.getOther(player).orElse(null);
        if (other == null) return value;

        return getBalanceLimit(other).map(limit -> {
            BigDecimal balance = getBalance(other);
            if (balance.add(value).compareTo(limit) > 0) {
                return limit.subtract(balance).max(BigDecimal.ZERO);
            } else return value;
        }).orElse(value);
    }

    @NotNull
    protected Optional<BigDecimal> getBalanceLimit(@NotNull Player player) {
        return Optional.empty();
    }

    protected abstract @NotNull BigDecimal getBalance(Player player);

    protected abstract void withdraw(Player player, @NotNull BigDecimal value);

    protected abstract void deposit(Player player, @NotNull BigDecimal value);

    protected abstract @NotNull TypeCap getMaxSupportedValue();

    @Override
    public boolean isEmpty() {
        return this.value.signum() == 0;
    }

    @Override
    public void serialize(@NotNull DataOutputStream out) throws IOException {
        out.writeUTF(this.value.toString());
    }

    @Override
    public void deserialize(@NotNull DataInputStream in) throws IOException {
        this.value = new BigDecimal(in.readUTF());
    }

    @Override
    public void inform(@NotNull T icon) {
        icon.applyTransition(this.value);
    }

    public boolean isDecimal() {
        return decimal;
    }
}
