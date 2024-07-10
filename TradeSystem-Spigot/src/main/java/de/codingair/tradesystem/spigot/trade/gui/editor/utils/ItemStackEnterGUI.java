package de.codingair.tradesystem.spigot.trade.gui.editor.utils;

import com.github.Anon8281.universalScheduler.UniversalScheduler;
import de.codingair.codingapi.player.gui.inventory.v2.GUI;
import de.codingair.codingapi.player.gui.inventory.v2.Page;
import de.codingair.codingapi.player.gui.inventory.v2.buttons.Button;
import de.codingair.codingapi.player.gui.inventory.v2.buttons.Item;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.AlreadyClosedException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.AlreadyOpenedException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.IsWaitingException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.NoPageException;
import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.utils.Lang;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemStackEnterGUI extends GUI {
    private static final int INPUT_SLOT = 13;
    private static final int ACCEPT_SLOT = 16;
    private final Callback<ItemStack> callback;
    private final ItemStack previous;
    private Listener listener;
    private boolean cancelling = false;

    public ItemStackEnterGUI(Player player, String title, @Nullable ItemStack previous, @NotNull Callback<ItemStack> callback) {
        super(player, TradeSystem.getInstance(), 27, title);

        this.callback = callback;
        this.previous = previous;

        registerPage(new DefaultPage(this), true);
    }

    @Override
    public void open() throws AlreadyOpenedException, NoPageException, IsWaitingException {
        super.open();

        Bukkit.getPluginManager().registerEvents(listener = new Listener() {
            @EventHandler (priority = EventPriority.HIGH)
            public void onClose(InventoryClickEvent e) {
                if (player.equals(e.getWhoClicked())) {
                    if (e.getSlot() == INPUT_SLOT || e.getView().getBottomInventory().equals(e.getClickedInventory())) {
                        e.setCancelled(false);
                        UniversalScheduler.getScheduler(TradeSystem.getInstance()).runTaskLater(() -> getActive().updateItem(ACCEPT_SLOT), 1);
                    }
                }
            }
        }, TradeSystem.getInstance());
    }

    @Override
    public void close() throws AlreadyClosedException {
        if (this.listener != null) {
            HandlerList.unregisterAll(this.listener);
            this.listener = null;
        }

        if (!cancelling) callback.accept(getItem(INPUT_SLOT));

        super.close();
    }

    private class DefaultPage extends Page {
        public DefaultPage(GUI gui) {
            super(gui);
        }

        @Override
        public void buildItems() {
            ItemStack blackGlass = new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE).setHideName(true).getItem();
            ItemStack grayGlass = new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE).setHideName(true).getItem();

            addButton(1, 1, new Button() {
                @Override
                public @Nullable ItemStack buildItem() {
                    ItemBuilder builder = new ItemBuilder();

                    builder.setType(XMaterial.BARRIER);
                    builder.setName("§c" + Lang.get("Back"));

                    return builder.getItem();
                }

                @Override
                public boolean canClick(ClickType clickType) {
                    return true;
                }

                @Override
                public void onClick(GUI gui, InventoryClickEvent inventoryClickEvent) {
                    try {
                        cancelling = true;
                        close();
                    } catch (AlreadyClosedException e) {
                        e.printStackTrace();
                    }
                }
            });

            addButton(ACCEPT_SLOT, new Button() {
                @Override
                public @Nullable ItemStack buildItem() {
                    ItemBuilder builder = new ItemBuilder();

                    if (hasItem()) {
                        builder.setType(XMaterial.LIME_TERRACOTTA);
                        builder.setName("§a" + Lang.get("Set"));
                    } else {
                        builder.setType(XMaterial.LIGHT_GRAY_TERRACOTTA);
                        builder.setName("§7" + Lang.get("Set"));
                    }

                    return builder.getItem();
                }

                @Override
                public boolean canClick(ClickType clickType) {
                    return hasItem();
                }

                private boolean hasItem() {
                    return gui.getItem(4, 1) != null;
                }

                @Override
                public void onClick(GUI gui, InventoryClickEvent inventoryClickEvent) {
                    try {
                        close();
                    } catch (AlreadyClosedException e) {
                        e.printStackTrace();
                    }
                }
            });

            addButton(3, 1, new Item(grayGlass));
            addButton(5, 1, new Item(grayGlass));
            for (int i = 0; i < 3; i++) {
                addLine(0, i, 8, i, new Item(blackGlass));
            }

            removeButtonAt(INPUT_SLOT);
            gui.setItem(INPUT_SLOT, previous);
        }
    }
}
