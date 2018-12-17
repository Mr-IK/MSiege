package red.man10.msiege;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import red.man10.msiege.util.InvListener;
import red.man10.msiege.util.InventoryAPI;

import java.util.List;

public class GUIManager implements Listener {

    private SiegeData data;

    public GUIManager(SiegeData data){
        this.data = data;
        Bukkit.getServer().getPluginManager().registerEvents(this,data.plugin);
    }

    public void openMain(Player p){
        InventoryAPI inv = new InventoryAPI(data.plugin,data.plugin.prefix,27);
        inv.setItem(11,inv.createUnbitem("§6§lカードショップ",
                new String[]{"§e新しいカードを手に入れましょう！","§eクリックでSHOPに行く"}, Material.CHEST,0,false));
        inv.addOriginalListing(new InvListener(data.plugin,inv){
            @EventHandler
            public void onClick(InventoryClickEvent e){
                if(!super.ClickCheck(e)){
                    return;
                }
                if(e.getSlot()!=11){
                    return;
                }
                e.setCancelled(true);
                inv.regenerateID();
                inv.updateTitle((Player)e.getWhoClicked(),"§d§lM§7§lSiege §6§lショップ");
                inv.clear();
                inv.allListenerRegist((Player)e.getWhoClicked());
                //ここにショップのことをかくぅー
            }
        });
        inv.setItem(13,inv.createUnbitem("§7§lカード管理",
                new String[]{"§eあなたのカードを管理します！","§eクリックで開く"}, Material.PAPER,0,true));
        inv.addOriginalListing(new InvListener(data.plugin,inv){
            @EventHandler
            public void onClick(InventoryClickEvent e){
                if(!super.ClickCheck(e)){
                    return;
                }
                if(e.getSlot()!=13){
                    return;
                }
                e.setCancelled(true);
                inv.regenerateID();
                inv.updateTitle((Player)e.getWhoClicked(),"§d§lM§7§lSiege §8§lカード管理");
                inv.clear();
                //ここにカード設定のことをかくぅー
                inv.setItem(11,inv.createUnbitem("§7§lカード設定",
                        new String[]{"§eあなたのカードを設定します！","§eクリックで設定開始"}, Material.DIAMOND_HOE,686,false));
                openSetting(inv,p);
                inv.setItem(15,inv.createUnbitem("§c§lカードを捨てる",
                        new String[]{"§cあなたのカードを捨てることができます！","§eクリックでゴミ捨て開始"}, Material.DIAMOND_HOE,860,false));
                openDropping(inv,p);
            }
        });
        inv.setItem(15,inv.createSkullitem("§a§lステータス",
                new String[]{"§e自分のキル数などを確認しよう！","§eクリックでステータスを見る"}, p.getUniqueId(),false));
        inv.addOriginalListing(new InvListener(data.plugin,inv){
            @EventHandler
            public void onClick(InventoryClickEvent e){
                if(!super.ClickCheck(e)){
                    return;
                }
                if(e.getSlot()!=15){
                    return;
                }
                e.setCancelled(true);
                e.getWhoClicked().closeInventory();
                Bukkit.dispatchCommand(e.getWhoClicked(),"msi stats");
            }
        });
        inv.openInv(p);
    }

    private void openSetting(InventoryAPI inv,Player p){
        inv.addOriginalListing(new InvListener(data.plugin,inv){
            @EventHandler
            public void onClick(InventoryClickEvent e){
                if(!super.ClickCheck(e)){
                    return;
                }
                if(e.getSlot()!=11){
                    return;
                }
                e.setCancelled(true);
                settingPage(p,1);
            }
        });
        inv.allListenerRegist(p);
    }

    public void settingPage(Player p,int page){
        InventoryAPI inv = new InventoryAPI(data.plugin,"§d§lM§7§lSiege §3§lカード設定"+page,54);
        inv.clear();
        //ここに設定のことをかくぅー
        int pos = 0;
        int skip = 0;
        if(page>1){
            int pages = page -1;
            skip = pages*45;
        }
        for(String card:data.getStats(p.getUniqueId().toString()).getHavecard()){
            if(skip!=0){
                skip--;
                continue;
            }
            SiegeCard cards = data.getCard(card);
            ItemStack item = cards.item.get(0);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§f"+cards.name);
            List<String> lore = cards.lore;
            lore.add("§e===================");
            lore.add("§a左クリックでセット");
            lore.add("§c右クリックでアンセットします！");
            if(data.getStats(p.getUniqueId().toString()).getSelectcard().contains(card)){
                lore.add("§aこのカードはセットされています！");
            }else{
                lore.add("§cこのカードはセットされていません！");
            }
            meta.setLore(lore);
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
        inv.addOriginalListing(new InvListener(data.plugin,inv) {
            @EventHandler
            public void onClick(InventoryClickEvent e) {
                if (!super.ClickCheck(e)) {
                    return;
                }
                e.setCancelled(true);
                if(e.getSlot()==45){
                    if(e.getInventory().getItem(45).getType()!=Material.DIAMOND_HOE){
                        return;
                    }
                    settingPage(p,page-1);
                    return;
                }else if(e.getSlot()==53){
                    if(e.getInventory().getItem(53).getType()!=Material.DIAMOND_HOE){
                        return;
                    }
                    settingPage(p,page+1);
                    return;
                }
                if(e.getClick().isLeftClick()) {
                    Bukkit.dispatchCommand(e.getWhoClicked(), "msi cardset " + e.getInventory().getItem(e.getSlot()).getItemMeta().getDisplayName().replaceFirst("§f", ""));
                }else if(e.getClick().isRightClick()) {
                    Bukkit.dispatchCommand(e.getWhoClicked(), "msi cardunset " + e.getInventory().getItem(e.getSlot()).getItemMeta().getDisplayName().replaceFirst("§f", ""));
                }
            }
        });
        inv.openInv(p);
    }

    private void openDropping(InventoryAPI inv,Player p){
        inv.addOriginalListing(new InvListener(data.plugin,inv){
            @EventHandler
            public void onClick(InventoryClickEvent e){
                if(!super.ClickCheck(e)){
                    return;
                }
                if(e.getSlot()!=15){
                    return;
                }
                e.setCancelled(true);
                droppingPage(p,1);
            }
        });
        inv.allListenerRegist(p);
    }

    public void droppingPage(Player p,int page){
        InventoryAPI inv = new InventoryAPI(data.plugin,"§d§lM§7§lSiege §8§lゴミ捨て場"+page,54);
        inv.clear();
        //ここに設定のことをかくぅー
        int pos = 0;
        int skip = 0;
        if(page>1){
            int pages = page -1;
            skip = pages*45;
        }
        for(String card:data.getStats(p.getUniqueId().toString()).getHavecard()){
            if(skip!=0){
                skip--;
                continue;
            }
            SiegeCard cards = data.getCard(card);
            ItemStack item = cards.item.get(0);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§f"+cards.name);
            List<String> lore = cards.lore;
            lore.add("§e===================");
            lore.add("§cクリックで捨てます！");
            if(data.getStats(p.getUniqueId().toString()).getSelectcard().contains(card)){
                lore.add("§4§l注意!! このカードはセットされています！");
            }else{
                lore.add("§cこのカードはセットされていません");
            }
            meta.setLore(lore);
            inv.setItem(pos,item);
            pos++;
            if(pos==44){
                break;
            }
        }
        inv.setItems(new int[]{46,47,48,49,50,51,52},new ItemStack(Material.STAINED_GLASS_PANE,1,(short)7));
        inv.setItem(45,inv.createUnbitem("§f§l戻る",new String[]{"§eページを一つ戻ります"},Material.DIAMOND_HOE,962,false));
        inv.setItem(53,inv.createUnbitem("§f§l次へ",new String[]{"§eページを一つ進めます"},Material.DIAMOND_HOE,963,false));
        if(page ==1){
            inv.setItem(45,new ItemStack(Material.STAINED_GLASS_PANE,1,(short)7));
        }
        if(pos!=44){
            inv.setItem(53,new ItemStack(Material.STAINED_GLASS_PANE,1,(short)7));
        }
        inv.addOriginalListing(new InvListener(data.plugin,inv) {
            @EventHandler
            public void onClick(InventoryClickEvent e) {
                if (!super.ClickCheck(e)) {
                    return;
                }
                e.setCancelled(true);
                if(e.getSlot()==45){
                    if(e.getInventory().getItem(45).getType()!=Material.DIAMOND_HOE){
                        return;
                    }
                    settingPage(p,page-1);
                    return;
                }else if(e.getSlot()==53){
                    if(e.getInventory().getItem(53).getType()!=Material.DIAMOND_HOE){
                        return;
                    }
                    settingPage(p,page+1);
                    return;
                }
                Bukkit.dispatchCommand(e.getWhoClicked(), "msi carddrop " + e.getInventory().getItem(e.getSlot()).getItemMeta().getDisplayName().replaceFirst("§f", ""));
            }
        });
        inv.openInv(p);
    }



}
