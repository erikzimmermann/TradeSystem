package de.codingair.tradesystem.spigot.trade.gui.layout.types.gui;

import de.codingair.codingapi.player.gui.inventory.v2.GUI;
import de.codingair.codingapi.player.gui.inventory.v2.buttons.Button;
import de.codingair.codingapi.player.gui.inventory.v2.buttons.GUISwitchButton;
import de.codingair.codingapi.player.gui.inventory.v2.buttons.Item;
import de.codingair.codingapi.tools.Call;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.trade.Trade;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.*;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.feedback.IconResult;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.utils.IconState;
import de.codingair.tradesystem.spigot.trade.gui.layout.types.utils.TriFunction;
import de.codingair.tradesystem.spigot.trade.gui.layout.utils.Perspective;
import de.codingair.tradesystem.spigot.utils.FloodgateUtils;
import de.codingair.tradesystem.spigot.utils.Lang;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class FloodgateGUIIcon<G> implements TradeIcon, Clickable, StateHolder, Input<G>, ItemPrepareIcon {
    private final IconState state = new IconState();
    private final ItemStack item;

    public FloodgateGUIIcon(@NotNull ItemStack itemStack) {
        this.item = itemStack;
    }

    @Override
    public boolean isDisabled() {
        return state.isDisabled();
    }

    @Override
    public void enable() {
        state.enable();
    }

    @Override
    public void disable(@Nullable TriFunction<Trade, Perspective, Player, String> onClickMessage) {
        state.disable(onClickMessage);
    }

    @Override
    public @NotNull Button getButton(@NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer) {
        if (!FloodgateUtils.isBedrockPlayer(viewer))
            return new Item(prepareItemStack(new ItemBuilder(item), trade, perspective, viewer).getItem());

        return new FloodgateButton() {
            @Override
            public @Nullable ItemStack buildItem() {
                return prepareItemStack(new ItemBuilder(item), trade, perspective, viewer).getItem();
            }

            @Override
            public boolean canClick(ClickType clickType) {
                return state.checkState(trade, perspective, viewer) && isClickable(trade, perspective, viewer);
            }

            @Override
            public void onClick(GUI gui, InventoryClickEvent inventoryClickEvent) {
                trade.acknowledgeGuiSwitch(viewer);  // fixes dupe glitch
            }

            @Override
            public boolean open(ClickType clickType, GUI gui, Call call) {
                viewer.closeInventory();
                this.open(viewer, trade, perspective, viewer, call, gui);
                return false;
            }
        };
    }

    protected void handleResult(@NotNull TradeIcon icon, @NotNull GUI gui, @NotNull IconResult result, @NotNull Trade trade, @NotNull Perspective perspective, @NotNull Perspective viewer) {
        trade.handleClickResult(icon, perspective, viewer, gui, result);
    }

    private abstract class FloodgateButton extends Button implements GUISwitchButton {

        @Override
        public boolean canSwitch(ClickType clickType) {
            return true;
        }

        protected void open(Player player, @NotNull Trade trade, @NotNull Perspective perspective, @NotNull Player viewer, Call call, GUI gui) {
            Perspective viewPerspective = trade.getPerspective(viewer);

            String def;
            if (isEmpty()) def = "";
            else def = makeString(trade, perspective, viewer, getValue(), true);

            FloodgateApi.getInstance().sendForm(player.getUniqueId(), CustomForm.builder()
                    .title(Lang.get("GUI_Title", viewer, new Lang.P("player", trade.getNames()[perspective.isPrimary() ? 1 : 0])))
                    .input(Lang.get("Economy_Offer_Title", viewer), "0", def)
                    .validResultHandler(response -> {
                        String input = response.asInput();
                        if (input == null) input = "";

                        G processed = FloodgateGUIIcon.this.convertInput(input);
                        IconResult result = processInput(trade, perspective, viewer, processed, input);

                        this.getClickSound().play(viewer);

                        if (result == IconResult.GUI) {
                            // use bukkit runnable to see the warning, which is why we re-open this form
                            Bukkit.getScheduler().runTaskLater(TradeSystem.getInstance(),
                                    () -> this.open(player, trade, perspective, viewer, call, gui),
                                    40
                            );
                        } else {
                            FloodgateGUIIcon.this.handleResult(FloodgateGUIIcon.this, gui, result, trade, perspective, viewPerspective);
                            call.proceed();
                        }
                    })
                    .closedOrInvalidResultHandler(() -> this.open(player, trade, perspective, viewer, call, gui))
            );
        }
    }
}
