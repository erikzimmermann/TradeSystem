package de.codingair.tradesystem.spigot.trade.gui.editor;

import de.codingair.codingapi.player.gui.inventory.v2.GUI;
import de.codingair.codingapi.player.gui.inventory.v2.Page;
import de.codingair.codingapi.player.gui.inventory.v2.buttons.Button;
import de.codingair.codingapi.player.gui.inventory.v2.buttons.Item;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.AlreadyClosedException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.PageAlreadyOpenedException;
import de.codingair.codingapi.server.sounds.Sound;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.gui.layout.Pattern;
import de.codingair.tradesystem.spigot.trade.gui.layout.registration.Type;
import de.codingair.tradesystem.spigot.utils.Lang;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class LayoutPage extends Page {
    private final Editor editor;

    public LayoutPage(Editor editor) {
        super(editor);
        this.editor = editor;
    }

    @Override
    public void buildItems() {
        ItemStack blackGlass = new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE).setHideName(true).getItem();
        ItemStack grayGlass = new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE).setHideName(true).getItem();

        addLine(0, 0, 0, 2, new Item(blackGlass));
        addLine(7, 0, 7, 2, new Item(blackGlass));
        addLine(1, 0, 6, 0, new Item(grayGlass));
        addButton(8, 1, new Item(grayGlass));

        //cancel icon
        addButton(8, 0, new Button() {
            @Override
            public @Nullable ItemStack buildItem() {
                ItemBuilder builder = new ItemBuilder();

                builder.setType(XMaterial.RED_TERRACOTTA);
                builder.setName("§c" + Lang.get("Cancel"));

                return builder.getItem();
            }

            @Override
            public boolean canClick(ClickType clickType) {
                return true;
            }

            @Override
            public void onClick(GUI gui, InventoryClickEvent inventoryClickEvent) {
                try {
                    Sound.ENTITY_ITEM_BREAK.playSound(gui.getPlayer(), 0.7F, 1);
                    gui.close();
                } catch (AlreadyClosedException e) {
                    e.printStackTrace();
                }
            }
        });

        //finish
        addButton(8, 2, new Button() {
            @Override
            public @Nullable ItemStack buildItem() {
                ItemBuilder builder = new ItemBuilder();

                if (editor.canFinish()) {
                    builder.setType(XMaterial.LIME_TERRACOTTA);
                    builder.setName("§a" + Lang.get("Finish"));
                } else {
                    builder.setType(XMaterial.LIGHT_GRAY_TERRACOTTA);
                    builder.setName("§7" + Lang.get("Finish"));
                    builder.addLore("", Lang.get("Not_Ready_For_Saving"));
                }

                return builder.getItem();
            }

            @Override
            public boolean canClick(ClickType clickType) {
                return editor.canFinish();
            }

            @Override
            public void onClick(GUI gui, InventoryClickEvent inventoryClickEvent) {
                try {
                    Pattern pattern = editor.buildPattern();
                    if (TradeSystem.getInstance().getLayoutManager().addPattern(pattern)) {
                        gui.getPlayer().sendMessage(Lang.getPrefix() + "§7" + Lang.get("Layout_Finished", gui.getPlayer()));
                    } else {
                        gui.getPlayer().sendMessage(Lang.getPrefix() + "§7" + Lang.get("Layout_Edited", gui.getPlayer()));
                    }

                    Sound.ENTITY_PLAYER_LEVELUP.playSound(gui.getPlayer(), 0.7F, 1);
                    gui.close();
                } catch (AlreadyClosedException e) {
                    e.printStackTrace();
                }
            }
        });

        //switch icons
        int slot = 1;
        for (Type value : Type.values()) {
            addButton(slot, new Button() {
                @Override
                public @Nullable ItemStack buildItem() {
                    ItemBuilder builder = value.getItem();
                    builder.setName(Editor.ITEM_TITLE_COLOR + value.getName());

                    if (isPageActive()) {
                        builder.addEnchantment(Enchantment.DAMAGE_ALL, 1);
                        builder.setHideEnchantments(true);
                    }

                    return builder.getItem();
                }

                private boolean isPageActive() {
                    Page p = gui.getActive();
                    if (p instanceof IconPage) {
                        IconPage iconPage = (IconPage) p;
                        return iconPage.getType() == value;
                    } else return false;
                }

                @Override
                public boolean canClick(ClickType clickType) {
                    return !isPageActive();
                }

                @Override
                public void onClick(GUI gui, InventoryClickEvent inventoryClickEvent) {
                    try {
                        Page page = ((Editor) gui).getPage(value);
                        gui.switchTo(page);
                        page.updateItems(true);
                    } catch (PageAlreadyOpenedException e) {
                        e.printStackTrace();
                    }
                }
            });

            slot++;
            if (slot == 7) break;
        }
    }
}
