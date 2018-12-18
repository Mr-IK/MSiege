package red.man10.msiege;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class CraftData{
    String name;
    ItemStack book;
    List<ItemStack> useitems = new ArrayList<>();
    ItemStack result;
    SiegeData data;

    public CraftData(SiegeData data,String name){
        this.name = name;
        this.data = data;
    }
    public void saveFile(){
        File folder = new File(data.plugin.getDataFolder(), File.separator + "Crafts");
        if(!folder.exists()){
            folder.mkdir();
        }
        File f = new File(folder, File.separator + name + ".yml");
        FileConfiguration data = YamlConfiguration.loadConfiguration(f);

        f.delete();

        if (!f.exists()) {
            try {
                data.set("name", name);
                data.set("book", book);
                data.set("uses", useitems);
                data.set("result", result);
                data.save(f);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }
    public void loadFile(){
        File folder = new File(data.plugin.getDataFolder(), File.separator + "Crafts");
        if(!folder.exists()){
            folder.mkdir();
        }
        File f = new File(folder, File.separator + name + ".yml");
        FileConfiguration data = YamlConfiguration.loadConfiguration(f);

        if (f.exists()) {
            name = data.getString("name");
            book = data.getItemStack("book");
            useitems = (List<ItemStack>) data.get("uses");
            result = data.getItemStack("result");
        }
    }
}
