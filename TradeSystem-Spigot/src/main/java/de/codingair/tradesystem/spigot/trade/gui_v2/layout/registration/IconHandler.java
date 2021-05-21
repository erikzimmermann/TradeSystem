package de.codingair.tradesystem.spigot.trade.gui_v2.layout.registration;

import de.codingair.tradesystem.spigot.trade.gui_v2.layout.registration.exceptions.IconAlreadyRegisteredException;
import de.codingair.tradesystem.spigot.trade.gui_v2.layout.registration.exceptions.NoProperConstructorException;
import de.codingair.tradesystem.spigot.trade.gui_v2.layout.registration.exceptions.TradeIconException;
import de.codingair.tradesystem.spigot.trade.gui_v2.layout.types.MultiTradeIcon;
import de.codingair.tradesystem.spigot.trade.gui_v2.layout.types.TradeIcon;
import de.codingair.tradesystem.spigot.trade.gui_v2.layout.types.impl.*;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

public class IconHandler {
    private static final Set<Class<? extends TradeIcon>> TRADE_ICONS = new HashSet<>();

    static {
        try {
            //register standard
            register(CancelIcon.class);
            register(ExpIcon.class);
            register(ShowExpIcon.class);
            register(ShowStatusIcon.class);
            register(StatusIcon.class);
            register(TradeSlot.class);
            register(TradeSlotOther.class);
        } catch (TradeIconException e) {
            e.printStackTrace();
        }
    }

    public static void register(@NotNull Class<? extends TradeIcon> tradeIcon) throws TradeIconException {
        if (TRADE_ICONS.contains(tradeIcon)) throw new IconAlreadyRegisteredException(tradeIcon);

        try {

            if (MultiTradeIcon.class.isAssignableFrom(tradeIcon)) {
                tradeIcon.getConstructor(ItemStack[].class);
            } else {
                try {
                    tradeIcon.getConstructor()
                            .newInstance();
                } catch (NoSuchMethodException e) {
                    tradeIcon.getConstructor(ItemStack.class)
                            .newInstance(new ItemStack(Material.STONE));
                }
            }

            //approved
            TRADE_ICONS.add(tradeIcon);
        } catch (NoSuchMethodException e) {
            throw new NoProperConstructorException(tradeIcon);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new NoProperConstructorException(tradeIcon, e);
        }
    }
}
