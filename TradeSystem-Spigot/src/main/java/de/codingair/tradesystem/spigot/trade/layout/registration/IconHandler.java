package de.codingair.tradesystem.spigot.trade.layout.registration;

import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import de.codingair.tradesystem.spigot.trade.layout.registration.exceptions.*;
import de.codingair.tradesystem.spigot.trade.layout.types.MultiTradeIcon;
import de.codingair.tradesystem.spigot.trade.layout.types.TradeIcon;
import de.codingair.tradesystem.spigot.trade.layout.types.Transition;
import de.codingair.tradesystem.spigot.trade.layout.types.impl.basic.*;
import de.codingair.tradesystem.spigot.trade.layout.types.impl.economy.exp.ExpLevelIcon;
import de.codingair.tradesystem.spigot.trade.layout.types.impl.economy.exp.ExpPointIcon;
import de.codingair.tradesystem.spigot.trade.layout.types.impl.economy.exp.ShowExpLevelIcon;
import de.codingair.tradesystem.spigot.trade.layout.types.impl.economy.exp.ShowExpPointIcon;
import de.codingair.tradesystem.spigot.trade.layout.types.impl.economy.money.EssentialsIcon;
import de.codingair.tradesystem.spigot.trade.layout.types.impl.economy.money.ShowEssentialsIcon;
import de.codingair.tradesystem.spigot.trade.layout.types.impl.economy.money.ShowVaultIcon;
import de.codingair.tradesystem.spigot.trade.layout.types.impl.economy.money.VaultIcon;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class IconHandler {
    private static final HashMap<String, Class<? extends TradeIcon>> TRADE_ICONS = new HashMap<>();
    private static final LinkedHashMap<Class<? extends TradeIcon>, EditorInfo> ICON_DATA = new LinkedHashMap<>();

    static {
        try {
            //register basic
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

            //economy
            register(ExpLevelIcon.class, new EditorInfo("Exp level icon", Type.ECONOMY, (editor) -> new ItemBuilder(XMaterial.EXPERIENCE_BOTTLE), false));
            register(ShowExpLevelIcon.class, new TransitionTargetEditorInfo("Exp level preview icon", ExpLevelIcon.class));
            register(ExpPointIcon.class, new EditorInfo("Exp point icon", Type.ECONOMY, (editor) -> new ItemBuilder(XMaterial.EXPERIENCE_BOTTLE), false));
            register(ShowExpPointIcon.class, new TransitionTargetEditorInfo("Exp point preview icon", ExpPointIcon.class));

            register(VaultIcon.class, new EditorInfo("Vault icon", Type.ECONOMY, (editor) -> new ItemBuilder(XMaterial.GOLD_NUGGET), false, "Vault"));
            register(ShowVaultIcon.class, new TransitionTargetEditorInfo("Vault preview icon", VaultIcon.class));

            register(EssentialsIcon.class, new EditorInfo("Essentials icon", Type.ECONOMY, (editor) -> new ItemBuilder(XMaterial.GOLD_NUGGET), false, "Essentials"));
            register(ShowEssentialsIcon.class, new TransitionTargetEditorInfo("Essentials preview icon", EssentialsIcon.class));
        } catch (TradeIconException e) {
            e.printStackTrace();
        }
    }

    public static boolean isTypeEmpty(@NotNull Type type) {
        for (EditorInfo value : ICON_DATA.values()) {
            if (type.equals(value.getType())) return false;
        }

        return true;
    }

    @NotNull
    public static Class<? extends TradeIcon> getIcon(@NotNull String name) {
        Class<? extends TradeIcon> icon = TRADE_ICONS.get(name);
        if (icon == null) throw new NullPointerException("Cannot find TradeIcon with name='" + name + "'");

        return icon;
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
     * @param tradeIcon The trade icon class to register.
     * @param info      The icon info of the registering icon.
     * @throws TradeIconException If the icon is not valid or 'transitionOrigin' is not registered.
     */
    public static void register(@NotNull Class<? extends TradeIcon> tradeIcon, @NotNull TransitionTargetEditorInfo info) throws TradeIconException {
        //check icon first before adding additional information
        register(tradeIcon, (EditorInfo) info);

        getInfo(info.getOrigin()).setTransitionTarget(tradeIcon);
    }

    /**
     * @param tradeIcon The trade icon class to register
     * @param info      The icon info of the registering icon.
     * @throws TradeIconException If the icon is not valid.
     */
    public static void register(@NotNull Class<? extends TradeIcon> tradeIcon, @NotNull EditorInfo info) throws TradeIconException {
        register(tradeIcon, info, false);
    }

    /**
     * @param tradeIcon The trade icon class to register
     * @param data      The icon data of the registering icon. Null, if this icon type is a {@link de.codingair.tradesystem.spigot.trade.layout.types.Transition} and should be configured together with the corresponding icon.
     * @param force     If {@link Boolean#TRUE}, the icon will not be checked whether it is valid or not. <b>Only use this if you know what you do!</b>
     * @throws TradeIconException If the icon is not valid.
     */
    public static void register(@NotNull Class<? extends TradeIcon> tradeIcon, @NotNull EditorInfo data, boolean force) throws TradeIconException {
        if (TRADE_ICONS.containsKey(tradeIcon.getSimpleName())) throw new IconAlreadyRegisteredException(tradeIcon);

        try {
            if (!force) {
                if (!data.matchRequirements()) throw new RequirementNotFulfilledException(tradeIcon);

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
            TRADE_ICONS.put(tradeIcon.getSimpleName(), tradeIcon);
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
