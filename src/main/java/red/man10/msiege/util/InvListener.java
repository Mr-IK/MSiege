package red.man10.msiege.util;

import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public abstract class InvListener implements Listener {
    private JavaPlugin plugin;
    private InventoryAPI inv;
    private UUID player;
    private String unique;

    public InvListener(JavaPlugin plugin,InventoryAPI inv){
        this.plugin = plugin;
        this.inv = inv;
        this.unique = inv.getInvUniqueID();
    }

    public void register(UUID uuid){
        this.player = uuid;
        plugin.getServer().getPluginManager().registerEvents(this,plugin);
    }

    public boolean ClickCheck(InventoryClickEvent e){
        if(e.getClickedInventory().equals(e.getWhoClicked().getInventory())){
            return false;
        }
        if(!e.getWhoClicked().getUniqueId().equals(player)){
            return false;
        }
        return inv.getName().contains(unique);
    }
}
