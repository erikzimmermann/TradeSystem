package de.codingair.tradesystem.spigot.extras.external.essentials;

import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import de.codingair.tradesystem.spigot.trade.gui.layout.Pattern;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.basic.*;
import de.codingair.tradesystem.spigot.trade.gui.layout.utils.IconData;
import org.bukkit.inventory.ItemStack;

public class DefaultEssentialsPattern extends Pattern {
    public static final String NAME = "Standard-Essentials";
    private static final ItemStack BLACK_STAINED = new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE).getItem();
    private static final ItemStack GRAY_STAINED = new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE).getItem();
    private static final ItemStack NUGGET = new ItemBuilder(XMaterial.GOLD_NUGGET).getItem();
    private static final ItemStack BARRIER = new ItemBuilder(XMaterial.BARRIER).getItem();
    private static final ItemStack STATUS_NONE = new ItemBuilder(XMaterial.LIGHT_GRAY_TERRACOTTA).getItem();
    private static final ItemStack STATUS_READY = new ItemBuilder(XMaterial.LIME_TERRACOTTA).getItem();
    private static final ItemStack STATUS_NOT_READY = new ItemBuilder(XMaterial.RED_TERRACOTTA).getItem();

    static {
        BLACK_STAINED.setItemMeta(null);
        GRAY_STAINED.setItemMeta(null);
        NUGGET.setItemMeta(null);
        BARRIER.setItemMeta(null);
        STATUS_NONE.setItemMeta(null);
        STATUS_READY.setItemMeta(null);
        STATUS_NOT_READY.setItemMeta(null);
    }

    public DefaultEssentialsPattern() {
        super(NAME, new IconData[] {
                /*0*/   new IconData(DecorationIcon.class, BLACK_STAINED.clone()),
                new IconData(DecorationIcon.class, BLACK_STAINED.clone()),
                new IconData(DecorationIcon.class, BLACK_STAINED.clone()),
                new IconData(EssentialsIcon.class, NUGGET.clone()),
                new IconData(DecorationIcon.class, GRAY_STAINED.clone()),
                /*5*/   new IconData(ShowEssentialsIcon.class, NUGGET.clone()),
                new IconData(DecorationIcon.class, BLACK_STAINED.clone()),
                new IconData(DecorationIcon.class, BLACK_STAINED.clone()),
                new IconData(DecorationIcon.class, BLACK_STAINED.clone()),
                new IconData(TradeSlot.class),
                /*10*/  new IconData(TradeSlot.class),
                new IconData(DecorationIcon.class, BLACK_STAINED.clone()),
                new IconData(StatusIcon.class, STATUS_NONE.clone(), STATUS_NOT_READY.clone(), STATUS_READY.clone()),
                new IconData(DecorationIcon.class, GRAY_STAINED.clone()),
                new IconData(ShowStatusIcon.class, STATUS_NOT_READY.clone(), STATUS_READY.clone()),
                /*15*/  new IconData(DecorationIcon.class, BLACK_STAINED.clone()),
                new IconData(TradeSlotOther.class),
                new IconData(TradeSlotOther.class),
                new IconData(TradeSlot.class),
                new IconData(TradeSlot.class),
                /*20*/  new IconData(DecorationIcon.class, BLACK_STAINED.clone()),
                new IconData(DecorationIcon.class, BLACK_STAINED.clone()),
                new IconData(CancelIcon.class, BARRIER.clone()),
                new IconData(DecorationIcon.class, BLACK_STAINED.clone()),
                new IconData(DecorationIcon.class, BLACK_STAINED.clone()),
                /*25*/  new IconData(TradeSlotOther.class),
                new IconData(TradeSlotOther.class),
                new IconData(TradeSlot.class),
                new IconData(TradeSlot.class),
                new IconData(TradeSlot.class),
                /*30*/  new IconData(DecorationIcon.class, BLACK_STAINED.clone()),
                new IconData(DecorationIcon.class, BLACK_STAINED.clone()),
                new IconData(DecorationIcon.class, BLACK_STAINED.clone()),
                new IconData(TradeSlotOther.class),
                new IconData(TradeSlotOther.class),
                /*35*/  new IconData(TradeSlotOther.class),
                new IconData(TradeSlot.class),
                new IconData(TradeSlot.class),
                new IconData(TradeSlot.class),
                new IconData(TradeSlot.class),
                /*40*/  new IconData(DecorationIcon.class, GRAY_STAINED.clone()),
                new IconData(TradeSlotOther.class),
                new IconData(TradeSlotOther.class),
                new IconData(TradeSlotOther.class),
                new IconData(TradeSlotOther.class),
                /*45*/  new IconData(TradeSlot.class),
                new IconData(TradeSlot.class),
                new IconData(TradeSlot.class),
                new IconData(TradeSlot.class),
                new IconData(DecorationIcon.class, GRAY_STAINED.clone()),
                /*50*/  new IconData(TradeSlotOther.class),
                new IconData(TradeSlotOther.class),
                new IconData(TradeSlotOther.class),
                new IconData(TradeSlotOther.class),
        });
    }
}
