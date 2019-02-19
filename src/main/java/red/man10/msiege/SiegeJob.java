package red.man10.msiege;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import red.man10.msiege.util.InvListener;
import red.man10.msiege.util.InventoryAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class SiegeJob {
    SiegeData data;
    List<JobData> joblist = new ArrayList<>();
    HashMap<UUID,String> selectjob = new HashMap<>();

    public SiegeJob(SiegeData data){
        this.data = data;
    }

    public void jobSelectInvOpen(Player p){
        InventoryAPI inv = new InventoryAPI(data.plugin,"§d§lM§7§lSiege §c§l役職設定 Page1",54);
        jobSelectInv(inv,p,1,true);
    }

    public void jobSelectInv(InventoryAPI inv,Player p,int page,boolean firstopen){
        inv.clear();
        inv.updateTitle(p,"§d§lM§7§lSiege §c§l役職設定 Page"+page);
        //ここに設定のことをかくぅー
        int pos = 0;
        int skip = 0;
        if(page>1){
            int pages = page -1;
            skip = pages*45;
        }
        for(JobData job:joblist){
            if(skip!=0){
                skip--;
                continue;
            }
            ItemStack item = job.jobitem.get(0).clone();
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§f§l"+job.name);
            item.setItemMeta(meta);
            inv.setItem(pos,item);
            pos++;
            if(pos==44){
                break;
            }
        }
        inv.setItems(new int[]{46,47,48,49,50,51,52},new ItemStack(Material.STAINED_GLASS_PANE,1,(short)7));
        inv.setItem(45,inv.createUnbitem("§f§l戻る",new String[]{"§eページを一つ戻ります"},Material.DIAMOND_HOE,962,false));
        inv.setItem(53,inv.createUnbitem("§f§l次へ",new String[]{"§eページを一つ進めます"},Material.DIAMOND_HOE,963,false));
        if(page==1){
            inv.setItem(45,new ItemStack(Material.STAINED_GLASS_PANE,1,(short)7));
        }
        if(pos!=44){
            inv.setItem(53,new ItemStack(Material.STAINED_GLASS_PANE,1,(short)7));
        }
        inv.addOriginalListing(new InvListener(data.plugin, inv) {
            private boolean ontimepass = !firstopen;
            @EventHandler
            public void onClick(InventoryClickEvent e) {
                if (!super.ClickCheck(e)) {
                    return;
                }
                e.setCancelled(true);
                if(e.getInventory().getItem(e.getSlot())==null){
                    return;
                }
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                if(e.getSlot()==45){
                    if(e.getInventory().getItem(45).getType()!=Material.DIAMOND_HOE){
                        return;
                    }
                    jobSelectInv(inv,p,page-1,false);
                    return;
                }else if(e.getSlot()==53){
                    if(e.getInventory().getItem(53).getType()!=Material.DIAMOND_HOE){
                        return;
                    }
                    jobSelectInv(inv,p,page+1,false);
                    return;
                }else if(e.getSlot()>=46&&e.getSlot()<=52){
                    return;
                }
                Bukkit.dispatchCommand(e.getWhoClicked(), "msi jobset " + e.getInventory().getItem(e.getSlot()).getItemMeta().getDisplayName().replaceFirst("§f§l", ""));
                super.unregister();
            }
            @EventHandler
            public void onClose(InventoryCloseEvent e){
                if(ontimepass){
                    ontimepass = false;
                    return;
                }
                super.closeCheck(e);
            }
        });
        if(firstopen) {
            inv.openInv(p);
        }else{
            inv.allListenerRegist(p);
        }
        inv.refresh(p);
    }



}
