
import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

public class TradeCompleteEvent extends Event implements Cancellable {

    private final Player player1;
    private final Player player2;
    private final List<ItemStack> player1Items;
    private final List<ItemStack> player2Items;

    public TradeCompleteEvent(Player player1, Player player2, List<ItemStack> player1Items, List<ItemStack> player2Items) {
        this.player1 = player1;
        this.player2 = player2;
        this.player1Items = player1Items;
        this.player2Items = player2Items;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public List<ItemStack> getPlayer1TradeItems() {
        return player1Items;
    }

    public void setPlayer1TradeItems(List<ItemStack> player1Items) {
        this.player1Items = player1Items;
    }

    public List<ItemStack> getPlayer2TradeItems() {
        return player2Items;
    }

    public void setPlayer2TradeItems(List<ItemStack> player2Items) {
        this.player2Items = player2Items;
    }

    
    

    private static final HandlerList HANDLERS = new HandlerList();
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}