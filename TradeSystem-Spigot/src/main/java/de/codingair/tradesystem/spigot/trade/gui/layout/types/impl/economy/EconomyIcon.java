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
import de.codingair.tradesystem.spigot.trade.gui.layout.utils.Perspective;
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
    public boolean isClickable(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer) {
        Player player = trade.getPlayer(perspective);
        if (player == null) return false;

        if (getBalance(player).signum() <= 0) {
            Lang.send(player, "Balance_limit_reached");
            return false;
        }

        return true;
    }

    @Override
    public @Nullable BigDecimal convertInput(@NotNull String input) {
        if (input.isEmpty()) return null;

        Number n = TradeSystem.handler().getMoneyPattern().parse(input, new ParsePosition(0));
        if (n == null) return null;

        // We forced the money pattern to parse BigDecimals only.
        // If we don't get a BigDecimal, there have been complications with the parsing.
        // We can then assume that either a NaN or an infinite number has been triggered.
        if (!(n instanceof BigDecimal)) return null;
        BigDecimal value = (BigDecimal) n;

        BigDecimal factor = TradeSystem.handler().getMoneyShortcutFactor(input);
        if (factor != null) value = value.multiply(factor);

        return value;
    }

    @Override
    public IconResult processInput(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer, @Nullable BigDecimal input, @NotNull String origin) {
        if (input == null || input.signum() == -1) {
            Lang.send(viewer, "Enter_Correct_Amount");
            return IconResult.GUI;
        }

        // make sure that we're only in a supported range of values
        input = getMaxSupportedValue().apply(input);

        // this might already be caught by converting the input to the max allowed value
        boolean isDecimal = input.remainder(BigDecimal.ONE).signum() != 0;
        if (!decimal && isDecimal) {
            Lang.send(viewer, "Enter_Correct_Amount");
            return IconResult.GUI;
        }

        Player player = trade.getPlayer(perspective);
        if (player == null) throw new NullPointerException("Player with perspective " + perspective + " is null");

        BigDecimal max = getBalance(player);
        if (input.compareTo(max) > 0) {
            Lang.send(viewer, "Only_X_Amount", new Lang.P("amount", makeString(trade, perspective, viewer, max, true)), new Lang.P("type", getName(viewer, max.equals(BigDecimal.ONE))));
            return IconResult.GUI;
        }

        this.value = checkLimit(trade, perspective, input);
        return IconResult.UPDATE;
    }

    @Override
    public @NotNull String makeString(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer, @Nullable BigDecimal current, boolean payment) {
        return makeFancyString(current, decimal);
    }

    static @NotNull String makeFancyString(@Nullable BigDecimal current, boolean decimal) {
        if (current == null) return "";

        Map.Entry<String, BigDecimal> shortcut = TradeSystem.handler().getApplicableMoneyShortcut(current);
        String appendix = "";
        if (shortcut != null) {
            current = current.divide(shortcut.getValue(), current.precision(), RoundingMode.FLOOR);
            appendix = shortcut.getKey().toUpperCase();
        }

        Number number = current;
        if (!decimal) number = number.intValue();
        return TradeSystem.handler().getMoneyPattern().format(number) + appendix;
    }

    @Override
    public @NotNull BigDecimal getValue() {
        return value;
    }

    /**
     * @param trade       The trade instance.
     * @param perspective The perspective of the player.
     * @return The value that is being displayed for the trade partner, therefore the value that is being offered for this player.
     */
    private @NotNull BigDecimal getShowValue(@NotNull Trade trade, @NotNull Perspective perspective) {
        T show = trade.getLayout()[perspective.id()].getIcon(getTargetClass());
        return show.getValue();
    }

    @Override
    public void setValue(BigDecimal value) {
        this.value = value;
    }

    @Override
    public BigDecimal getDefault() {
        return BigDecimal.ZERO;
    }

    @Override
    public @NotNull ItemBuilder prepareItemStack(@NotNull ItemBuilder layout, @NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer) {
        Player player = trade.getPlayer(perspective);
        if (player == null) throw new NullPointerException("Player with perspective " + perspective + " is null");

        layout.setName("§e" + getName(player, false) + ": §7" + makeString(trade, perspective, player, value, true));

        layout.addLore("", "§7» " + Lang.get("Click_To_Change", viewer));
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
    public @NotNull FinishResult tryFinish(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer, boolean initiationServer) {
        if (value.signum() > 0 && getBalance(trade, perspective).compareTo(value) < 0) {
            return FinishResult.ERROR_ECONOMY;
        }

        return FinishResult.PASS;
    }

    @Override
    public void onFinish(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer, boolean initiationServer) {
        Player player = trade.getPlayer(perspective);
        if (player == null) throw new NullPointerException("Player with perspective " + perspective + " is null");

        BigDecimal diff = getOverallDifference(trade, perspective);
        int sign = diff.signum();

        if (sign < 0) {
            withdraw(player, diff.negate());
        } else if (sign > 0) {
            deposit(player, diff);

            Player other = trade.getPlayer(perspective.flip());

            // call economy receive event for external logging purposes
            TradeReceiveEconomyEvent e = other != null ?
                    new TradeReceiveEconomyEvent(player, other, diff, nameSingular, namePlural) :
                    new TradeReceiveEconomyEvent(player, trade.getNames()[perspective.flip().id()], trade.getUniqueId(perspective.flip()), diff, nameSingular, namePlural);
            Bukkit.getPluginManager().callEvent(e);
        }

        // log absolute economy action
        BigDecimal value = getValue();
        if (value.signum() > 0) {
            // offering
            String fancyDiff = makeString(trade, perspective, player, value, true);
            log(trade, TradeLog.OFFERED_AMOUNT, player.getName(), namePlural, fancyDiff);
        }

        value = getShowValue(trade, perspective);
        if (value.signum() > 0) {
            // receiving
            String fancyDiff = makeString(trade, perspective, player, value, false);
            log(trade, TradeLog.RECEIVED_AMOUNT, player.getName(), namePlural, fancyDiff);
        }
    }

    /**
     * @param trade       The trade instance.
     * @param perspective The perspective of the player.
     * @return The computed difference comparing the offered amount of this player and the offered amount of the trade partner.
     */
    @NotNull
    public BigDecimal getOverallDifference(@NotNull Trade trade, @NotNull Perspective perspective) {
        return getShowValue(trade, perspective).subtract(value);
    }

    @NotNull
    private BigDecimal checkLimit(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull BigDecimal value) {
        Player other = trade.getPlayer(perspective.flip());
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

    protected @NotNull BigDecimal getBalance(@NotNull Trade trade, @NotNull Perspective perspective) {
        Player player = trade.getPlayer(perspective);
        if (player == null) throw new NullPointerException("Player with perspective " + perspective + " is null");
        return getBalance(player);
    }

    protected abstract @NotNull BigDecimal getBalance(@NotNull Player player);

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
