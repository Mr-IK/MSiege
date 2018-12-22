package red.man10.msiege;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import red.man10.msiege.util.InventoryAPI;

import java.util.ArrayList;
import java.util.UUID;

public class SiegeCraft implements Listener {

    SiegeData data;
    ArrayList<UUID> playerlist = new ArrayList<>();
    ArrayList<CraftData> datalist = new ArrayList<>();
    public SiegeCraft(SiegeData data){
        this.data = data;
        datareload();
        Bukkit.getServer().getPluginManager().registerEvents(this,data.plugin);
    }

    public void datareload(){
        datalist.clear();
        for(String str:data.craftlist()){
            CraftData cdata = new CraftData(data,str);
            cdata.loadFile();
            datalist.add(cdata);
        }
    }

    @EventHandler
    public void Intract(PlayerInteractEvent e){
        Player player = e.getPlayer();
        if(playerlist.contains(player.getUniqueId())){
            if(e.getAction()==Action.RIGHT_CLICK_BLOCK){
                Block block = e.getClickedBlock();
                if(block.getType() == Material.WORKBENCH) {
                    e.setCancelled(true);
                    data.showMessage(player.getUniqueId().toString(),
                            "§cクラフトは禁止されています");
                    return;
                }else if(block.getType() == Material.CHEST) {
                    e.setCancelled(true);
                    SiegeTeam team = data.tc.getTeam(player);
                    if(team == null){
                        data.showMessage(player.getUniqueId().toString(),"§aゲームに参加していません");
                        return;
                    }
                    team.openteaminv(player);
                    return;
                }else if(block.getType() == Material.ENDER_CHEST) {
                    e.setCancelled(true);
                    SiegeTeam team = data.tc.getTeam(player);
                    if(team == null){
                        data.showMessage(player.getUniqueId().toString(),"§aゲームに参加していません");
                        return;
                    }
                    e.setCancelled(false);
                    return;
                }else if(block.getType() == Material.DISPENSER) {
                    e.setCancelled(true);
                    SiegeTeam team = data.tc.getTeam(player);
                    if(team == null){
                        data.showMessage(player.getUniqueId().toString(),"§aゲームに参加していません");
                        return;
                    }
                    data.cmd.gui.openItemBank(player, new InventoryAPI(data.plugin, "§d§lM§7§lSiege §3§lItemBank", 27),false);
                    return;
                }else if(block.getType() == Material.ENCHANTMENT_TABLE) {
                    e.setCancelled(true);
                    data.cmd.gui.openNexusShop(player,data.tc.getTeam(player),data.getStats(player.getUniqueId().toString()).getJoinarena());
                    return;
                }
            }
            if(e.getAction()==Action.RIGHT_CLICK_AIR||e.getAction()==Action.RIGHT_CLICK_BLOCK){
                for(CraftData data:datalist){
                    if(data.book.equals(player.getInventory().getItemInMainHand())){
                        for(ItemStack item:data.useitems){
                            if(getAmount(player,item)<item.getAmount()){
                                this.data.showMessage(player.getUniqueId().toString(),
                                        "§c材料が足りないためクラフトができません！");
                                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT,2.0f,0.5f);
                                return;
                            }
                        }
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE,2.0f,1.0f);
                        for(ItemStack item:data.useitems){
                            removeAmount(player,item,item.getAmount());
                        }
                        player.getInventory().addItem(data.result);
                        this.data.showMessage(player.getUniqueId().toString(),
                                "§aクラフトを行いました！");
                        break;
                    }
                }
            }

        }
    }


    public static int getAmount(Player arg0, ItemStack arg1) {
        if (arg1 == null)
            return 0;
        int amount = 0;
        for (ItemStack slot : arg0.getInventory().getContents()) {
            if(slot!=null){
                if(slot.getType() ==arg1.getType()){
                    if(arg1.getItemMeta()==null||slot.getItemMeta().equals(arg1.getItemMeta())){
                        amount = amount + slot.getAmount();
                    }
                }
            }

        }
        return amount;
    }

    public static void removeAmount(Player arg0, ItemStack arg1,int i) {
        if (arg1 == null)
            return;
        int amount = i;
        for (ItemStack slot : arg0.getInventory().getContents()) {
            if(slot!=null){
                if(slot.getType() ==arg1.getType()){
                    if(arg1.getItemMeta()==null||slot.getItemMeta().equals(arg1.getItemMeta())){
                        if(amount<slot.getAmount()){
                            slot.setAmount(slot.getAmount()-amount);
                            break;
                        }else{
                            amount = amount - slot.getAmount();
                            slot.setAmount(0);
                        }
                    }
                }
            }

        }
    }

    @EventHandler
    public void Craft(CraftItemEvent event) {
        Player player = (Player) event.getWhoClicked();
        if(playerlist.contains(player.getUniqueId())){
            event.setCancelled(true);
        }
    }
}
