package de.codingair.tradesystem.spigot.extras.external.tokenmanager;

import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import de.codingair.tradesystem.spigot.TradeSystem;
import de.codingair.tradesystem.spigot.events.TradeIconInitializeEvent;
import de.codingair.tradesystem.spigot.extras.external.PluginDependency;
import de.codingair.tradesystem.spigot.trade.gui.layout.registration.EditorInfo;
import de.codingair.tradesystem.spigot.trade.gui.layout.registration.TransitionTargetEditorInfo;
import de.codingair.tradesystem.spigot.trade.gui.layout.registration.Type;
import de.codingair.tradesystem.spigot.trade.gui.layout.registration.exceptions.TradeIconException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class TokenManagerDependency implements PluginDependency, Listener {

    @EventHandler
    public void onIconInitialize(TradeIconInitializeEvent e) {
        try {
            e.registerIcon(TradeSystem.getInstance(), TokenIcon.class, new EditorInfo("TokenManager icon", Type.ECONOMY, (editor) -> new ItemBuilder(XMaterial.TRIPWIRE_HOOK), false, "TokenManager"));
            e.registerIcon(TradeSystem.getInstance(), ShowTokenIcon.class, new TransitionTargetEditorInfo("TokenManager preview icon", TokenIcon.class));
        } catch (TradeIconException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public @NotNull String getPluginName() {
        return "TokenManager";
    }
}
