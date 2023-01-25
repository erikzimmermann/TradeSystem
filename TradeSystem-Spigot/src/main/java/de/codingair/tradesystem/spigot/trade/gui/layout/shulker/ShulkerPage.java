package de.codingair.tradesystem.spigot.trade.gui.layout.shulker;

import de.codingair.codingapi.player.gui.inventory.v2.GUI;
import de.codingair.codingapi.player.gui.inventory.v2.Page;
import de.codingair.codingapi.player.gui.inventory.v2.buttons.Button;
import de.codingair.codingapi.player.gui.inventory.v2.buttons.Item;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.AlreadyClosedException;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import de.codingair.tradesystem.spigot.utils.Head;
import de.codingair.tradesystem.spigot.utils.Lang;
import org.bukkit.block.ShulkerBox;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ShulkerPage extends Page {
    private ShulkerBox box;

    public ShulkerPage(ShulkerPeekGUI gui, ShulkerBox box) {
        super(gui);
        this.box = box;
    }

    @Override
    public void buildItems() {
        Inventory box = this.box.getInventory();
        for (int i = 0; i < box.getSize(); i++) {
            addButton(i, new Item(box.getItem(i)));
        }

        ItemStack item = new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE).setHideName(true).getItem();
        addLine(0, 3, 8, 3, new Item(item));
        addButton(4, 3, new Button() {
            @Override
            public @Nullable ItemStack buildItem() {
                return new ItemBuilder(Head.GRAY_ARROW_LEFT.getItem()).setName("§7" + Lang.get("Back")).getItem();
            }

            @Override
            public boolean canClick(ClickType clickType) {
                return true;
            }

            @Override
            public void onClick(GUI gui, InventoryClickEvent inventoryClickEvent) {
                try {
                    gui.close();
                } catch (AlreadyClosedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void update(ShulkerBox box) {
        this.box = box;
        rebuild();
    }
}
