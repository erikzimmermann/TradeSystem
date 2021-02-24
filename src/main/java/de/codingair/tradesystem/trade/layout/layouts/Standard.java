package de.codingair.tradesystem.trade.layout.layouts;

import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import de.codingair.tradesystem.trade.layout.Function;
import de.codingair.tradesystem.trade.layout.Item;
import de.codingair.tradesystem.trade.layout.utils.AbstractPattern;
import de.codingair.tradesystem.trade.layout.utils.Pattern;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Standard extends AbstractPattern implements Pattern {
    public Standard() {
        super(null, null);
    }

    @Override
    public String getName() {
        return "Standard";
    }

    @Override
    public List<Item> getItems() {
        ItemStack blackStained = new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE).getItem();
        ItemStack grayStained = new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE).getItem();
        ItemStack nugget = new ItemBuilder(XMaterial.GOLD_NUGGET).getItem();
        ItemStack barrier = new ItemBuilder(Material.BARRIER).getItem();

        ItemStack status_none = new ItemBuilder(XMaterial.LIGHT_GRAY_TERRACOTTA).getItem();
        ItemStack status_ready = new ItemBuilder(XMaterial.LIME_TERRACOTTA).getItem();
        ItemStack status_not_ready = new ItemBuilder(XMaterial.RED_TERRACOTTA).getItem();


        List<Item> items = new ArrayList<>();

        items.add(new Item(0, blackStained, Function.DECORATION));
        items.add(new Item(1, blackStained, Function.DECORATION));
        items.add(new Item(2, blackStained, Function.DECORATION));
        items.add(new Item(6, blackStained, Function.DECORATION));
        items.add(new Item(7, blackStained, Function.DECORATION));
        items.add(new Item(8, blackStained, Function.DECORATION));
        items.add(new Item(11, blackStained, Function.DECORATION));
        items.add(new Item(20, blackStained, Function.DECORATION));
        items.add(new Item(21, blackStained, Function.DECORATION));
        items.add(new Item(30, blackStained, Function.DECORATION));
        items.add(new Item(31, blackStained, Function.DECORATION));
        items.add(new Item(32, blackStained, Function.DECORATION));
        items.add(new Item(23, blackStained, Function.DECORATION));
        items.add(new Item(24, blackStained, Function.DECORATION));
        items.add(new Item(15, blackStained, Function.DECORATION));

        items.add(new Item(4, grayStained, Function.DECORATION));
        items.add(new Item(13, grayStained, Function.DECORATION));
        items.add(new Item(40, grayStained, Function.DECORATION));
        items.add(new Item(49, grayStained, Function.DECORATION));

        items.add(new Item(22, barrier, Function.CANCEL));
        items.add(new Item(3, nugget, Function.PICK_MONEY));
        items.add(new Item(3, grayStained, Function.MONEY_REPLACEMENT));
        items.add(new Item(5, nugget, Function.SHOW_MONEY));
        items.add(new Item(5, grayStained, Function.MONEY_REPLACEMENT));

        items.add(new Item(12, status_none, Function.PICK_STATUS_NONE));
        items.add(new Item(12, status_not_ready, Function.PICK_STATUS_NOT_READY));
        items.add(new Item(12, status_ready, Function.PICK_STATUS_READY));

        items.add(new Item(14, status_not_ready, Function.SHOW_STATUS_NOT_READY));
        items.add(new Item(14, status_ready, Function.SHOW_STATUS_READY));

        items.add(new Item(9, null, Function.EMPTY_FIRST_TRADER));
        items.add(new Item(10, null, Function.EMPTY_FIRST_TRADER));
        items.add(new Item(18, null, Function.EMPTY_FIRST_TRADER));
        items.add(new Item(19, null, Function.EMPTY_FIRST_TRADER));
        items.add(new Item(27, null, Function.EMPTY_FIRST_TRADER));
        items.add(new Item(28, null, Function.EMPTY_FIRST_TRADER));
        items.add(new Item(29, null, Function.EMPTY_FIRST_TRADER));
        items.add(new Item(36, null, Function.EMPTY_FIRST_TRADER));
        items.add(new Item(37, null, Function.EMPTY_FIRST_TRADER));
        items.add(new Item(38, null, Function.EMPTY_FIRST_TRADER));
        items.add(new Item(39, null, Function.EMPTY_FIRST_TRADER));
        items.add(new Item(45, null, Function.EMPTY_FIRST_TRADER));
        items.add(new Item(46, null, Function.EMPTY_FIRST_TRADER));
        items.add(new Item(47, null, Function.EMPTY_FIRST_TRADER));
        items.add(new Item(48, null, Function.EMPTY_FIRST_TRADER));

        items.add(new Item(17, null, Function.EMPTY_SECOND_TRADER));
        items.add(new Item(16, null, Function.EMPTY_SECOND_TRADER));
        items.add(new Item(26, null, Function.EMPTY_SECOND_TRADER));
        items.add(new Item(25, null, Function.EMPTY_SECOND_TRADER));
        items.add(new Item(35, null, Function.EMPTY_SECOND_TRADER));
        items.add(new Item(34, null, Function.EMPTY_SECOND_TRADER));
        items.add(new Item(33, null, Function.EMPTY_SECOND_TRADER));
        items.add(new Item(44, null, Function.EMPTY_SECOND_TRADER));
        items.add(new Item(43, null, Function.EMPTY_SECOND_TRADER));
        items.add(new Item(42, null, Function.EMPTY_SECOND_TRADER));
        items.add(new Item(41, null, Function.EMPTY_SECOND_TRADER));
        items.add(new Item(53, null, Function.EMPTY_SECOND_TRADER));
        items.add(new Item(52, null, Function.EMPTY_SECOND_TRADER));
        items.add(new Item(51, null, Function.EMPTY_SECOND_TRADER));
        items.add(new Item(50, null, Function.EMPTY_SECOND_TRADER));

        return items;
    }

    @Override
    public boolean isStandard() {
        return true;
    }
}
