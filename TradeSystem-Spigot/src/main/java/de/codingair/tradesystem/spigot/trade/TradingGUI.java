package de.codingair.tradesystem.spigot.trade;

import de.codingair.codingapi.player.gui.anvil.*;
import de.codingair.codingapi.player.gui.inventory.gui.GUI;
import de.codingair.codingapi.player.gui.inventory.gui.GUIListener;
import de.codingair.codingapi.player.gui.inventory.gui.itembutton.ItemButton;
import de.codingair.codingapi.player.gui.inventory.gui.itembutton.ItemButtonOption;
import de.codingair.codingapi.player.gui.sign.SignGUI;
import de.codingair.codingapi.server.sounds.Sound;
import de.codingair.codingapi.server.sounds.SoundData;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.utils.TextAlignment;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.layout.Item;
import de.codingair.tradesystem.spigot.trade.layout.utils.Pattern;
import de.codingair.tradesystem.spigot.utils.Lang;
import de.codingair.tradesystem.spigot.utils.money.AdapterType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TradingGUI extends GUI {
    private final Trade trade;
    private final int id;
    private final Pattern layout;
    public boolean pause = false;

    public TradingGUI(Player p, int id, Trade trade) {
        super(p, Lang.get("GUI_Title", p).replace("%player%", trade.getOther(p.getName())), 54, TradeSystem.getInstance(), false);

        this.layout = TradeSystem.getInstance().getLayoutManager().getActive();
        this.trade = trade;
        this.id = id;

        setCanDropItems(true);
        setMoveOwnItems(true);
        setEditableSlots(true, trade.getSlots());

        initialize(p);

        addListener(new GUIListener() {
            @Override
            public void onDropItem(InventoryClickEvent e) {
                if (!TradeSystem.getInstance().getTradeManager().isDropItems()) {
                    //check for cursor
                    trade.getWaitForPickup()[trade.getId(getPlayer())] = true;
                    Bukkit.getScheduler().runTaskLater(TradeSystem.getInstance(), () -> {
                        trade.getCursor()[trade.getId(getPlayer())] = e.getCursor() != null && e.getCursor().getType() != Material.AIR;
                        trade.getWaitForPickup()[trade.getId(getPlayer())] = false;
                    }, 1);
                }
            }

            @Override
            public void onClickBottomInventory(InventoryClickEvent e) {
                if (!TradeSystem.getInstance().getTradeManager().isDropItems()) {
                    //check for cursor
                    trade.getWaitForPickup()[trade.getId(getPlayer())] = true;
                    Bukkit.getScheduler().runTaskLater(TradeSystem.getInstance(), () -> {
                        trade.getCursor()[trade.getId(getPlayer())] = e.getCursor() != null && e.getCursor().getType() != Material.AIR;
                        trade.getWaitForPickup()[trade.getId(getPlayer())] = false;
                        trade.cancelOverflow(trade.getOtherId(getPlayer()));
                    }, 1);
                }
            }

            @Override
            public void onInvClickEvent(InventoryClickEvent e) {
                //cancel faster --> fix dupe glitch
                if (e.getView().getTopInventory().equals(e.getClickedInventory()) && trade.getSlots().contains(e.getSlot()) && e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
                    trade.updateReady(trade.getId(getPlayer()), false);
                }

                if (e.getClick().name().equals("SWAP_OFFHAND")) {
                    if (e.getView().getTopInventory().equals(e.getClickedInventory())) {
                        e.setCancelled(true);
                        return;
                    }
                }

                //check if it's blocked
                ItemStack blockedItem = null;
                switch (e.getAction().name()) {
                    case "SWAP_WITH_CURSOR":
                    case "PLACE_ALL":
                    case "PLACE_ONE": {
                        //check cursor
                        blockedItem = e.getCursor();
                        break;
                    }

                    case "MOVE_TO_OTHER_INVENTORY": {
                        //check current
                        blockedItem = e.getCurrentItem();
                        break;
                    }

                    case "HOTBAR_SWAP":
                    case "HOTBAR_MOVE_AND_READD": {
                        //check hotbar
                        blockedItem = e.getView().getBottomInventory().getItem(e.getHotbarButton());
                        break;
                    }
                }

                if (blockedItem != null && TradeSystem.getInstance().getTradeManager().isBlocked(blockedItem)) {
                    e.setCancelled(true);
                    getPlayer().sendMessage(Lang.getPrefix() + Lang.get("Trade_Placed_Blocked_Item", p));
                    TradeSystem.getInstance().getTradeManager().playBlockSound(getPlayer());
                    return;
                }

                boolean fits = true;
                if (!TradeSystem.getInstance().getTradeManager().isDropItems()) {
                    //check for cursor
                    trade.getWaitForPickup()[trade.getId(getPlayer())] = true;
                    Bukkit.getScheduler().runTaskLater(TradeSystem.getInstance(), () -> {
                        trade.getCursor()[trade.getId(getPlayer())] = e.getCursor() != null && e.getCursor().getType() != Material.AIR;
                        trade.getWaitForPickup()[trade.getId(getPlayer())] = false;
                    }, 1);

                    if (!e.isCancelled()) {
                        //check if fits
                        switch (e.getAction().name()) {
                            case "PLACE_ONE": {
                                ItemStack item = e.getCurrentItem();
                                List<Integer> remove = new ArrayList<>();

                                if (item != null && item.getType() != Material.AIR) {
                                    item = item.clone();
                                    item.setAmount(item.getAmount() + 1);
                                    remove.add(e.getSlot());
                                } else {
                                    assert e.getCursor() != null;
                                    item = e.getCursor().clone();
                                    item.setAmount(1);
                                }

                                if (!trade.fitsTrade(getPlayer(), remove, item)) fits = false;
                                break;
                            }

                            case "PLACE_SOME": {
                                assert e.getCurrentItem() != null;
                                ItemStack item = e.getCurrentItem().clone();
                                item.setAmount(item.getMaxStackSize());

                                List<Integer> remove = new ArrayList<>();
                                remove.add(e.getSlot());

                                if (!trade.fitsTrade(getPlayer(), remove, item)) fits = false;
                                break;
                            }

                            case "PLACE_ALL":
                                assert e.getCursor() != null;
                                if (!trade.fitsTrade(getPlayer(), e.getCursor().clone())) fits = false;
                                break;

                            case "HOTBAR_SWAP": {
                                ItemStack item = e.getView().getBottomInventory().getItem(e.getHotbarButton());
                                if (item != null && !trade.fitsTrade(getPlayer(), item.clone())) fits = false;
                                break;
                            }

                            case "HOTBAR_MOVE_AND_READD": {
                                ItemStack item = e.getView().getBottomInventory().getItem(e.getHotbarButton());
                                List<Integer> remove = new ArrayList<>();
                                remove.add(e.getSlot());

                                if (item != null && !trade.fitsTrade(getPlayer(), remove, item.clone())) fits = false;
                                else if (isMovable(e.getSlot())) { //only own items
                                    e.setCancelled(true);

                                    ItemStack current = e.getView().getTopInventory().getItem(e.getSlot());
                                    assert current != null;
                                    ItemStack top = current.clone();

                                    current = e.getView().getBottomInventory().getItem(e.getHotbarButton());
                                    assert current != null;
                                    ItemStack bottom = current.clone();

                                    e.getView().getTopInventory().setItem(e.getSlot(), bottom);
                                    e.getView().getBottomInventory().setItem(e.getHotbarButton(), top);
                                }
                                break;
                            }

                            case "SWAP_WITH_CURSOR": {
                                List<Integer> remove = new ArrayList<>();
                                remove.add(e.getSlot());

                                assert e.getCursor() != null;
                                if (!trade.fitsTrade(getPlayer(), remove, e.getCursor().clone())) fits = false;
                                break;
                            }

                            case "DROP_ALL_CURSOR":
                            case "DROP_ALL_SLOT":
                            case "DROP_ONE_CURSOR":
                            case "DROP_ONE_SLOT":
                                assert e.getCurrentItem() != null;
                                if (!trade.fitsTrade(getPlayer(), e.getCurrentItem().clone())) fits = false;
                                break;

                            default:
                                fits = true;
                                break;
                        }
                    }
                }

                if (!fits) {
                    getPlayer().sendMessage(Lang.getPrefix() + Lang.get("Trade_Partner_No_Space", p));
                    TradeSystem.getInstance().getTradeManager().playBlockSound(getPlayer());
                    e.setCancelled(true);
                } else Bukkit.getScheduler().runTaskLater(TradeSystem.getInstance(), (Runnable) trade::update, 1);
            }

            @Override
            public void onInvOpenEvent(InventoryOpenEvent e) {
            }

            @Override
            public void onInvCloseEvent(InventoryCloseEvent e) {
                if (!pause) trade.cancel();
            }

            @Override
            public void onInvDragEvent(InventoryDragEvent e) {
                if (e.getNewItems().isEmpty()) return;

                //check if it's blocked
                if (TradeSystem.getInstance().getTradeManager().isBlocked(e.getNewItems().values().iterator().next())) {
                    e.setCancelled(true);
                    getPlayer().sendMessage(Lang.getPrefix() + Lang.get("Trade_Placed_Blocked_Item", p));
                }

                if (!e.isCancelled() && !TradeSystem.getInstance().getTradeManager().isDropItems() && !trade.fitsTrade(getPlayer(), e.getNewItems().values().toArray(new ItemStack[0]))) {
                    getPlayer().sendMessage(Lang.getPrefix() + Lang.get("Trade_Partner_No_Space", p));
                    TradeSystem.getInstance().getTradeManager().playBlockSound(getPlayer());
                    e.setCancelled(true);
                } else Bukkit.getScheduler().runTaskLater(TradeSystem.getInstance(), (Runnable) trade::update, 1);
            }

            @Override
            public void onMoveToTopInventory(ItemStack item, int oldRawSlot, List<Integer> newRawSlots) {

            }

            @Override
            public boolean onMoveToTopInventory(int oldRawSlot, List<Integer> newRawSlots, ItemStack item) {
                //check if it's blocked
                if (TradeSystem.getInstance().getTradeManager().isBlocked(item)) {
                    getPlayer().sendMessage(Lang.getPrefix() + Lang.get("Trade_Placed_Blocked_Item", p));
                    TradeSystem.getInstance().getTradeManager().playBlockSound(getPlayer());
                    return true;
                }

                if (!TradeSystem.getInstance().getTradeManager().isDropItems() && !trade.fitsTrade(getPlayer(), item)) {
                    getPlayer().sendMessage(Lang.getPrefix() + Lang.get("Trade_Partner_No_Space", p));
                    TradeSystem.getInstance().getTradeManager().playBlockSound(getPlayer());
                    return true;
                } else Bukkit.getScheduler().runTaskLater(TradeSystem.getInstance(), (Runnable) trade::update, 1);

                return false;
            }

            @Override
            public void onCollectToCursor(ItemStack item, List<Integer> oldRawSlots, int newRawSlot) {
                Bukkit.getScheduler().runTaskLater(TradeSystem.getInstance(), (Runnable) trade::update, 1);
            }
        });
    }

    public void synchronizeTitle() {
        setTitle(Lang.get("GUI_Title", getPlayer()).replace("%player%", trade.getOther(getPlayer().getName())), true);
    }

    @Override
    public void clear() {
        super.getButtons().clear();
        for (int i = 0; i < getSize(); i++) {
            if (trade.getSlots().contains(i) || trade.getOtherSlots().contains(i)) continue;
            setItem(i, new ItemStack(Material.AIR));
        }
    }

    @Override
    public void initialize(Player p) {
        setBuffering(true);

        for (int i = 0; i < 54; i++) {
            if (trade.getSlots().contains(i) || trade.getOtherSlots().contains(i)) continue;
            setItem(i, new ItemStack(Material.AIR));
        }

        for (Item item : this.layout.getItems()) {
            handleItem(item);
        }

        release();
    }

    private void handleItem(Item item) {
        if (item == null || item.getFunction() == null) return;

        ItemButtonOption option = new ItemButtonOption();
        option.setOnlyLeftClick(true);
        option.setClickSound(new SoundData(Sound.UI_BUTTON_CLICK, 0.6F, 1F));

        switch (item.getFunction()) {
            case DECORATION: {
                setItem(item.getSlot(), new ItemBuilder(item.getItem()).setHideName(true).getItem());
                break;
            }

            case PICK_MONEY: {
                if (AdapterType.canEnable() && TradeSystem.getInstance().getTradeManager().isTradeMoney()) {
                    int money = trade.getMoney()[id];
                    ItemBuilder moneyBuilder = new ItemBuilder(item.getItem()).setName("§e" + Lang.get("Money_Amount", getPlayer()) + ": §7" + TradeSystem.getInstance().getTradeManager().makeMoneyFancy(money) + " " + (money == 1 ? Lang.get("Coin", getPlayer()) : Lang.get("Coins", getPlayer()))).addLore("", "§7» " + Lang.get("Click_To_Change", getPlayer()));
                    if (trade.getMoney()[id] > 0) moneyBuilder.addEnchantment(Enchantment.DAMAGE_ALL, 1).setHideEnchantments(true);

                    addButton(new ItemButton(item.getSlot(), moneyBuilder.getItem()) {
                        @Override
                        public void onClick(InventoryClickEvent e) {
                            if (TradeSystem.getProfile(getPlayer()).getMoney() <= 0) {
                                getPlayer().sendMessage(Lang.getPrefix() + Lang.get("No_Money", getPlayer()));
                                return;
                            }

                            pause = true;
                            switch (TradeSystem.man().getMoneyGUI()) {
                                case SIGN:
                                    openSignGUI();
                                    break;

                                case ANVIL:
                                    openAnvilGUI();
                                    break;
                            }
                        }

                        private void openAnvilGUI() {
                            AnvilGUI.openAnvil(TradeSystem.getInstance(), getPlayer(), new AnvilListener() {
                                private int amount = -999;

                                @Override
                                public void onClick(AnvilClickEvent e) {
                                    if (!e.getSlot().equals(AnvilSlot.OUTPUT)) return;

                                    try {
                                        String in = e.getInput(false).trim().toLowerCase();
                                        amount = processInput(in);
                                    } catch (NumberFormatException ignored) {
                                    }

                                    if (amount < 0) {
                                        amount = -999;
                                        e.getPlayer().sendMessage(Lang.getPrefix() + Lang.get("Enter_Correct_Amount", getPlayer()));
                                        return;
                                    }

                                    int max;
                                    if (amount > (max = TradeSystem.getProfile(e.getPlayer()).getMoney())) {
                                        amount = -999;
                                        e.getPlayer().sendMessage(Lang.getPrefix() + (max == 1 ? Lang.get("Only_1_Coin", getPlayer()).replace("%coins%", max + "") : Lang.get("Only_X_Coins", getPlayer()).replace("%coins%", TradeSystem.getInstance().getTradeManager().makeMoneyFancy(max) + "")));
                                        return;
                                    }

                                    e.setClose(true);
                                }

                                @Override
                                public void onClose(AnvilCloseEvent e) {
                                    e.setPost(() -> {
                                        if (trade.isFinished()) return;

                                        if (amount >= 0) {
                                            trade.getMoney()[id] = amount;
                                            trade.update();
                                        }

                                        pause = false;
                                        open();
                                    });
                                }
                            }, new ItemBuilder(Material.PAPER).setName(trade.getMoney()[id] == 0 ? (Lang.get("Money_Amount", getPlayer()) + "...") : TradeSystem.getInstance().getTradeManager().makeMoneyFancy(trade.getMoney()[id]) + "").getItem());
                        }

                        private void openSignGUI() {
                            Bukkit.getScheduler().runTask(TradeSystem.getInstance(), () -> {
                                String input = "";
                                if (trade.getMoney()[id] != 0) input += trade.getMoney()[id];

                                new SignGUI(getPlayer(), TradeSystem.getInstance(), input, "^^^^^^^^", "Enter money", "above") {
                                    @Override
                                    public void onSignChangeEvent(String[] s) {
                                        if (trade.cancelling) {
                                            close();
                                            return;
                                        }

                                        int amount = -999;

                                        String in;
                                        try {
                                            in = ChatColor.stripColor(s[0]).trim().toLowerCase();

                                            if (in.isEmpty()) {
                                                //cancel
                                                goBack();
                                                return;
                                            }

                                            amount = processInput(in);
                                        } catch (NumberFormatException ignored) {
                                            in = "";
                                        }

                                        int max;
                                        if (amount < 0) {
                                            getPlayer().sendMessage(Lang.getPrefix() + Lang.get("Enter_Correct_Amount", getPlayer()));
                                        } else if (amount > (max = TradeSystem.getProfile(getPlayer()).getMoney())) {
                                            getPlayer().sendMessage(Lang.getPrefix() + (max == 1 ? Lang.get("Only_1_Coin", getPlayer()).replace("%coins%", max + "") : Lang.get("Only_X_Coins", getPlayer()).replace("%coins%", TradeSystem.getInstance().getTradeManager().makeMoneyFancy(max) + "")));
                                        } else {
                                            trade.getMoney()[id] = amount;
                                            trade.update();

                                            goBack();
                                            return;
                                        }

                                        getLines()[0] = in;
                                        Bukkit.getScheduler().runTask(TradeSystem.getInstance(), this::open);
                                    }

                                    private void goBack() {
                                        pause = false;
                                        close();
                                        TradingGUI.this.open();
                                    }
                                }.open();
                            });
                        }

                        private int processInput(String in) {
                            Integer factor = TradeSystem.getInstance().getTradeManager().getMoneyShortcutFactor(in);

                            //example: "1,000,000.5" -> "1.5 mio"
                            if (factor != null) {
                                //allow comma
                                in = in.replaceAll(",", ".");
                                String moneyIn = in.replaceAll("[a-z]", "").trim();
                                return (int) (Double.parseDouble(moneyIn) * factor);
                            } else {
                                in = in.replaceAll(",", "");
                                int sep = in.indexOf(".");
                                if (sep == -1) sep = in.length();

                                String moneyIn = in.substring(0, sep).replaceAll("\\D", "");
                                return Integer.parseInt(moneyIn);
                            }
                        }
                    }.setOption(option));
                }
                break;
            }

            case SHOW_MONEY: {
                int money = trade.getMoney()[trade.getOtherId(id)];

                ItemBuilder moneyBuilder = new ItemBuilder(item.getItem()).setName("§e" + Lang.get("Money_Amount", getPlayer()) + ": §7" + TradeSystem.getInstance().getTradeManager().makeMoneyFancy(money) + " " + (money == 1 ? Lang.get("Coin", getPlayer()) : Lang.get("Coins", getPlayer())));
                if (trade.getMoney()[trade.getOtherId(id)] > 0) moneyBuilder.addEnchantment(Enchantment.DAMAGE_ALL, 1).setHideEnchantments(true);

                if (AdapterType.canEnable() && TradeSystem.getInstance().getTradeManager().isTradeMoney()) setItem(item.getSlot(), moneyBuilder.getItem());
                break;
            }

            case MONEY_REPLACEMENT: {
                if (!AdapterType.canEnable() || !TradeSystem.getInstance().getTradeManager().isTradeMoney())
                    setItem(item.getSlot(), new ItemBuilder(item.getItem()).setHideName(true).setHideEnchantments(true).setHideStandardLore(true).getItem());
                break;
            }

            case PICK_STATUS_NONE: {
                if (!TradeSystem.getInstance().getTradeManager().isTradeBoth() && !trade.emptyTrades()) break;

                boolean canFinish = false;
                for (Integer slot : trade.getSlots()) {
                    if (getItem(slot) != null && !getItem(slot).getType().equals(Material.AIR)) canFinish = true;
                }
                if (trade.getMoney()[id] != 0) canFinish = true;

                ItemBuilder ready = new ItemBuilder(item.getItem());
                if (!canFinish) {
                    ready.setText("§c" + Lang.get("Trade_Only_With_Objects", getPlayer()), TextAlignment.LEFT, 150);
                    setItem(item.getSlot(), ready.getItem());
                }
                break;
            }

            case PICK_STATUS_NOT_READY: {
                boolean canFinish = canFinish();

                ItemBuilder ready = new ItemBuilder(item.getItem());
                if (canFinish) {
                    if (trade.getReady()[id]) return;
                    else ready.setName("§7" + Lang.get("Status", getPlayer()) + ": §c" + Lang.get("Not_Ready", getPlayer()));

                    addButton(new ItemButton(item.getSlot(), ready.getItem()) {
                        @Override
                        public void onClick(InventoryClickEvent e) {
                            trade.updateReady(id, !trade.getReady()[id]);
                        }
                    }.setOption(option));
                }
                break;
            }

            case PICK_STATUS_READY: {
                boolean canFinish = canFinish();

                ItemBuilder ready = new ItemBuilder(item.getItem());
                if (canFinish) {
                    if (trade.getReady()[id])
                        ready.setName("§7" + Lang.get("Status", getPlayer()) + ": §a" + Lang.get("Ready", getPlayer())).addLore("", "§7" + Lang.get("Wait_For_Other_Player", getPlayer()));
                    else return;

                    addButton(new ItemButton(item.getSlot(), ready.getItem()) {
                        @Override
                        public void onClick(InventoryClickEvent e) {
                            trade.updateReady(id, !trade.getReady()[id]);
                        }
                    }.setOption(option));
                }
                break;
            }

            case SHOW_STATUS_NOT_READY: {
                ItemBuilder ready = new ItemBuilder(item.getItem());
                if (trade.getReady()[trade.getOtherId(id)]) return;
                else ready.setName("§7" + Lang.get("Status", getPlayer()) + ": §c" + Lang.get("Not_Ready", getPlayer()));
                setItem(item.getSlot(), ready.getItem());
                break;
            }

            case SHOW_STATUS_READY: {
                ItemBuilder ready = new ItemBuilder(item.getItem());
                if (trade.getReady()[trade.getOtherId(id)]) ready.setColor(DyeColor.LIME).setName("§7" + Lang.get("Status", getPlayer()) + ": §a" + Lang.get("Ready", getPlayer()));
                else return;
                setItem(item.getSlot(), ready.getItem());
                break;
            }

            case CANCEL: {
                addButton(new ItemButton(item.getSlot(), new ItemBuilder(item.getItem()).setName("§c" + Lang.get("Cancel_Trade", getPlayer())).getItem()) {
                    @Override
                    public void onClick(InventoryClickEvent e) {
                        trade.cancel();
                    }
                }.setOption(option));
                break;
            }
        }
    }

    private boolean canFinish() {
        boolean canFinish = !TradeSystem.getInstance().getTradeManager().isTradeBoth() && !trade.emptyTrades();

        if (!canFinish) {
            for (Integer slot : trade.getSlots()) {
                if (getItem(slot) != null && !getItem(slot).getType().equals(Material.AIR)) canFinish = true;
            }
            if (trade.getMoney()[id] != 0) canFinish = true;
        }
        return canFinish;
    }
}
