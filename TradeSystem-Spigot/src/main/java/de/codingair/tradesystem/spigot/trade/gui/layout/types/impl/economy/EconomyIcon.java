package de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy;

import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.extras.tradelog.TradeLogMessages;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.TradeHandler;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.InputIcon;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.TradeIcon;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.Transition;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.feedback.FinishResult;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.feedback.IconResult;
import de.codingair.tradesystem.spigot.utils.Lang;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

public abstract class EconomyIcon<T extends Transition.Consumer<Double> & TradeIcon> extends InputIcon<Double> implements Transition<T, Double> {
    public static final int FRACTION_DIGITS = 4;
    private final String nameSingular;
    private final String namePlural;
    private final TradeLogMessages give;
    private final TradeLogMessages receive;
    private final boolean decimal;
    private double value = 0;

    /**
     * @param itemStack    The item that will be used to show this icon.
     * @param nameSingular The name (singular) - The plugin checks the language file first if a language tag can be found by the given name. Otherwise, the pure name will be used.
     * @param namePlural   The name (plural) - The plugin checks the language file first if a language tag can be found by the given name. Otherwise, the pure name will be used.
     * @param giveLog      The log type for trading. Can be null or {@link TradeLogMessages#NONE}.
     * @param receiveLog   The log type for receiving. Can be null or {@link TradeLogMessages#NONE}.
     * @param decimal      Whether decimals are allowed or not.
     */
    public EconomyIcon(@NotNull ItemStack itemStack, @NotNull String nameSingular, @NotNull String namePlural, @Nullable TradeLogMessages giveLog, @Nullable TradeLogMessages receiveLog, boolean decimal) {
        super(itemStack);
        this.nameSingular = nameSingular;
        this.namePlural = namePlural;
        this.give = giveLog;
        this.receive = receiveLog;
        this.decimal = decimal;
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
    public @Nullable Double convertInput(@NotNull String input) {
        if (input.isEmpty()) return 0D;

        Number n = TradeSystem.man().getMoneyPattern().parse(input, new ParsePosition(0));
        if (n == null) return 0D;
        return n.doubleValue();
    }

    @Override
    public IconResult processInput(@NotNull Trade trade, @NotNull Player player, @Nullable Double input, @NotNull String origin) {
        if (input == null || input < 0) {
            player.sendMessage(Lang.getPrefix() + Lang.get("Enter_Correct_Amount", player));
            return IconResult.GUI;
        } else {
            boolean isDecimal = input.intValue() != input;
            if (!decimal && isDecimal) {
                player.sendMessage(Lang.getPrefix() + Lang.get("Enter_Correct_Amount", player));
                return IconResult.GUI;
            } else {
                double max = getPlayerValue(player);
                if (input > max) {
                    String s = Lang.get("Only_X_Amount")
                            .replace("%amount%", makeString(max))
                            .replace("%type%", getName(player, max == 1));

                    player.sendMessage(Lang.getPrefix() + s);
                    return IconResult.GUI;
                }
            }
        }

        this.value = input;
        return IconResult.UPDATE;
    }

    @Override
    public @NotNull String makeString(@Nullable Double current) {
        if (current == null) return "";

        Number number = current;
        if (!decimal) number = number.intValue();
        return TradeSystem.man().getMoneyPattern().format(number);
    }

    @Override
    public @Nullable Double getValue() {
        return value;
    }

    @Override
    public @NotNull ItemBuilder prepareItemStack(@NotNull ItemBuilder layout, @NotNull Trade trade, @NotNull Player player, @Nullable Player other, @NotNull String othersName) {
        layout.setName("§e" + getName(player, false) + ": §7" + makeString(value));

        layout.addLore("", "§7» " + Lang.get("Click_To_Change", player));
        if (value > 0) layout.addEnchantment(Enchantment.DAMAGE_ALL, 1).setHideEnchantments(true);

        return layout;
    }

    @NotNull
    private String getName(@NotNull Player player, boolean singular) {
        try {
            return Lang.get(singular ? nameSingular : namePlural, player);
        } catch (NullPointerException ex) {
            return singular ? nameSingular : namePlural;
        }
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
        double diff = show.getValue() - value;

        NumberFormat format = NumberFormat.getNumberInstance(Locale.ENGLISH);
        String fancyDiff = format.format(decimal ? diff : (int) diff);

        if (diff < 0) {
            withdraw(player, -diff);
            log(trade, give, player.getName(), fancyDiff);
        } else if (diff > 0) {
            deposit(player, diff);
            log(trade, receive, player.getName(), fancyDiff);
        }
    }

    public abstract double getPlayerValue(Player player);

    public abstract void withdraw(Player player, double value);

    public abstract void deposit(Player player, double value);

    @Override
    public boolean isEmpty() {
        return this.value == 0;
    }

    @Override
    public void serialize(@NotNull DataOutputStream out) throws IOException {
        out.writeDouble(this.value);
    }

    @Override
    public void deserialize(@NotNull DataInputStream in) throws IOException {
        this.value = in.readDouble();
    }

    @Override
    public void inform(@NotNull T icon) {
        icon.applyTransition(this.value);
    }
}
