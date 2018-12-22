package red.man10.msiege;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SiegeCard {
    String name;
    List<String> lore;
    CardType type;
    List<ItemStack> item;
    String arg;
    int cost;

    SiegeData data;

    enum CardType{
        item,command,armor
    }

    public SiegeCard(SiegeData data,String name,boolean noload){
        this.item = new ArrayList<>();
        this.data = data;
        this.name = name;
        if(!noload) {
            loadFile();
        }
    }

    public void saveFile(){
        File folder = new File(data.plugin.getDataFolder(), File.separator + "Cards");
        if(!folder.exists()){
            folder.mkdir();
        }
        File f = new File(folder, File.separator + name + ".yml");
        FileConfiguration data = YamlConfiguration.loadConfiguration(f);

        f.delete();

        if (!f.exists()) {
            try {
                data.set("name", name);
                data.set("lore", lore);
                data.set("type", type.name());
                data.set("item", item);
                data.set("arg", arg);
                data.set("cost", cost);
                data.save(f);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    public void loadFile(){
        File folder = new File(data.plugin.getDataFolder(), File.separator + "Cards");
        if(!folder.exists()){
            folder.mkdir();
        }
        File f = new File(folder, File.separator + name + ".yml");
        FileConfiguration data = YamlConfiguration.loadConfiguration(f);

        if (f.exists()) {
            name = data.getString("name");
            lore = data.getStringList("lore");
            type = CardType.valueOf(data.getString("type"));
            if(data.contains("item")) {
                item = (List<ItemStack>)data.get("item");
            }
            if(data.contains("arg")) {
                arg = data.getString("arg");
            }
            cost = data.getInt("cost");
        }
    }

    public void enablePower(Player p){
        if(type.equals(CardType.item)){
            for(ItemStack item: item){
                p.getInventory().addItem(item);
            }
        }else if(type.equals(CardType.command)){
            if(arg.startsWith("@")){
                String cmd = arg.replaceFirst("@","");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),cmd);
            }else{
                Bukkit.dispatchCommand(p, arg);
            }
        }else if(type.equals(CardType.armor)){
            String[] args = arg.split(" ");
            int count= 0;
            for(ItemStack item: item){
                if(count<=args.length-1){
                    if(args[count].equalsIgnoreCase("helmet")){
                        if(p.getInventory().getHelmet()!=null){
                            p.getInventory().addItem(item);
                            count++;
                            continue;
                        }
                        p.getInventory().setHelmet(item);
                        count++;
                    }else if(args[count].equalsIgnoreCase("chestplate")){
                        if(p.getInventory().getChestplate()!=null){
                            p.getInventory().addItem(item);
                            count++;
                            continue;
                        }
                        p.getInventory().setChestplate(item);
                        count++;
                    }else if(args[count].equalsIgnoreCase("legging")){
                        if(p.getInventory().getLeggings()!=null){
                            p.getInventory().addItem(item);
                            count++;
                            continue;
                        }
                        p.getInventory().setLeggings(item);
                        count++;
                    }else if(args[count].equalsIgnoreCase("boot")){
                        if(p.getInventory().getBoots()!=null){
                            p.getInventory().addItem(item);
                            count++;
                            continue;
                        }
                        p.getInventory().setBoots(item);
                        count++;
                    }
                }
            }
        }
    }

}
