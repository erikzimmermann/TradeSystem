package de.codingair.tradesystem.spigot.trade.layout.utils;

import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.layout.Pattern;
import de.codingair.tradesystem.spigot.trade.layout.types.TradeIcon;
import de.codingair.tradesystem.spigot.trade.layout.types.impl.basic.*;
import de.codingair.tradesystem.spigot.trade.layout.types.impl.economy.exp.ExpLevelIcon;
import de.codingair.tradesystem.spigot.trade.layout.types.impl.economy.exp.ShowExpLevelIcon;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * An import function which converts JSON stored patterns from TradeSystem v1.3.2 into new patterns.
 */
public class ImportHelper {

    @NotNull
    public static Pattern convert(@NotNull String data) throws ParseException {
        JSONObject json = (JSONObject) new JSONParser().parse(data);

        String name = (String) json.get("Name");
        IconData[] items = new IconData[54];

        JSONArray jsonA = (JSONArray) new JSONParser().parse((String) json.get("Items"));

        for (Object o : jsonA) {
            String s = (String) o;
            convertIcon(s, items);
        }

        return new Pattern(name, items);
    }

    private static void convertIcon(@NotNull String data, IconData[] items) throws ParseException {
        JSONObject json = (JSONObject) new JSONParser().parse(data);

        int slot = Integer.parseInt("" + json.get("Slot"));
        ItemStack item = json.get("Item") == null ? null : ItemBuilder.getFromJSON((String) json.get("Item")).getItem();
        Class<? extends TradeIcon> icon = convertFunction((String) json.get("Function"));
        if (icon == null) return;

        if (item != null) {
            if (item.getType() == Material.AIR) item = null;
        }

        if (icon.equals(StatusIcon.CannotReadyIcon.class)) {
            if (items[slot] == null) items[slot] = new IconData(StatusIcon.class, item, null, null);
            else items[slot].getItems()[0] = item;
        } else if (icon.equals(StatusIcon.NotReadyIcon.class)) {
            if (items[slot] == null) items[slot] = new IconData(StatusIcon.class, null, item, null);
            else items[slot].getItems()[1] = item;
        } else if (icon.equals(StatusIcon.ReadyIcon.class)) {
            if (items[slot] == null) items[slot] = new IconData(StatusIcon.class, null, null, item);
            else items[slot].getItems()[2] = item;
        } else if (icon.equals(ShowStatusIcon.ShowNotReadyIcon.class)) {
            if (items[slot] == null) items[slot] = new IconData(ShowStatusIcon.class, item, null);
            else items[slot].getItems()[0] = item;
        } else if (icon.equals(ShowStatusIcon.ShowReadyIcon.class)) {
            if (items[slot] == null) items[slot] = new IconData(ShowStatusIcon.class, null, item);
            else items[slot].getItems()[1] = item;
        } else if (item != null) items[slot] = new IconData(icon, item);
        else items[slot] = new IconData(icon);
    }

    @Nullable
    private static Class<? extends TradeIcon> convertFunction(@NotNull String function) {
        boolean usingMoney = TradeSystem.getInstance().getOldConfig().getBoolean("TradeSystem.Trade_with_money", true);

        switch (function.toUpperCase()) {
            case "DECORATION":
                return DecorationIcon.class;

            case "MONEY_REPLACEMENT":
                if (usingMoney) return null;
                else return DecorationIcon.class;

            case "PICK_MONEY":
                if (usingMoney) return ExpLevelIcon.class;
                else return null;

            case "SHOW_MONEY":
                if (usingMoney) return ShowExpLevelIcon.class;
                else return null;

            case "PICK_STATUS_NONE":
                return StatusIcon.CannotReadyIcon.class;

            case "PICK_STATUS_NOT_READY":
                return StatusIcon.NotReadyIcon.class;

            case "PICK_STATUS_READY":
                return StatusIcon.ReadyIcon.class;

            case "SHOW_STATUS_NOT_READY":
                return ShowStatusIcon.ShowNotReadyIcon.class;

            case "SHOW_STATUS_READY":
                return ShowStatusIcon.ShowReadyIcon.class;

            case "CANCEL":
                return CancelIcon.class;

            case "EMPTY_FIRST_TRADER":
                return TradeSlot.class;

            case "EMPTY_SECOND_TRADER":
                return TradeSlotOther.class;

            default:
                throw new IllegalStateException("Cannot convert old function '" + function + "' into a TradeIcon.");
        }
    }

}
