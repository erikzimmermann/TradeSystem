package de.codingair.tradesystem.spigot.extras.external.vault;

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

public class VaultDependency implements PluginDependency, Listener {

    @EventHandler
    public void onIconInitialize(TradeIconInitializeEvent e) {
        try {
            e.registerIcon(TradeSystem.getInstance(), VaultIcon.class, new EditorInfo("Vault icon", Type.ECONOMY, (editor) -> new ItemBuilder(XMaterial.GOLD_NUGGET), false, "Vault"));
            e.registerIcon(TradeSystem.getInstance(), ShowVaultIcon.class, new TransitionTargetEditorInfo("Vault preview icon", VaultIcon.class));
        } catch (TradeIconException ex) {
            throw new RuntimeException(ex);
        }
    }

    @EventHandler
    public void onPatternRegistration(TradePatternRegistrationEvent e) {
        e.addPattern(new DefaultVaultPattern());
    }

    @Override
    public @NotNull String getPluginName() {
        return "Vault";
    }
}
