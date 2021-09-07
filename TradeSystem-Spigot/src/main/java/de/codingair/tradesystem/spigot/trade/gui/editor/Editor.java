package de.codingair.tradesystem.spigot.trade.gui.editor;

import de.codingair.codingapi.player.gui.inventory.v2.GUI;
import de.codingair.codingapi.player.gui.inventory.v2.Page;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.AlreadyOpenedException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.IsWaitingException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.NoPageException;
import de.codingair.codingapi.server.sounds.Sound;
import de.codingair.codingapi.tools.Call;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.gui.layout.Pattern;
import de.codingair.tradesystem.spigot.trade.gui.layout.registration.*;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.MultiTradeIcon;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.TradeIcon;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.Transition;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.basic.DecorationIcon;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.basic.TradeSlot;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.basic.TradeSlotOther;
import de.codingair.tradesystem.spigot.trade.gui.layout.utils.IconData;
import de.codingair.tradesystem.spigot.utils.Lang;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class Editor extends GUI {
    public static final String ITEM_TITLE_COLOR = "§6§n";
    public static final String ITEM_SUB_TITLE_COLOR = "§3";

    private final String name;
    private final ItemStack[] copy = new ItemStack[54];
    private final Inventory layoutInventory;
    private final HashMap<Integer, Class<? extends TradeIcon>> icons = new HashMap<>();
    private final HashMap<Class<? extends TradeIcon>, ItemStack[]> variants = new HashMap<>();

    public Editor(String name, Player player) {
        this(name, null, player);
    }

    public Editor(Pattern pattern, Player player) {
        this(pattern.getName(), pattern, player);
    }

    private Editor(@NotNull String name, @Nullable Pattern pattern, Player player) {
        super(player, TradeSystem.getInstance(), 27, Lang.get("Layout_Editor", player));
        this.name = name;
        this.layoutInventory = Bukkit.createInventory(player, 54, "§c" + Lang.get("Layout_Editor_Set_Items", player));
        if (pattern != null) applyPattern(pattern);

        LayoutPage basic = new LayoutPage(this);

        boolean first = true;
        for (Type value : Type.values()) {
            if (IconHandler.isTypeEmpty(value)) continue;

            registerPage(new IconPage(this, value, basic), first);
            first = false;
        }
    }

    @Override
    public void open() throws AlreadyOpenedException, NoPageException, IsWaitingException {
        super.open();
        Sound.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(player, 0.7F, 1);
    }

    @NotNull
    Page getPage(@NotNull Type type) {
        for (Page value : pages) {
            if (value instanceof IconPage) {
                if (((IconPage) value).getType() == type) return value;
            }
        }

        throw new IllegalStateException("Could not found page with type " + type);
    }

    private void applyPattern(@NotNull Pattern pattern) {
        int slot = -1;

        for (IconData iconData : pattern) {
            slot++;
            if (iconData == null) continue;

            if (TradeSlot.class.isAssignableFrom(iconData.getTradeIcon())) {
                this.icons.put(slot, iconData.getTradeIcon());
                continue;
            }

            ItemStack adding = null;
            if (iconData.getItems().length > 0) {
                adding = iconData.getItems()[0];
            }

            layoutInventory.setItem(slot, adding);
            if (DecorationIcon.class.isAssignableFrom(iconData.getTradeIcon())) continue;

            this.icons.put(slot, iconData.getTradeIcon());

            if (MultiTradeIcon.class.isAssignableFrom(iconData.getTradeIcon())) {
                variants.put(iconData.getTradeIcon(), iconData.getItems());
            }
        }
    }

    public Pattern buildPattern() {
        IconData[] data = new IconData[54];

        for (int slot = 0; slot < 54; slot++) {
            Class<? extends TradeIcon> iconClass = icons.get(slot);

            IconData icon = null;
            if (iconClass != null) {
                if (MultiTradeIcon.class.isAssignableFrom(iconClass)) {
                    //multi
                    ItemStack[] items = variants.get(iconClass);
                    if (items == null) throw new IllegalStateException("Cannot create a layout pattern without enough information: missing variants for " + iconClass.getName());

                    icon = new IconData(iconClass, items);
                } else if (TradeSlot.class.isAssignableFrom(iconClass)) {
                    //multi
                    icon = new IconData(iconClass);
                } else {
                    //normal
                    ItemStack item = layoutInventory.getItem(slot);
                    if (item == null) throw new IllegalStateException("Cannot create a layout pattern without enough information: missing item for " + iconClass.getName());

                    icon = new IconData(iconClass, item);
                }
            } else {
                ItemStack item = layoutInventory.getItem(slot);
                if (item != null) icon = new IconData(DecorationIcon.class, item);
            }

            data[slot] = icon;
        }

        return new Pattern(name, data);
    }

    public boolean canFinish() {
        //all necessary set?
        for (EditorInfo necessaryIcon : IconHandler.getNecessaryIcons()) {
            if (!icons.containsValue(necessaryIcon.getTradeIcon())) {
                return false;
            }
        }

        //all variants set?
        for (Class<? extends TradeIcon> value : icons.values()) {
            if (MultiTradeIcon.class.isAssignableFrom(value)) {
                ItemStack[] items = variants.get(value);

                if (items == null) return false;
                for (ItemStack item : items) {
                    if (item == null) return false;
                }
            }
        }

        //transition origins/targets set?
        for (Class<? extends TradeIcon> value : icons.values()) {
            if (Transition.class.isAssignableFrom(value)) {
                Class<? extends TradeIcon> target = IconHandler.getTransitionTarget(value);
                if (!icons.containsValue(target)) return false;
            } else if (Transition.Consumer.class.isAssignableFrom(value)) {
                TransitionTargetEditorInfo info = (TransitionTargetEditorInfo) IconHandler.getInfo(value);
                Class<? extends TradeIcon> origin = info.getOrigin();
                if (!icons.containsValue(origin)) return false;
            }
        }

        //same trade slot amount?
        int tradeSlots = (int) icons.values().stream().filter(i -> i.equals(TradeSlot.class)).count();
        int otherTradeSlots = (int) icons.values().stream().filter(i -> i.equals(TradeSlotOther.class)).count();

        if (tradeSlots == 0) return false;
        return tradeSlots == otherTradeSlots;
    }

    void openLayoutInventory(@Nullable Class<? extends TradeIcon> setting, @Nullable Call callback) {
        prepareLayout();

        Bukkit.getPluginManager().registerEvents(createListener(setting, callback), TradeSystem.getInstance());
        player.openInventory(this.layoutInventory);
    }

    private void prepareLayout() {
        ItemStack[] items = this.layoutInventory.getContents();

        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            copy[i] = item == null ? null : item.clone();

            Class<? extends TradeIcon> icon = this.icons.get(i);
            if (icon != null) {
                if (TradeSlot.class.isAssignableFrom(icon)) {
                    layoutInventory.setItem(i, buildSlotCursor(icon, 1));
                } else if (item != null) {
                    //add marker
                    item.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);

                    ItemMeta meta = item.getItemMeta();
                    assert meta != null;

                    String name = IconHandler.getInfo(icon).getName();
                    meta.setDisplayName("§c" + name);

                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                    item.setItemMeta(meta);
                }
            }
        }
    }

    private void cleanLayout() {
        for (int i = 0; i < this.copy.length; i++) {
            cleanItem(i, null);
        }
    }

    private void cleanItem(int slot, @Nullable Class<? extends TradeIcon> icon) {
        if (icon != null && TradeSlot.class.isAssignableFrom(icon)) {
            this.copy[slot] = null;
            this.layoutInventory.setItem(slot, null);
            return;
        }

        if (this.copy[slot] == null || this.layoutInventory.getItem(slot) == null) return;
        this.layoutInventory.setItem(slot, this.copy[slot]);
        this.copy[slot] = null;
    }

    @NotNull
    private ItemStack buildSlotCursor(Class<? extends TradeIcon> icon, int amount) {
        ItemBuilder builder = new ItemBuilder();

        if (TradeSlot.class.equals(icon)) {
            builder.setType(XMaterial.BLACK_STAINED_GLASS_PANE);
        } else {
            builder.setType(XMaterial.WHITE_STAINED_GLASS_PANE);
        }

        builder.setAmount(amount);
        builder.addEnchantment(Enchantment.DAMAGE_ALL, 1);
        builder.setHideEnchantments(true);

        EditorInfo info = IconHandler.getInfo(icon);
        builder.setName("§c" + info.getName());

        return builder.getItem();
    }

    public boolean needsMoreDecorationItems() {
        boolean needItems = false;
        for (EditorInfo e : IconHandler.getNecessaryIcons()) {
            if (!this.icons.containsValue(e.getTradeIcon())) {
                needItems = true;
                break;
            }
        }

        if (!needItems) return false;

        ItemStack[] items = this.layoutInventory.getContents();
        for (int i = 0; i < items.length; i++) {
            if (this.icons.containsKey(i)) continue;
            if (items[i] != null && items[i].getType() != Material.AIR) return false;
        }

        return true;
    }

    public boolean hasNoUsableItems() {
        ItemStack[] items = this.layoutInventory.getContents();
        for (int i = 0; i < items.length; i++) {
            Class<? extends TradeIcon> current = this.icons.get(i);
            if (current != null && TradeSlot.class.isAssignableFrom(current)) continue;

            if (items[i] != null && items[i].getType() != Material.AIR) return false;
        }

        return true;
    }

    private int countIcon(@NotNull Class<? extends TradeIcon> setting) {
        int count = 0;

        for (Class<? extends TradeIcon> value : this.icons.values()) {
            if (value.equals(setting)) count++;
        }

        return count;
    }

    public int getSlotOf(@NotNull Class<? extends TradeIcon> icon) {
        for (Map.Entry<Integer, Class<? extends TradeIcon>> e : icons.entrySet()) {
            if (icon.equals(e.getValue())) return e.getKey();
        }

        return -1;
    }

    public Map<Integer, Class<? extends TradeIcon>> getIcons() {
        return icons;
    }

    public int getAmountOf(@NotNull Class<? extends TradeIcon> icon) {
        return (int) icons.values().stream().filter(i -> i.equals(icon)).count();
    }

    public Inventory getLayoutInventory() {
        return layoutInventory;
    }

    @Nullable
    public ItemStack[] getVariants(@NotNull Class<? extends TradeIcon> icon, @Nullable Integer size) {
        if (size == null) return variants.get(icon);
        return variants.computeIfAbsent(icon, i -> new ItemStack[size]);
    }

    @NotNull
    private Listener createListener(@Nullable Class<? extends TradeIcon> setting, @Nullable Call callback) {
        //noinspection unused
        return new Listener() {
            private boolean open = false;

            @EventHandler (priority = EventPriority.LOW)
            public void onClose(InventoryCloseEvent e) {
                if (!player.equals(e.getPlayer()) || !open) return;
                closing(e.getView());
            }

            @EventHandler
            public void onOpen(InventoryOpenEvent e) {
                if (!player.equals(e.getPlayer())) return;

                if (isSlotIcon()) {
                    assert setting != null;
                    int amount = 26 - countIcon(setting);
                    Bukkit.getScheduler().runTaskLater(TradeSystem.getInstance(), () -> e.getView().setCursor(buildSlotCursor(setting, amount)), 1);
                }

                open = true;
            }

            @EventHandler
            public void onDrop(PlayerDropItemEvent e) {
                if (!player.equals(e.getPlayer())) return;

                if (isSlotIcon()) {
                    e.setCancelled(true);
                }
            }

            @EventHandler
            public void onDrag(InventoryDragEvent e) {
                if (!player.equals(e.getWhoClicked())) return;

                if (isSlotIcon()) {
                    for (Integer rawSlot : e.getRawSlots()) {
                        if (rawSlot > 54) {
                            e.setCancelled(true);
                            return;
                        }
                    }
                }
            }

            @EventHandler
            public void onClick(InventoryClickEvent e) {
                if (!player.equals(e.getWhoClicked())) return;

                if (setting != null) {
                    if (callback != null) {
                        e.setCancelled(true);

                        if (e.getView().getTopInventory().equals(e.getClickedInventory()) && e.getCurrentItem() != null) {
                            Class<? extends TradeIcon> icon = icons.get(e.getSlot());
                            //trade slots don't contain any items
                            if (icon != null && TradeSlot.class.isAssignableFrom(icon)) return;

                            removeIcon(e.getSlot());
                            removeIcon(getSlotOf(setting));
                            icons.put(e.getSlot(), setting);

                            if (MultiTradeIcon.class.isAssignableFrom(setting)) {
                                //set variant of multi icon
                                MultiEditorInfo info = (MultiEditorInfo) IconHandler.getInfo(setting);

                                cleanItem(e.getSlot(), null);
                                getVariants(setting, info.getIconName().length)[0] = e.getCurrentItem().clone();
                            }

                            Sound.UI_BUTTON_CLICK.playSound(player, 0.7F, 1);
                            closing(e.getView());
                            callback.proceed();
                        }
                    } else {
                        //setting trade slots
                        if (!e.getView().getTopInventory().equals(e.getClickedInventory())) e.setCancelled(true);
                        else {
                            Class<? extends TradeIcon> current = icons.get(e.getSlot());
                            if (current != null && !setting.equals(current)) {
                                e.setCancelled(true);
                            } else if (e.getAction() == InventoryAction.HOTBAR_SWAP
                                    || e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                                e.setCancelled(true);
                            }
                        }
                    }
                } else {
                    Class<? extends TradeIcon> icon;
                    if (e.getView().getTopInventory().equals(e.getClickedInventory()) && e.getCurrentItem() != null && (icon = icons.remove(e.getSlot())) != null) {
                        //remove marker
                        cleanItem(e.getSlot(), icon);
                    }
                }
            }

            private void removeIcon(int slot) {
                Class<? extends TradeIcon> icon = icons.remove(slot);
                if (icon == null) return;

                if (Transition.class.isAssignableFrom(icon)) {
                    EditorInfo info = IconHandler.getInfo(icon);
                    Class<? extends TradeIcon> target = info.getTransitionTarget();
                    assert target != null;

                    if (icons.containsValue(target)) {
                        removeIcon(getSlotOf(target));
                    }
                }
            }

            private boolean isSlotIcon() {
                if (setting == null) return false;
                return TradeSlot.class.isAssignableFrom(setting);
            }

            private void closing(InventoryView view) {
                if (isSlotIcon()) {
                    ItemStack cursor = buildSlotCursor(setting, 1);
                    view.setCursor(null);

                    icons.entrySet().removeIf(e -> e.getValue().equals(setting) && layoutInventory.getItem(e.getKey()) == null);
                    for (int slot = 0; slot < layoutInventory.getContents().length; slot++) {
                        ItemStack item = layoutInventory.getContents()[slot];

                        if (cursor.isSimilar(item)) {
                            icons.putIfAbsent(slot, setting);
                            item.setAmount(1);
                        }
                    }
                }

                HandlerList.unregisterAll(this);
                cleanLayout();
                getActive().updateItems();
            }
        };
    }
}
