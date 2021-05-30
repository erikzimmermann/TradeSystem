package de.codingair.tradesystem.spigot.trade.layout.types.utils;

import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.utils.Lang;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SimpleSignGUIIcon<G> extends SignGUIIcon<G> {
    public SimpleSignGUIIcon(@NotNull ItemStack itemStack) {
        super(itemStack);
    }

    @Override
    public @Nullable String[] buildSignLines(@NotNull Trade trade, @NotNull Player player) {
        String input = makeString(getValue());

        String[] text = new String[4];
        text[0] = input;

        String[] description = Lang.get("Sign_Enter_Amount").split("\n", 3);
        System.arraycopy(description, 0, text, 1, 3);

        return text;
    }
}
