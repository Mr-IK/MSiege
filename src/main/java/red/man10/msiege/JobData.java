package red.man10.msiege;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.List;

class JobData{
    SiegeJob job;
    String name;
    List<ItemStack> jobitem;

    public JobData(SiegeJob job,String name){
        this.job = job;
        this.name = name;
        loadFile();
    }

    public JobData(SiegeJob job,String name,List<ItemStack> jobitem){
        this.job = job;
        this.name = name;
        this.jobitem = jobitem;
        saveFile();
    }

    public void saveFile(){
        File folder = new File(job.data.plugin.getDataFolder(), File.separator + "Jobs");
        if(!folder.exists()){
            folder.mkdir();
        }
        File f = new File(folder, File.separator + name + ".yml");
        FileConfiguration data = YamlConfiguration.loadConfiguration(f);

        f.delete();

        if (!f.exists()) {
            try {
                data.set("name", name);
                data.set("jobitem", jobitem);
                data.save(f);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    public void loadFile(){
        File folder = new File(job.data.plugin.getDataFolder(), File.separator + "Jobs");
        if(!folder.exists()){
            folder.mkdir();
        }
        File f = new File(folder, File.separator + name + ".yml");
        FileConfiguration data = YamlConfiguration.loadConfiguration(f);

        if (f.exists()) {
            name = data.getString("name");
            jobitem = (List<ItemStack>)data.get("jobitem");
        }
    }
}