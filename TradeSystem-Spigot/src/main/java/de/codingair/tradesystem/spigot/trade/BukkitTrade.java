package de.codingair.tradesystem.spigot.trade;

import de.codingair.codingapi.API;
import de.codingair.codingapi.player.gui.anvil.AnvilGUI;
import de.codingair.codingapi.player.gui.inventory.PlayerInventory;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.AlreadyOpenedException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.IsWaitingException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.NoPageException;
import de.codingair.tradesystem.spigot.trade.gui.TradingGUI;
import de.codingair.tradesystem.spigot.trade.gui.layout.utils.Perspective;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class BukkitTrade extends Trade {
    private final Player[] players = new Player[2];

    BukkitTrade(Player p0, Player p1, boolean initiationServer) {
        super(p0.getName(), p1.getName(), initiationServer);
        this.players[0] = p0;
        this.players[1] = p1;
    }

    @Override
    protected void initializeGUIs() {
        this.guis[0] = new TradingGUI(this.players[0], this, 0);
        this.guis[1] = new TradingGUI(this.players[1], this, 1);
    }

    @Override
    protected void startGUI() {
        this.guis[0].prepareStart();
        this.guis[1].prepareStart();

        try {
            this.guis[0].open();
            this.guis[1].open();
        } catch (AlreadyOpenedException | NoPageException | IsWaitingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void updateDisplayItem(@NotNull Perspective perspective, int slotId, @Nullable ItemStack item) {
        guis[perspective.id()].setItem(otherSlots.get(slotId), item);
    }

    @Override
    protected @Nullable ItemStack getCurrentOfferedItem(@NotNull Perspective perspective, int slotId) {
        return guis[perspective.id()].getItem(slots.get(slotId));
    }

    @Override
    protected @Nullable ItemStack getCurrentDisplayedItem(@NotNull Perspective perspective, int slotId) {
        return guis[perspective.id()].getItem(otherSlots.get(slotId));
    }

    @Override
    protected void clearOpenAnvils() {
        for (Player player : this.players) {
            for (AnvilGUI gui : API.getRemovables(player, AnvilGUI.class)) {
                gui.clearInventory();
            }
        }
    }

    @Override
    public @Nullable Player getPlayer(@NotNull Perspective perspective) {
        return players[perspective.id()];
    }

    @Override
    public @NotNull UUID getUniqueId(@NotNull Perspective perspective) {
        return players[perspective.id()].getUniqueId();
    }

    @Override
    protected void onItemPickUp(@NotNull Perspective perspective) {
        cancelItemOverflow(perspective.flip());
    }

    @Override
    protected boolean isActive() {
        return guis[0] != null && guis[1] != null;
    }

    @Override
    protected boolean isPaused() {
        return pause[0] && pause[1];
    }

    @Override
    protected boolean isInitiator(@NotNull Perspective perspective) {
        return perspective.isPrimary();
    }

    @Override
    protected @NotNull PlayerInventory getPlayerInventory(@NotNull Perspective perspective) {
        PlayerInventory inventory = new PlayerInventory(players[perspective.id()], true);

        if (inventory.getPlayer() != null) {
            ItemStack item = inventory.getPlayer().getOpenInventory().getCursor();
            if (item != null && item.getType() != Material.AIR) inventory.addItem(item);
        }

        return inventory;
    }

    @Override
    protected @Nullable ItemStack removeReceivedItem(@NotNull Perspective perspective, int slotId) {
        int slot = slots.get(slotId);

        ItemStack item = guis[perspective.flip().id()].getItem(slot);
        guis[perspective.flip().id()].setItem(slot, null);

        return item;
    }

    @Override
    protected @NotNull CompletableFuture<Boolean> canFinish() {
        return CompletableFuture.completedFuture(ready[0] && ready[1]);
    }

    @Override
    protected @NotNull Stream<Player> getParticipants() {
        return Arrays.stream(players);
    }

    @Override
    protected void onReadyStateChange(@NotNull Perspective perspective, boolean ready) {
        // ignore
    }
}
