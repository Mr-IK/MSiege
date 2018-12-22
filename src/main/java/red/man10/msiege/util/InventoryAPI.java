package red.man10.msiege.util;

import net.minecraft.server.v1_12_R1.ChatMessage;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.PacketPlayOutOpenWindow;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

/*
  2018/12/17 Mr_IK 作成。
  インベントリのあらゆる機能を最大限使用できるAPIです
 */

public class InventoryAPI{

    private JavaPlugin plugin;
    private Inventory inv;
    private UUID uniqueInventoryID;
    private ArrayList<InvListener> listeners;
    private String name;


    //init inventory
    public InventoryAPI(JavaPlugin plugin,String name,int size){
        this.plugin = plugin;
        uniqueInventoryID = UUID.randomUUID();
        inv = Bukkit.getServer().createInventory(null, size, getInvUniqueID()+name);
        this.name = getInvUniqueID()+name;
        listeners = new ArrayList<>();
    }

    //updated inv
    public void refresh(Player p){
        p.updateInventory();
    }

    //String message hideen
    private String hideString(String title){
        StringBuilder hiden = new StringBuilder(title);
        String result = title;
        for(int i = 0;i<result.length();i = i+2){
            hiden.insert(i,"§");
            result = new String(hiden);
        }
        return result;
    }

    //event reseting
    public void regenerateID(){
        uniqueInventoryID = UUID.randomUUID();
    }

    //get event original id
    public String getInvUniqueID(){
        return hideString(getUniqueInventoryID().toString());
    }

    //reseting inventory instance
    public void resetInv(String name,int size) {
        inv = Bukkit.getServer().createInventory(null, size, getInvUniqueID()+name);
    }

    //inv fill item
    public void fillInv(ItemStack item){
        for(int i = 0;i<inv.getSize();i++){
            setItem(i,item);
        }
    }


    //event regist and open inv
    public void openInv(Player p){
        allListenerRegist(p);
        p.openInventory(inv);
    }

    //event regist
    public void allListenerRegist(Player p){
        for(InvListener listener:listeners){
            listener.register(p.getUniqueId());
        }
        listeners.clear();
    }


    //inventory clear
    public void clear(){
        inv.clear();
    }

    //updated inventory title
    public void updateTitle(Player p, String title) {
        EntityPlayer ep = ((CraftPlayer)p).getHandle();
        PacketPlayOutOpenWindow packet = new PacketPlayOutOpenWindow(ep.activeContainer.windowId, "minecraft:chest", new ChatMessage(getInvUniqueID()+title), p.getOpenInventory().getTopInventory().getSize());
        ep.playerConnection.sendPacket(packet);
        ep.updateInventory(ep.activeContainer);
        this.name = getInvUniqueID()+title;
    }

    //setting item
    public void setItem(int i, ItemStack item){
        inv.setItem(i,item);
    }

    //get inventory name
    public String getName() {
        return name;
    }

    //get item
    public ItemStack getItem(int i){
        return inv.getItem(i);
    }

    //set item
    public void setItems(int[] i, ItemStack item){
        for(int ii :i){
            inv.setItem(ii,item);
        }
    }

    //listing commands
    public void listingCommands(Player p,int i,String[] commands,boolean itempickup){
        listeners.add(new InvListener(plugin,this){
            @EventHandler
            public void onClick(InventoryClickEvent e){
                if(!super.ClickCheck(e)){
                    return;
                }
                if(e.getSlot()==i){
                    if(!itempickup){
                        e.setCancelled(true);
                    }
                    for(String cmd : commands){
                        if(cmd.startsWith("@")){
                            cmd = cmd.replaceFirst("@","");
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("<player>",p.getName()));
                            continue;
                        }
                        Bukkit.dispatchCommand(p, cmd.replace("<player>",p.getName()));
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
    }

    public void listingCommands(Player p,int[] i,String[] commands,boolean itempickup){
        listeners.add(new InvListener(plugin,this){
            @EventHandler
            public void onClick(InventoryClickEvent e){
                if(!super.ClickCheck(e)){
                    return;
                }
                for(int ii: i){
                    if(e.getSlot()!=ii){
                        return;
                    }
                    if(!itempickup){
                        e.setCancelled(true);
                    }
                    for(String cmd : commands){
                        if(cmd.startsWith("@")){
                            cmd = cmd.replaceFirst("@","");
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                            continue;
                        }
                        Bukkit.dispatchCommand(e.getWhoClicked(), cmd);
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
    }

    public void addOriginalListing(InvListener listener){
        listeners.add(listener);
    }

    private UUID getUniqueInventoryID(){
        return uniqueInventoryID;
    }

    public ItemStack createUnbitem(String name, String[] lore, Material item, int dura,boolean pikapika){
        ItemStack items = new ItemStack(item,1,(short)dura);
        ItemMeta meta = items.getItemMeta();
        meta.setLore(Arrays.asList(lore));
        meta.setDisplayName(name);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        if(pikapika){
            meta.addEnchant(Enchantment.ARROW_FIRE,1,true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        meta.setUnbreakable(true);
        items.setItemMeta(meta);
        return items;
    }

    public boolean isEmpty(){
        return inv.firstEmpty()==-1;
    }

    public ItemStack createSkullitem(String name, String[] lore,UUID playeruuid, boolean pikapika){
        ItemStack items = new ItemStack(Material.SKULL_ITEM,1,(short)3);
        SkullMeta meta = (SkullMeta) items.getItemMeta();
        meta.setLore(Arrays.asList(lore));
        meta.setDisplayName(name);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        if(pikapika){
            meta.addEnchant(Enchantment.ARROW_FIRE,1,true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        meta.setUnbreakable(true);
        meta.setOwningPlayer(Bukkit.getPlayer(playeruuid));
        items.setItemMeta(meta);
        return items;
    }

}
