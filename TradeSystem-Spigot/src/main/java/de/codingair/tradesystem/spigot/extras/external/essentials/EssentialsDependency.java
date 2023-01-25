package de.codingair.tradesystem.spigot.extras.external.essentials;

import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.events.TradeIconInitializeEvent;
import de.codingair.tradesystem.spigot.events.TradePatternRegistrationEvent;
import de.codingair.tradesystem.spigot.extras.external.PluginDependency;
import de.codingair.tradesystem.spigot.trade.gui.layout.registration.EditorInfo;
import de.codingair.tradesystem.spigot.trade.gui.layout.registration.TransitionTargetEditorInfo;
import de.codingair.tradesystem.spigot.trade.gui.layout.registration.Type;
import de.codingair.tradesystem.spigot.trade.gui.layout.registration.exceptions.TradeIconException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class EssentialsDependency implements PluginDependency, Listener {

    @EventHandler
    public void onIconInitialize(TradeIconInitializeEvent e) {
        try {
            e.registerIcon(TradeSystem.getInstance(), EssentialsIcon.class, new EditorInfo("Essentials icon", Type.ECONOMY, (editor) -> new ItemBuilder(XMaterial.GOLD_NUGGET), false, getPluginName()));
            e.registerIcon(TradeSystem.getInstance(), ShowEssentialsIcon.class, new TransitionTargetEditorInfo("Essentials preview icon", EssentialsIcon.class));
        } catch (TradeIconException ex) {
            throw new RuntimeException(ex);
        }
    }

    @EventHandler
    public void onPatternRegistration(TradePatternRegistrationEvent e) {
        e.addPattern(new DefaultEssentialsPattern());
    }

    @Override
    public @NotNull String getPluginName() {
        return "Essentials";
    }
}
