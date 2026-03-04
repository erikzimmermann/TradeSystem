package de.codingair.tradesystem.spigot.trade;

import de.codingair.codingapi.API;
import de.codingair.codingapi.player.gui.anvil.AnvilGUI;
import de.codingair.codingapi.player.gui.inventory.PlayerInventory;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.AlreadyOpenedException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.IsWaitingException;
import de.codingair.codingapi.player.gui.inventory.v2.exceptions.NoPageException;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.gui.TradingGUI;
import de.codingair.tradesystem.spigot.trade.gui.layout.utils.Perspective;
import de.codingair.tradesystem.spigot.utils.CompatibilityUtilPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * Preview trade that allows a single player to view the trade GUI
 * without requiring a second player. Used for testing layouts.
 */
public class PreviewTrade extends Trade {
    private final Player player;
    private final boolean isPreviewMode = true;

    public PreviewTrade(Player player) {
        super(player.getName(), "Preview", true);
        this.player = player;
    }

    @Override
    protected void initializeGUIs() {
        // Only create GUI for the preview player
        this.guis[0] = new TradingGUI(this.player, this, 0);
        this.guis[1] = null; // No second player in preview mode
    }

    @Override
    protected void createGUIs() {
        this.guis[0].prepareStart();
    }

    @Override
    protected void startGUIs() {
        try {
            this.guis[0].open();
        } catch (AlreadyOpenedException | NoPageException | IsWaitingException e) {
            throw new RuntimeException("Cannot open preview GUI.", e);
        }
    }

    @Override
    public void updateDisplayItem(@NotNull Perspective perspective, int slotId, @Nullable ItemStack item) {
        if (guis[0] != null && perspective.id() == 0) {
            guis[0].setItem(otherSlots.get(slotId), item);
        }
    }

    @Override
    public @Nullable ItemStack getCurrentOfferedItem(@NotNull Perspective perspective, int slotId) {
        if (guis[0] != null && perspective.id() == 0) {
            return guis[0].getItem(slots.get(slotId));
        }
        return null;
    }

    @Override
    protected @Nullable ItemStack getCurrentDisplayedItem(@NotNull Perspective perspective, int slotId) {
        if (guis[0] != null && perspective.id() == 0) {
            return guis[0].getItem(otherSlots.get(slotId));
        }
        return null;
    }

    @Override
    protected @NotNull CompletableFuture<Void> markAsInitialized() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    protected void clearOpenAnvils() {
        for (AnvilGUI gui : API.getRemovables(player, AnvilGUI.class)) {
            gui.clearInventory();
        }
    }

    @Override
    public @Nullable Player getPlayer(@NotNull Perspective perspective) {
        // Only return player for primary perspective
        return perspective.id() == 0 ? player : null;
    }

    @Override
    public @NotNull String getWorld(@NotNull Perspective perspective) {
        return player.getWorld().getName();
    }

    @Override
    public @Nullable String getServer(@NotNull Perspective perspective) {
        return TradeSystem.proxy().getServerName();
    }

    @Override
    public @NotNull UUID getUniqueId(@NotNull Perspective perspective) {
        return player.getUniqueId();
    }

    @Override
    protected void onItemPickUp(@NotNull Perspective perspective) {
        // Do nothing in preview mode
    }

    @Override
    protected boolean isActive() {
        return guis[0] != null;
    }

    @Override
    protected boolean isPaused() {
        return pause[0];
    }

    @Override
    protected boolean isInitiator(@NotNull Perspective perspective) {
        return perspective.isPrimary();
    }

    @Override
    protected @NotNull PlayerInventory getPlayerInventory(@NotNull Perspective perspective) {
        PlayerInventory inventory = new PlayerInventory(player, true);

        if (inventory.getPlayer() != null) {
            ItemStack item = CompatibilityUtilPlayer.getCursor(inventory.getPlayer());
            if (item != null && item.getType() != Material.AIR) inventory.addItem(item);
        }

        return inventory;
    }

    @Override
    public void synchronizePlayerInventory(@NotNull Perspective perspective) {
        // Inventories are always synchronized in preview mode
    }

    @Override
    protected @Nullable ItemStack removeReceivedItem(@NotNull Perspective perspective, int slotId) {
        // In preview mode, don't actually remove items
        return null;
    }

    @Override
    protected @NotNull CompletableFuture<Boolean> canFinish() {
        // Preview mode cannot finish normally
        return CompletableFuture.completedFuture(false);
    }

    @Override
    protected @NotNull Stream<Player> getParticipants() {
        return Stream.of(player);
    }

    @Override
    protected void onReadyStateChange(@NotNull Perspective perspective, boolean ready) {
        // Ignore in preview mode
    }

    /**
     * @return true if this trade is in preview mode
     */
    public boolean isPreview() {
        return isPreviewMode;
    }
}
