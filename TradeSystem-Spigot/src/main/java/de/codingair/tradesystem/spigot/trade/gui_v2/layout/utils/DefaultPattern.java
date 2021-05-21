package de.codingair.tradesystem.spigot.trade.gui_v2.layout.utils;

import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import de.codingair.tradesystem.spigot.trade.gui_v2.layout.Pattern;
import de.codingair.tradesystem.spigot.trade.gui_v2.layout.types.impl.*;
import de.codingair.tradesystem.spigot.trade.gui_v2.layout.types.utils.DecorationIcon;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class DefaultPattern extends Pattern {
    private static final ItemStack BLACK_STAINED = new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE).getItem();
    private static final ItemStack GRAY_STAINED = new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE).getItem();
    private static final ItemStack NUGGET = new ItemBuilder(XMaterial.GOLD_NUGGET).getItem();
    private static final ItemStack BARRIER = new ItemBuilder(Material.BARRIER).getItem();
    private static final ItemStack STATUS_NONE = new ItemBuilder(XMaterial.LIGHT_GRAY_TERRACOTTA).getItem();
    private static final ItemStack STATUS_READY = new ItemBuilder(XMaterial.LIME_TERRACOTTA).getItem();
    private static final ItemStack STATUS_NOT_READY = new ItemBuilder(XMaterial.RED_TERRACOTTA).getItem();

    public DefaultPattern() {
        super("Standard", new IconData[] {
        /*0*/   new IconData(DecorationIcon.class, BLACK_STAINED),
                new IconData(DecorationIcon.class, BLACK_STAINED),
                new IconData(DecorationIcon.class, BLACK_STAINED),
                new IconData(ExpIcon.class, NUGGET),
                new IconData(DecorationIcon.class, GRAY_STAINED),
        /*5*/   new IconData(ShowExpIcon.class, NUGGET),
                new IconData(DecorationIcon.class, BLACK_STAINED),
                new IconData(DecorationIcon.class, BLACK_STAINED),
                new IconData(DecorationIcon.class, BLACK_STAINED),
                new IconData(TradeSlot.class, (ItemStack[]) null),
        /*10*/  new IconData(TradeSlot.class, (ItemStack[]) null),
                new IconData(DecorationIcon.class, BLACK_STAINED),
                new IconData(StatusIcon.class, STATUS_NONE, STATUS_NOT_READY, STATUS_READY),
                new IconData(DecorationIcon.class, GRAY_STAINED),
                new IconData(ShowStatusIcon.class, STATUS_NOT_READY, STATUS_READY),
        /*15*/  new IconData(DecorationIcon.class, BLACK_STAINED),
                new IconData(TradeSlotOther.class, (ItemStack[]) null),
                new IconData(TradeSlotOther.class, (ItemStack[]) null),
                new IconData(TradeSlot.class, (ItemStack[]) null),
                new IconData(TradeSlot.class, (ItemStack[]) null),
        /*20*/  new IconData(DecorationIcon.class, BLACK_STAINED),
                new IconData(DecorationIcon.class, BLACK_STAINED),
                new IconData(CancelIcon.class, BARRIER),
                new IconData(DecorationIcon.class, BLACK_STAINED),
                new IconData(DecorationIcon.class, BLACK_STAINED),
        /*25*/  new IconData(TradeSlotOther.class, (ItemStack[]) null),
                new IconData(TradeSlotOther.class, (ItemStack[]) null),
                new IconData(TradeSlot.class, (ItemStack[]) null),
                new IconData(TradeSlot.class, (ItemStack[]) null),
                new IconData(TradeSlot.class, (ItemStack[]) null),
        /*30*/  new IconData(DecorationIcon.class, BLACK_STAINED),
                new IconData(DecorationIcon.class, BLACK_STAINED),
                new IconData(DecorationIcon.class, BLACK_STAINED),
                new IconData(TradeSlotOther.class, (ItemStack[]) null),
                new IconData(TradeSlotOther.class, (ItemStack[]) null),
        /*35*/  new IconData(TradeSlotOther.class, (ItemStack[]) null),
                new IconData(TradeSlot.class, (ItemStack[]) null),
                new IconData(TradeSlot.class, (ItemStack[]) null),
                new IconData(TradeSlot.class, (ItemStack[]) null),
                new IconData(TradeSlot.class, (ItemStack[]) null),
        /*40*/  new IconData(DecorationIcon.class, GRAY_STAINED),
                new IconData(TradeSlotOther.class, (ItemStack[]) null),
                new IconData(TradeSlotOther.class, (ItemStack[]) null),
                new IconData(TradeSlotOther.class, (ItemStack[]) null),
                new IconData(TradeSlotOther.class, (ItemStack[]) null),
        /*45*/  new IconData(TradeSlot.class, (ItemStack[]) null),
                new IconData(TradeSlot.class, (ItemStack[]) null),
                new IconData(TradeSlot.class, (ItemStack[]) null),
                new IconData(TradeSlot.class, (ItemStack[]) null),
                new IconData(DecorationIcon.class, GRAY_STAINED),
        /*50*/  new IconData(TradeSlotOther.class, (ItemStack[]) null),
                new IconData(TradeSlotOther.class, (ItemStack[]) null),
                new IconData(TradeSlotOther.class, (ItemStack[]) null),
                new IconData(TradeSlotOther.class, (ItemStack[]) null),
        });
    }
}
