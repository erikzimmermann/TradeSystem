package de.codingair.tradesystem.spigot.trade;

import de.codingair.codingapi.API;
import de.codingair.codingapi.player.gui.anvil.AnvilGUI;
import de.codingair.codingapi.player.gui.inventory.PlayerInventory;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.AlreadyOpenedException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.IsWaitingException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.NoPageException;
import de.codingair.tradesystem.spigot.trade.gui.TradingGUI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
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
    protected void updateDisplayItem(int id, int slotId, @Nullable ItemStack item) {
        guis[id].setItem(otherSlots.get(slotId), item);
    }

    @Override
    protected @Nullable ItemStack getCurrentDisplayedItem(int id, int slotId) {
        return guis[id].getItem(otherSlots.get(slotId));
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
    protected @Nullable Player getPlayer(int id) {
        return players[id];
    }

    @Override
    protected void onItemPickUp(@NotNull Player player, int id) {
        cancelItemOverflow(players[getOtherId(id)], id);
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
    protected boolean isInitiator(@NotNull Player player, int id) {
        return id == 0;
    }

    @Override
    protected @NotNull PlayerInventory getPlayerInventory(int playerId) {
        return new PlayerInventory(players[playerId], true);
    }

    @Override
    protected @Nullable ItemStack removeReceivedItem(int id, int slotId) {
        int oId = getOtherId(id);
        int slot = slots.get(slotId);

        ItemStack item = guis[oId].getItem(slot);
        guis[oId].setItem(slot, null);

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
    protected void onReadyStateChange(int id, boolean ready) {
        // ignore
    }
}
