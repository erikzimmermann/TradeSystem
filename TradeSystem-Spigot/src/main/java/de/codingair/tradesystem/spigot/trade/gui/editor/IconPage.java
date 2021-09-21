package de.codingair.tradesystem.spigot.trade.gui.editor;

import de.codingair.codingapi.player.gui.inventory.v2.GUI;
import de.codingair.codingapi.player.gui.inventory.v2.Page;
import de.codingair.codingapi.player.gui.inventory.v2.buttons.Button;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.AlreadyOpenedException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.IsWaitingException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.NoPageException;
import de.codingair.codingapi.tools.Call;
import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.gui.editor.utils.InventoryEditorButton;
import de.codingair.tradesystem.spigot.trade.gui.editor.utils.ItemStackEnterGUI;
import de.codingair.tradesystem.spigot.trade.gui.layout.registration.EditorInfo;
import de.codingair.tradesystem.spigot.trade.gui.layout.registration.IconHandler;
import de.codingair.tradesystem.spigot.trade.gui.layout.registration.MultiEditorInfo;
import de.codingair.tradesystem.spigot.trade.gui.layout.registration.Type;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.MultiTradeIcon;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.Transition;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.basic.DecorationIcon;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.basic.TradeSlot;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.basic.TradeSlotOther;
import de.codingair.tradesystem.spigot.utils.Lang;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IconPage extends Page {
    protected final List<EditorInfo> icons;
    private final Type type;
    protected Editor editor;

    public IconPage(Editor editor, Type type, Page basic) {
        super(editor, basic);
        this.editor = editor;
        this.type = type;
        this.icons = IconHandler.getIcons(this.type);
    }

    @Override
    public void buildItems() {
        int slot = 1;
        for (EditorInfo icon : this.icons) {
            addButton(slot, 2, buildButton(slot + 18, icon));
            if (++slot == 7) break;
        }
    }

    private Button buildButton(int slot, EditorInfo editorInfo) {
        return new InventoryEditorButton() {
            int variantId = 0; //id of 0 allows to open the select inventory GUI
            boolean resetting = false; //confirm flag

            @Override
            public @Nullable ItemStack buildItem() {
                boolean alreadySet = isLayoutInventoryNotEmpty() && editor.getIcons().containsValue(editorInfo.getTradeIcon());

                ItemBuilder builder = editorInfo.getEditorIcon(editor);

                boolean addMarker = false; //add enchanting effect
                boolean done = alreadySet;

                if (done && TradeSlot.class.isAssignableFrom(editorInfo.getTradeIcon())) {
                    done = editor.getAmountOf(TradeSlot.class) == editor.getAmountOf(TradeSlotOther.class);
                }

                //do we need more decoration items?
                if (editorInfo.getTradeIcon().equals(DecorationIcon.class)) {
                    if (editor.needsMoreDecorationItems()) {
                        addMarker = true;
                    } else {
                        done = true;
                    }
                } else {
                    if (editorInfo.isNecessary()) {
                        //do we have all necessary items?
                        if (!alreadySet && !editor.needsMoreDecorationItems()) {
                            addMarker = true;
                        }
                    }

                    //if we already selected one item to this icon
                    if (!addMarker && alreadySet) {
                        if (MultiTradeIcon.class.isAssignableFrom(editorInfo.getTradeIcon()) && editorInfo instanceof MultiEditorInfo) {
                            //did we configure all variants?

                            MultiEditorInfo multiEditorInfo = (MultiEditorInfo) editorInfo;
                            ItemStack[] variants = editor.getVariants(editorInfo.getTradeIcon(), null);

                            int configured = 0;
                            if (variants != null) {
                                for (ItemStack i : variants) {
                                    if (i != null) configured++;
                                }
                            }

                            boolean variantsNotDone = configured != multiEditorInfo.getIconName().length;
                            if (variantsNotDone) addMarker = true;
                            done = !variantsNotDone;
                        }

                        //something that needs another item
                        if (Transition.class.isAssignableFrom(editorInfo.getTradeIcon()) && editorInfo.getTransitionTarget() != null) {
                            //did we configure the transition target?

                            boolean transitionTargetNotSet = !editor.getIcons().containsValue(editorInfo.getTransitionTarget());
                            if (transitionTargetNotSet && !editor.needsMoreDecorationItems()) {
                                addMarker = true;
                            }

                            done = !transitionTargetNotSet;
                        }
                    }
                }

                if (addMarker) {
                    builder.addEnchantment(Enchantment.DAMAGE_ALL, 1);
                    builder.setHideEnchantments(true);
                }

                builder.setName(Editor.ITEM_TITLE_COLOR + editorInfo.getName() + (done ? "§r §a✔" : ""));
                builder.addLore("");

                //add click description
                if (editorInfo.getTradeIcon().equals(DecorationIcon.class)) {
                    builder.addLore(Editor.ITEM_SUB_TITLE_COLOR + Lang.get("Leftclick") + ": " + (alreadySet ? "§7" : "§a") + Lang.get("Edit"));
                } else if (TradeSlot.class.isAssignableFrom(editorInfo.getTradeIcon())) {
                    int tradeSlots = editor.getAmountOf(TradeSlot.class);
                    int otherTradeSlots = editor.getAmountOf(TradeSlotOther.class);

                    if (tradeSlots != otherTradeSlots) {
                        builder.removeLore();
                        builder.addText("§7" + Lang.get("Layout_Hint_Same_Slot_Amount", gui.getPlayer()), 150);
                        builder.addLore("");
                    }

                    builder.addLore(Editor.ITEM_SUB_TITLE_COLOR + Lang.get("Leftclick") + ": " + (alreadySet ? "§7" : "§a") + Lang.get("Edit"));
                } else {
                    builder.addLore(Editor.ITEM_SUB_TITLE_COLOR + Lang.get("Leftclick") + ": " + (alreadySet || editor.hasNoUsableItems() ? "§7" : "§a") + Lang.get("Select"));
                }

                if (alreadySet) {
                    //icon cannot be a transition AND a multi trade icon at the same time
                    if (MultiTradeIcon.class.isAssignableFrom(editorInfo.getTradeIcon()) && editorInfo instanceof MultiEditorInfo) {
                        MultiEditorInfo multiEditorInfo = (MultiEditorInfo) editorInfo;
                        ItemStack[] variants = editor.getVariants(editorInfo.getTradeIcon(), null);

                        boolean variantSet = variants != null && variants[variantId] != null;
                        int configured = 0;
                        if (variants != null) {
                            for (ItemStack i : variants) {
                                if (i != null) configured++;
                            }
                        }
                        String statusColor = configured == multiEditorInfo.getIconName().length ? "§a" : "§c";

                        builder.removeLore();
                        builder.addLore("");
                        builder.addLore(Editor.ITEM_SUB_TITLE_COLOR + Lang.get("Variant") + ": " + (variantSet ? "§a" : "§c") + (variantId + 1) + ". " + multiEditorInfo.getIconName()[variantId] + " §8(" + statusColor + configured + "§8/" + statusColor + multiEditorInfo.getIconName().length + "§8)");
                        builder.addLore(Editor.ITEM_SUB_TITLE_COLOR + Lang.get("Leftclick") + ": §7" + (variantId == 0 ? Lang.get("Select") : Lang.get("Edit_Variant")));
                        builder.addLore("§8" + Lang.get("Shift_Leftclick") + ": §8→");
                    } else {
                        if (Transition.class.isAssignableFrom(editorInfo.getTradeIcon())) {
                            boolean hasTarget = editor.getIcons().containsValue(editorInfo.getTransitionTarget());

                            builder.addLore(Editor.ITEM_SUB_TITLE_COLOR + Lang.get("Rightclick") + ": " + (hasTarget || editor.hasNoUsableItems() ? "§7" : "§a") + Lang.get("Select_Foreign_Preview"));
                        }
                    }

                    if (!TradeSlot.class.isAssignableFrom(editorInfo.getTradeIcon())) {
                        //trade slots should not be able to be reset
                        builder.addLore("§8" + Lang.get("Shift_Rightclick") + ": " + (resetting ? "§c" : "§8") + Lang.get("Reset") + (resetting ? "?" : ""));
                    }
                }

                return builder.getItem();
            }

            @Override
            public boolean canClick(ClickType clickType) {
                boolean alreadySet = isLayoutInventoryNotEmpty() && editor.getIcons().containsValue(editorInfo.getTradeIcon());

                if (editorInfo.getTradeIcon().equals(DecorationIcon.class)) return clickType == ClickType.LEFT;
                else if (TradeSlot.class.isAssignableFrom(editorInfo.getTradeIcon())) return clickType == ClickType.LEFT;
                else if (alreadySet) {
                    if (clickType == ClickType.SHIFT_RIGHT) return true; //reset
                    else if (MultiTradeIcon.class.isAssignableFrom(editorInfo.getTradeIcon()) && editorInfo instanceof MultiEditorInfo) return clickType != ClickType.RIGHT;
                    else if (Transition.class.isAssignableFrom(editorInfo.getTradeIcon())) {
                        boolean hasTarget = editor.getIcons().containsValue(editorInfo.getTransitionTarget());

                        if (clickType == ClickType.RIGHT) {
                            return !hasTarget && !editor.hasNoUsableItems();
                        }
                    }
                }

                return clickType == ClickType.LEFT && !editor.hasNoUsableItems();
            }

            @Override
            public void onClick(GUI gui, InventoryClickEvent e) {
                boolean alreadySet = isLayoutInventoryNotEmpty() && editor.getIcons().containsValue(editorInfo.getTradeIcon());

                if (alreadySet) {
                    if (MultiTradeIcon.class.isAssignableFrom(editorInfo.getTradeIcon()) && editorInfo instanceof MultiEditorInfo) {
                        if (e.getClick() == ClickType.SHIFT_LEFT) {
                            variantId++;
                            MultiEditorInfo multiEditorInfo = (MultiEditorInfo) editorInfo;
                            if (variantId == multiEditorInfo.getIconName().length) variantId = 0;
                            updateItem(slot);
                        }
                    }

                    if (e.getClick() == ClickType.SHIFT_RIGHT && !TradeSlot.class.isAssignableFrom(editorInfo.getTradeIcon())) {
                        //reset
                        if (resetting) {
                            editor.getIcons().entrySet().removeIf(entry -> entry.getValue().equals(editorInfo.getTradeIcon()));

                            //keep variants (as backup) & set id back to 0
                            variantId = 0;

                            resetting = false;
                            updateItem(slot);
                        } else {
                            resetting = true;
                            updateItem(slot);

                            Bukkit.getScheduler().runTaskLater(TradeSystem.getInstance(), () -> {
                                resetting = false;
                                updateItem(slot);
                            }, 10L);
                        }
                    }
                }
            }

            @Override
            public boolean canSwitch(ClickType clickType) {
                if (editorInfo.getTradeIcon().equals(DecorationIcon.class)) return clickType == ClickType.LEFT;
                else if (TradeSlot.class.isAssignableFrom(editorInfo.getTradeIcon())) return clickType == ClickType.LEFT;
                else if (clickType == ClickType.SHIFT_RIGHT) return false;
                else if (MultiTradeIcon.class.isAssignableFrom(editorInfo.getTradeIcon()) && editorInfo instanceof MultiEditorInfo)
                    return clickType == ClickType.LEFT;

                return !editor.hasNoUsableItems();
            }

            @Override
            public boolean open(ClickType clickType, GUI gui, Call call) {
                if (editorInfo.getTradeIcon().equals(DecorationIcon.class))
                    //special cases
                    editor.openLayoutInventory(null, null);

                else if (TradeSlot.class.isAssignableFrom(editorInfo.getTradeIcon()))
                    editor.openLayoutInventory(editorInfo.getTradeIcon(), null);

                else if (clickType == ClickType.LEFT && variantId == 0 || !editor.getIcons().containsValue(editorInfo.getTradeIcon())) //variantId == 0 means we're going to select the main item
                    //default behavior
                    editor.openLayoutInventory(editorInfo.getTradeIcon(), () -> {
                        boolean multi = MultiTradeIcon.class.isAssignableFrom(editorInfo.getTradeIcon());
                        if (multi) {
                            //start at 1 since the first ItemStack will be set when we select this item
                            variantId = 1;
                            updateItem(slot);
                        }

                        call.proceed();
                    });

                else if (Transition.class.isAssignableFrom(editorInfo.getTradeIcon()))
                    //transition icon
                    editor.openLayoutInventory(IconHandler.getTransitionTarget(editorInfo.getTradeIcon()), call);

                else if (MultiTradeIcon.class.isAssignableFrom(editorInfo.getTradeIcon()) && editorInfo instanceof MultiEditorInfo) {
                    //multi icon
                    MultiEditorInfo multiEditorInfo = (MultiEditorInfo) editorInfo;

                    try {
                        ItemStack[] variants = editor.getVariants(editorInfo.getTradeIcon(), null);
                        ItemStack previous = variants == null ? null : variants[variantId];

                        new ItemStackEnterGUI(gui.getPlayer(), "§8" + editorInfo.getName() + ": §9" + multiEditorInfo.getIconName()[variantId], previous, new Callback<ItemStack>() {
                            @Override
                            public void accept(ItemStack itemStack) {
                                editor.getVariants(editorInfo.getTradeIcon(), multiEditorInfo.getIconName().length)[variantId] = itemStack;
                                if (itemStack != null) {
                                    if (variantId == 0) {
                                        //update item in layout
                                        editor.getLayoutInventory().setItem(editor.getSlotOf(editorInfo.getTradeIcon()), itemStack);
                                    }

                                    variantId++;
                                    if (multiEditorInfo.getIconName().length == variantId) variantId = 0;
                                }

                                updateItem(slot);
                                getBasic().updateItems();
                            }
                        }).open();
                    } catch (AlreadyOpenedException | NoPageException | IsWaitingException e) {
                        e.printStackTrace();
                    }

                    //ignore close listener in API
                    return false;
                }

                //reopen when closed
                return true;
            }
        };
    }

    private boolean isLayoutInventoryNotEmpty() {
        ItemStack[] contents = editor.getLayoutInventory().getContents();

        for (ItemStack content : contents) {
            if (content != null && content.getType() != Material.AIR) return true;
        }

        return false;
    }

    public Type getType() {
        return type;
    }
}
