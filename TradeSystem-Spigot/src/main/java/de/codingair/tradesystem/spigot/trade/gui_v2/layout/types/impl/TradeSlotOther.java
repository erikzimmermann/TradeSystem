package de.codingair.tradesystem.spigot.trade.gui_v2.layout.types.impl;

import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import de.codingair.tradesystem.spigot.trade.gui_v2.layout.TradeLayout;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class TradeSlotOther extends TradeSlot {
    @Override
    public @NotNull ItemStack getEditorIcon(@NotNull TradeLayout layout, @NotNull Player player) {
        int amount = 25 - layout.getPattern().getAmountOf(TradeSlotOther.class);

        return new ItemBuilder(XMaterial.WHITE_STAINED_GLASS_PANE)
                .setAmount(amount)
                .addEnchantment(Enchantment.DAMAGE_ALL, 1)
                .setHideName(true)
                .setHideEnchantments(true)
                .getItem();
    }
}
