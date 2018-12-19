package red.man10.msiege;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import red.man10.msiege.util.InvListener;
import red.man10.msiege.util.InventoryAPI;

import java.util.ArrayList;
import java.util.List;

public class GUIManager implements Listener {

    private SiegeData data;

    public GUIManager(SiegeData data){
        this.data = data;
        Bukkit.getServer().getPluginManager().registerEvents(this,data.plugin);
    }

    public void openBattle(Player p) {
        InventoryAPI inv = new InventoryAPI(data.plugin, data.plugin.prefix, 27);
        inv.setItem(11,inv.createUnbitem("§6§lチームチェスト",
                new String[]{"§eチームメイトと共有のチェストです！","§eクリックで開く"}, Material.CHEST,0,true));
        inv.addOriginalListing(new InvListener(data.plugin,inv){
            @EventHandler
            public void onClick(InventoryClickEvent e){
                if(!super.ClickCheck(e)){
                    return;
                }
                if(e.getSlot()!=11){
                    return;
                }
                p.playSound(p.getLocation(), Sound.BLOCK_CHEST_OPEN,1.0f,1.0f);
                e.setCancelled(true);
                inv.regenerateID();
                super.unregister();
                p.closeInventory();
                SiegeTeam team = data.tc.getTeam(p);
                if(team!=null){
                    team.openteaminv(p);
                }
            }
            @EventHandler
            public void onClose(InventoryCloseEvent e){
                super.unregister();
            }
        });

        inv.setItem(13,inv.createUnbitem("§5§lマイチェスト",
                new String[]{"§e自分用のチェストです！","§eクリックで開く"}, Material.ENDER_CHEST,0,true));
        inv.addOriginalListing(new InvListener(data.plugin,inv){
            @EventHandler
            public void onClick(InventoryClickEvent e){
                if(!super.ClickCheck(e)){
                    return;
                }
                if(e.getSlot()!=13){
                    return;
                }
                p.playSound(p.getLocation(), Sound.BLOCK_ENDERCHEST_OPEN,1.0f,1.0f);
                e.setCancelled(true);
                inv.regenerateID();
                super.unregister();
                p.closeInventory();
                SiegeTeam team = data.tc.getTeam(p);
                if(team != null){
                    p.openInventory(p.getEnderChest());
                }
            }
            @EventHandler
            public void onClose(InventoryCloseEvent e){
                super.unregister();
            }
        });
        inv.setItem(15,inv.createUnbitem("§3§lアイテムバンク",
                new String[]{"§eman10と共通のアイテムバンクからアイテムを取り出せます","§eクリックで開く"}, Material.DISPENSER,0,true));
        inv.addOriginalListing(new InvListener(data.plugin,inv){
            @EventHandler
            public void onClick(InventoryClickEvent e){
                if(!super.ClickCheck(e)){
                    return;
                }
                if(e.getSlot()!=15){
                    return;
                }
                p.playSound(p.getLocation(), Sound.BLOCK_CHEST_OPEN,1.0f,1.5f);
                e.setCancelled(true);
                inv.regenerateID();
                super.unregister();
                p.closeInventory();
                SiegeTeam team = data.tc.getTeam(p);
                if(team != null){
                    openItemBank(p, new InventoryAPI(data.plugin, "§d§lM§7§lSiege §3§lItemBank", 27),false);
                }
            }
            @EventHandler
            public void onClose(InventoryCloseEvent e){
                super.unregister();
            }
        });
        inv.openInv(p);
    }

    public void openItemBank(Player p,InventoryAPI inv,boolean update) {
        inv.fillInv(new ItemStack(Material.STAINED_GLASS_PANE,1,(short)7));
        inv.setItems(new int[]{10,11,12,13,14,15,16},new ItemStack(Material.AIR));
        inv.setItem(10,inv.createUnbitem("§f§l羊毛",
                new String[]{"§a"+data.ibm.getItemStorage(p.getUniqueId().toString(),8).amount+"個所持しています","§f左クリックで1個","§f右クリックで1スタック 取り出します"}, Material.WOOL,0,false));
        inv.setItem(11,inv.createUnbitem("§7§l丸石",
                new String[]{"§a"+data.ibm.getItemStorage(p.getUniqueId().toString(),1).amount+"個所持しています","§f左クリックで1個","§f右クリックで1スタック 取り出します"}, Material.COBBLESTONE,0,false));
        inv.addOriginalListing(new InvListener(data.plugin,inv){
            @EventHandler
            public synchronized void onClick(InventoryClickEvent e){
                if(!super.ClickCheck(e)){
                    return;
                }
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                e.setCancelled(true);
                if(e.getSlot()==10){
                    SiegeTeam team = data.tc.getTeam(p);
                    if(team!=null){
                        if(e.getClick().isLeftClick()) {
                            Bukkit.getScheduler().runTaskAsynchronously(data.plugin, () -> {
                                if(data.ibm.reduceItem(p.getUniqueId().toString(),8,1)){
                                    p.getInventory().addItem(new ItemStack(Material.WOOL));
                                    openItemBank(p,inv,true);
                                }else{
                                    data.showMessage(p.getUniqueId().toString(),"§c§l引き出しに失敗しました");
                                }
                            });
                        }else if(e.getClick().isRightClick()) {
                            Bukkit.getScheduler().runTaskAsynchronously(data.plugin, () -> {
                                if(data.ibm.reduceItem(p.getUniqueId().toString(),8,64)){
                                    p.getInventory().addItem(new ItemStack(Material.WOOL,64));
                                    openItemBank(p,inv,true);
                                }else{
                                    data.showMessage(p.getUniqueId().toString(),"§c§l引き出しに失敗しました");
                                }
                            });
                        }
                    }
                }else if(e.getSlot()==11) {
                    SiegeTeam team = data.tc.getTeam(p);
                    if (team != null) {
                        if (e.getClick().isLeftClick()) {
                            Bukkit.getScheduler().runTaskAsynchronously(data.plugin, () -> {
                                if (data.ibm.reduceItem(p.getUniqueId().toString(), 1, 1)) {
                                    p.getInventory().addItem(new ItemStack(Material.COBBLESTONE));
                                    openItemBank(p,inv,true);
                                } else {
                                    data.showMessage(p.getUniqueId().toString(), "§c§l引き出しに失敗しました");
                                }
                            });
                        } else if (e.getClick().isRightClick()) {
                            Bukkit.getScheduler().runTaskAsynchronously(data.plugin, () -> {
                                if (data.ibm.reduceItem(p.getUniqueId().toString(), 1, 64)) {
                                    p.getInventory().addItem(new ItemStack(Material.COBBLESTONE, 64));
                                    openItemBank(p,inv,true);
                                } else {
                                    data.showMessage(p.getUniqueId().toString(), "§c§l引き出しに失敗しました");
                                }
                            });
                        }
                    }
                }
            }
            @EventHandler
            public void onClose(InventoryCloseEvent e){
                if(e.getPlayer().getUniqueId()==p.getUniqueId()){
                    super.unregister();
                }
            }
        });
        if(!update){
            inv.openInv(p);
        }else{
            inv.allListenerRegist(p);
        }
    }

    public void openMain(Player p){
        SiegeTeam team = data.tc.getTeam(p);
        if(team != null){
            openBattle(p);
            return;
        }
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
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                e.setCancelled(true);
                inv.regenerateID();
                super.unregister();
                shop((Player)e.getWhoClicked());
            }
            @EventHandler
            public void onClose(InventoryCloseEvent e){
                if(e.getPlayer().getUniqueId()==p.getUniqueId()){
                    super.unregister();
                }
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
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                e.setCancelled(true);
                inv.regenerateID();
                super.unregister();
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
            @EventHandler
            public void onClose(InventoryCloseEvent e){
                if(e.getPlayer().getUniqueId()==p.getUniqueId()){
                    super.unregister();
                }
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
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                e.setCancelled(true);
                e.getWhoClicked().closeInventory();
                super.unregister();
                Bukkit.dispatchCommand(e.getWhoClicked(),"msi stats");
            }
            @EventHandler
            public void onClose(InventoryCloseEvent e){
                if(e.getPlayer().getUniqueId()==p.getUniqueId()){
                    super.unregister();
                }
            }
        });
        inv.openInv(p);
    }

    public void shop(Player p){
        shop(new InventoryAPI(data.plugin,data.plugin.prefix,54),p,false);
    }

    public void shop(InventoryAPI inv,Player p,boolean update){
        inv.clear();
        inv.fillInv(new ItemStack(Material.STAINED_GLASS_PANE,1,(short)4));
        inv.setItems(new int[]{10,11,12,13,14,15,16, 19,20,21,22,23,24,25, 28,29,30,31,32,33,34, 37,38,39,40,41,42,43, 46,47,48,49,50,51,52},new ItemStack(Material.AIR));
        inv.allListenerRegist(p);
        //ここにショップのことをかくぅー
        int pos = 10;
        for(String str : data.plugin.config.getStringList("shop")){
            if(pos == 17){
                pos=19;
            }else if(pos == 26){
                pos=28;
            }else if(pos == 35){
                pos=37;
            }else if(pos == 44){
                pos=46;
            }else if(pos == 53){
                break;
            }
            String card = str.split(" = ")[0];
            int buy;
            try{
                buy =  Integer.parseInt(str.split(" = ")[1]);
            }catch (NumberFormatException ee){
                continue;
            }
            SiegeCard cards = data.getCard(card);
            ItemStack item = cards.item.get(0).clone();
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§f"+cards.name);
            List<String> lore = new ArrayList<>(cards.lore);
            lore.add("§e===================");
            lore.add("§6コスト: §e"+cards.cost);
            lore.add("§6値段: §e"+buy);
            lore.add("§aクリックで購入!");
            if(data.getStats(p.getUniqueId().toString()).getHavecard().contains(card)){
                lore.add("§cこのカードはすでに所持しています！");
            }else{
                lore.add("§aこのカードは所持していません！");
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(pos,item);
            pos++;
        }
        inv.addOriginalListing(new InvListener(data.plugin,inv) {
            @EventHandler
            public void onClick(InventoryClickEvent e) {
                if (!super.ClickCheck(e)) {
                    return;
                }
                e.setCancelled(true);
                if (e.getSlot() >= 10&&e.getSlot() <=16||e.getSlot() >= 19&&e.getSlot() <=25||e.getSlot() >= 28&&e.getSlot() <=34||e.getSlot() >= 37&&e.getSlot() <=43||e.getSlot() >= 46&&e.getSlot() <=52) {
                    if(e.getInventory().getItem(e.getSlot())==null){
                        return;
                    }
                    String name = e.getInventory().getItem(e.getSlot()).getItemMeta().getDisplayName().replaceFirst("§f", "");
                    String buys = e.getInventory().getItem(e.getSlot()).getItemMeta().getLore().
                            get(e.getInventory().getItem(e.getSlot()).getItemMeta().getLore().size()-3).replace("§6値段: §e","");
                    int buy;
                    try{
                        buy = Integer.parseInt(buys);
                    }catch (NumberFormatException ee){
                        data.showMessage(p.getUniqueId().toString(),"§c不明なエラーが発生しました。");
                        return;
                    }
                    if(data.getStats(p.getUniqueId().toString()).getHavecard().contains(name)){
                        data.showMessage(p.getUniqueId().toString(),"§cこのカードはすでに所持しています!");
                        return;
                    }
                    if(data.getStats(p.getUniqueId().toString()).getPoint()<buy){
                        data.showMessage(p.getUniqueId().toString(),"§cポイントが足りません！ 所持ポイント: "+
                                data.getStats(p.getUniqueId().toString()).getPoint()+" 必要ポイント: "+buy);
                        return;
                    }
                    data.getStats(p.getUniqueId().toString()).addhavecard(name);
                    data.getStats(p.getUniqueId().toString()).setPoint(data.getStats(p.getUniqueId().toString()).getPoint()-buy);
                    data.showMessage(p.getUniqueId().toString(),"§aカード「"+name+"」を購入しました。 所持ポイント: "+data.getStats(p.getUniqueId().toString()).getPoint());
                    data.getStats(p.getUniqueId().toString()).saveFile();
                    super.unregister();
                    shop(p);
                }
            }
            @EventHandler
            public void onClose(InventoryCloseEvent e){
                if(e.getPlayer().getUniqueId()==p.getUniqueId()){
                    super.unregister();
                }
            }
        });
        if(update){
            inv.allListenerRegist(p);
        }else{
            inv.openInv(p);
        }
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
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                e.setCancelled(true);
                super.unregister();
                settingPage(new InventoryAPI(data.plugin,"§d§lM§7§lSiege §3§lカード設定 Page1",54),p,1,true);
            }
            @EventHandler
            public void onClose(InventoryCloseEvent e){
                if(e.getPlayer().getUniqueId()==p.getUniqueId()){
                    super.unregister();
                }
            }
        });
        inv.allListenerRegist(p);
    }

    public void settingPage(InventoryAPI inv,Player p,int page,boolean invnull){
        inv.clear();
        inv.updateTitle(p,"§d§lM§7§lSiege §3§lカード設定 Page"+page);
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
            if(cards==null){
                data.getStats(p.getUniqueId().toString()).removehavecard(card);
                continue;
            }
            ItemStack item = cards.item.get(0).clone();
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§f"+cards.name);
            List<String> lore = new ArrayList<>(cards.lore);
            lore.add("§e===================");
            lore.add("§6コスト: §e"+cards.cost);
            lore.add("§a左クリックでセット");
            lore.add("§c右クリックでアンセットします！");
            if(data.getStats(p.getUniqueId().toString()).getSelectcard().contains(card)){
                lore.add("§aこのカードはセットされています！");
            }else{
                lore.add("§cこのカードはセットされていません！");
            }
            meta.setLore(lore);
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
            private boolean ontimepass = true;
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
                    settingPage(inv,p,page-1,false);
                    return;
                }else if(e.getSlot()==53){
                    if(e.getInventory().getItem(53).getType()!=Material.DIAMOND_HOE){
                        return;
                    }
                    settingPage(inv,p,page+1,false);
                    return;
                }else if(e.getSlot()>=46&&e.getSlot()<=52){
                    return;
                }
                if(e.getClick().isLeftClick()) {
                    Bukkit.dispatchCommand(e.getWhoClicked(), "msi cardset " + e.getInventory().getItem(e.getSlot()).getItemMeta().getDisplayName().replaceFirst("§f", ""));
                }else if(e.getClick().isRightClick()) {
                    Bukkit.dispatchCommand(e.getWhoClicked(), "msi cardunset " + e.getInventory().getItem(e.getSlot()).getItemMeta().getDisplayName().replaceFirst("§f", ""));
                }
                super.unregister();
                settingPage(inv,p,page,false);
            }
            @EventHandler
            public void onClose(InventoryCloseEvent e){
                if(ontimepass){
                    ontimepass = false;
                    return;
                }
                if(e.getPlayer().getUniqueId()==p.getUniqueId()){
                    super.unregister();
                }
            }
        });
        if(invnull) {
            inv.openInv(p);
        }else{
            inv.allListenerRegist(p);
        }
        inv.refresh(p);
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
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                e.setCancelled(true);
                super.unregister();
                droppingPage(new InventoryAPI(data.plugin,"§d§lM§7§lSiege §8§lゴミ捨て場 Page1",54),p,1,true);
            }
            @EventHandler
            public void onClose(InventoryCloseEvent e){
                if(e.getPlayer().getUniqueId()==p.getUniqueId()){
                    super.unregister();
                }
            }
        });
        inv.allListenerRegist(p);
    }

    public void droppingPage(InventoryAPI inv,Player p,int page,boolean invnull){
        inv.clear();
        inv.updateTitle(p,"§d§lM§7§lSiege §8§lゴミ捨て場 Page"+page);
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
            if(cards==null){
                data.getStats(p.getUniqueId().toString()).removehavecard(card);
                continue;
            }
            ItemStack item = cards.item.get(0).clone();
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§f"+cards.name);
            List<String> lore = new ArrayList<>(cards.lore);
            lore.add("§e===================");
            lore.add("§cクリックで捨てます！");
            if(data.getStats(p.getUniqueId().toString()).getSelectcard().contains(card)){
                lore.add("§4§l注意!! このカードはセットされています！");
            }else{
                lore.add("§cこのカードはセットされていません");
            }
            meta.setLore(lore);
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
        if(page ==1){
            inv.setItem(45,new ItemStack(Material.STAINED_GLASS_PANE,1,(short)7));
        }
        if(pos!=44){
            inv.setItem(53,new ItemStack(Material.STAINED_GLASS_PANE,1,(short)7));
        }
        inv.addOriginalListing(new InvListener(data.plugin, inv) {
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
                    droppingPage(inv,p,page-1,false);
                    return;
                }else if(e.getSlot()==53){
                    if(e.getInventory().getItem(53).getType()!=Material.DIAMOND_HOE){
                        return;
                    }
                    droppingPage(inv,p,page+1,false);
                    return;
                }
                Bukkit.dispatchCommand(e.getWhoClicked(), "msi carddrop " + e.getInventory().getItem(e.getSlot()).getItemMeta().getDisplayName().replaceFirst("§f", ""));
                super.unregister();
                droppingPage(inv,p,page,false);
            }

            private boolean ontimepass = true;

            @EventHandler
            public void onClose(InventoryCloseEvent e){
                if(ontimepass){
                    ontimepass = false;
                    return;
                }
                if(e.getPlayer().getUniqueId()==p.getUniqueId()){
                    super.unregister();
                }
            }
        });
        if(invnull) {
            inv.openInv(p);
        }else{
            inv.allListenerRegist(p);
        }
        inv.refresh(p);
    }

}
