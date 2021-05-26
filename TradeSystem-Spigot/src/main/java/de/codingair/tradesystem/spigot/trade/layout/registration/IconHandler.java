package de.codingair.tradesystem.spigot.trade.layout.registration;

import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import de.codingair.tradesystem.spigot.trade.layout.registration.exceptions.IconAlreadyRegisteredException;
import de.codingair.tradesystem.spigot.trade.layout.registration.exceptions.IncompatibleTypesException;
import de.codingair.tradesystem.spigot.trade.layout.registration.exceptions.NoProperConstructorException;
import de.codingair.tradesystem.spigot.trade.layout.registration.exceptions.TradeIconException;
import de.codingair.tradesystem.spigot.trade.layout.types.MultiTradeIcon;
import de.codingair.tradesystem.spigot.trade.layout.types.TradeIcon;
import de.codingair.tradesystem.spigot.trade.layout.types.Transition;
import de.codingair.tradesystem.spigot.trade.layout.types.impl.basic.*;
import de.codingair.tradesystem.spigot.trade.layout.types.impl.economy.ExpLevelIcon;
import de.codingair.tradesystem.spigot.trade.layout.types.impl.economy.ShowExpLevelIcon;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class IconHandler {
    public static final Set<Class<? extends TradeIcon>> TRADE_ICONS = new HashSet<>();
    public static final LinkedHashMap<Class<? extends TradeIcon>, EditorInfo> ICON_DATA = new LinkedHashMap<>();

    static {
        try {
            //register standard
            register(DecorationIcon.class, new EditorInfo("Decoration", Type.BASIC, (editor) -> {
                if (editor.needsMoreDecorationItems()) return new ItemBuilder(XMaterial.MINECART);
                else return new ItemBuilder(XMaterial.CHEST_MINECART);
            }, true));
            register(StatusIcon.class, new MultiEditorInfo("Status icon", Type.BASIC, (editor) -> new ItemBuilder(XMaterial.LIGHT_GRAY_TERRACOTTA), true,
                    "Cannot ready", "Not ready", "Ready"));
            register(ShowStatusIcon.class, new MultiEditorInfo("Status preview icon", Type.BASIC, (editor) -> new ItemBuilder(XMaterial.LIME_TERRACOTTA), true,
                    "Not ready", "Ready"));
            register(TradeSlot.class, new EditorInfo("Own trade slots", Type.BASIC, (editor) -> new ItemBuilder(XMaterial.BLACK_STAINED_GLASS)
                    .setAmount(Math.max(1, getAmountOf(TradeSlot.class, editor.getIcons()))), true));
            register(TradeSlotOther.class, new EditorInfo("Foreign trade slots", Type.BASIC, (editor) -> new ItemBuilder(XMaterial.WHITE_STAINED_GLASS)
                    .setAmount(Math.max(1, getAmountOf(TradeSlotOther.class, editor.getIcons()))), true));
            register(CancelIcon.class, new EditorInfo("Cancel icon", Type.BASIC, (editor) -> new ItemBuilder(XMaterial.BARRIER), false));

            register(ExpLevelIcon.class, new EditorInfo("Exp level icon", Type.ECONOMY, (editor) -> new ItemBuilder(XMaterial.EXPERIENCE_BOTTLE), false));
            register(ShowExpLevelIcon.class, ExpLevelIcon.class, "Exp level preview icon");

        } catch (TradeIconException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    public static Class<? extends TradeIcon> getTransitionTarget(@NotNull Class<? extends TradeIcon> icon) {
        EditorInfo info = getInfo(icon);

        Class<? extends TradeIcon> target = info.getTransitionTarget();
        if (target == null) throw new IllegalStateException("Could not found a transition target for " + icon.getName());

        return target;
    }

    public static int getAmountOf(@NotNull Class<? extends TradeIcon> icon, @NotNull Map<Integer, Class<? extends TradeIcon>> icons) {
        return (int) icons.values().stream().filter(icon::equals).count();
    }

    public static List<EditorInfo> getIcons(@NotNull Type type) {
        List<EditorInfo> icons = new ArrayList<>();

        for (EditorInfo value : ICON_DATA.values()) {
            if (value.nameOnly()) continue;
            if (value.getType() == type) icons.add(value);
        }

        return icons;
    }

    public static List<EditorInfo> getNecessaryIcons() {
        List<EditorInfo> icons = new ArrayList<>();

        for (EditorInfo value : ICON_DATA.values()) {
            if (value.getTradeIcon().equals(DecorationIcon.class)
                    || TradeSlot.class.isAssignableFrom(value.getTradeIcon())) continue;
            if (value.isNecessary()) icons.add(value);
        }

        return icons;
    }

    /**
     * @param tradeIcon        The trade icon class to register.
     * @param transitionOrigin The other {@link TradeIcon} which may forward information to 'tradeIcon'.
     * @param name             The name of 'tradeIcon'.
     * @throws TradeIconException If the icon is not valid or 'transitionOrigin' is not registered.
     */
    public static void register(@NotNull Class<? extends TradeIcon> tradeIcon, @NotNull Class<? extends TradeIcon> transitionOrigin, String name) throws TradeIconException {
        //check icon first before adding additional information
        register(tradeIcon, new EditorInfo(name));

        getInfo(transitionOrigin).setTransitionTarget(tradeIcon);
    }

    /**
     * @param tradeIcon The trade icon class to register
     * @param data      The icon data of the registering icon. Null, if this icon type is a {@link de.codingair.tradesystem.spigot.trade.layout.types.Transition} and should be configured together with the corresponding icon.
     * @throws TradeIconException If the icon is not valid.
     */
    public static void register(@NotNull Class<? extends TradeIcon> tradeIcon, @NotNull EditorInfo data) throws TradeIconException {
        register(tradeIcon, data, false);
    }

    /**
     * @param tradeIcon The trade icon class to register
     * @param data      The icon data of the registering icon. Null, if this icon type is a {@link de.codingair.tradesystem.spigot.trade.layout.types.Transition} and should be configured together with the corresponding icon.
     * @param force     If {@link Boolean#TRUE}, the icon will not be checked whether it is valid or not. <b>Only use this if you know what you do!</b>
     * @throws TradeIconException If the icon is not valid.
     */
    public static void register(@NotNull Class<? extends TradeIcon> tradeIcon, @NotNull EditorInfo data, boolean force) throws TradeIconException {
        if (TRADE_ICONS.contains(tradeIcon)) throw new IconAlreadyRegisteredException(tradeIcon);

        try {
            if (!force) {
                if (MultiTradeIcon.class.isAssignableFrom(tradeIcon)) {
                    if (Transition.class.isAssignableFrom(tradeIcon))
                        //cannot handle both at the same time in the layout editor (IconPage.class)
                        throw new IncompatibleTypesException(tradeIcon, MultiTradeIcon.class, Transition.class);

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
            }

            //approved
            TRADE_ICONS.add(tradeIcon);
            data.setTradeIcon(tradeIcon);
            ICON_DATA.put(tradeIcon, data);
        } catch (NoSuchMethodException e) {
            throw new NoProperConstructorException(tradeIcon);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new NoProperConstructorException(tradeIcon, e);
        }
    }

    @NotNull
    public static EditorInfo getInfo(Class<? extends TradeIcon> icon) {
        EditorInfo info = ICON_DATA.get(icon);
        if (info == null) throw new IllegalStateException("IconInfo from " + icon.getName() + " could not be found.");
        return info;
    }
}
