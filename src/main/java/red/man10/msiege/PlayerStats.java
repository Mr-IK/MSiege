package red.man10.msiege;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerStats {

    private String uuid_s;
    private MSiege plugin;

    private Location logoutloc;
    private String joinarena;
    private UUID joingameuniqueid;

    private String name;
    private int win;
    private int lose;
    private int kill;
    private int death;
    private int point;
    private int match;
    private ItemStack[] armor;
    private ItemStack[] inv;
    private List<String> havecard = new ArrayList<>();
    private List<String> selectcard = new ArrayList<>();

    public PlayerStats(MSiege plugin,String uuid,boolean noload){
        this.plugin = plugin;
        this.uuid_s = uuid;
        if(!noload){
            loadFile();
        }
    }

    public UUID getJoingameuniqueid() {
        return joingameuniqueid;
    }

    public void setJoingameuniqueid(UUID joingameuniqueid) {
        this.joingameuniqueid = joingameuniqueid;
    }

    public void addhavecard(String cardname){
        havecard.add(cardname);
    }

    public void removehavecard(String cardname){
        havecard.remove(cardname);
    }

    public void addselectcard(String cardname){
        selectcard.add(cardname);
    }

    public void removeselectcard(String cardname){
        selectcard.remove(cardname);
    }

    public void setLogoutloc(Location joinarena) {
        this.logoutloc = joinarena;
    }

    public void resetLogoutloc(){
        logoutloc = null;
    }

    public String getJoinarena() {
        return joinarena;
    }

    public void setJoinarena(String joinarena) {
        this.joinarena = joinarena;
    }

    public void resetJoinArena(){
        joinarena = null;
    }

    public Location getLogoutloc() {
        return logoutloc;
    }

    public void saveInv(){
        inv = Bukkit.getPlayer(UUID.fromString(uuid_s)).getInventory().getContents();
        armor = Bukkit.getPlayer(UUID.fromString(uuid_s)).getInventory().getArmorContents();
    }

    public void updateInv(){
        Bukkit.getPlayer(UUID.fromString(uuid_s)).getInventory().clear();
        Bukkit.getPlayer(UUID.fromString(uuid_s)).getInventory().setContents(inv);
        Bukkit.getPlayer(UUID.fromString(uuid_s)).getInventory().setArmorContents(armor);
    }


    public void saveFile(){
        File userdata = new File(plugin.getDataFolder(), File.separator + "Users");
        if(!userdata.exists()){
            userdata.mkdir();
        }
        File f = new File(userdata, File.separator + uuid_s + ".yml");
        FileConfiguration data = YamlConfiguration.loadConfiguration(f);

        f.delete();

        if (!f.exists()) {
            try {
                data.set("name", name);
                data.set("uuid", uuid_s);
                data.set("win", win);
                data.set("lose", lose);
                data.set("kill", kill);
                data.set("death", death);
                data.set("point", point);
                data.set("match", match);
                saveInv();
                data.set("inventory", inv);
                data.set("armor",armor);
                data.set("havecard", havecard);
                data.set("selcard",selectcard);
                data.save(f);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    public void loadFile(){
        File userdata = new File(plugin.getDataFolder(), File.separator + "Users");
        if(!userdata.exists()){
            userdata.mkdir();
        }
        File f = new File(userdata, File.separator + uuid_s + ".yml");
        FileConfiguration data = YamlConfiguration.loadConfiguration(f);

        if (f.exists()) {
            name = data.getString("name");
            uuid_s = data.getString("uuid");
            win = data.getInt("win");
            lose = data.getInt("lose");
            kill = data.getInt("kill");
            death = data.getInt("death");
            point = data.getInt("point");
            match = data.getInt("match");
            selectcard = data.getStringList("selcard");
            havecard = data.getStringList("havecard");
            Object a = data.get("inventory");
            Object b = data.get("armor");

            if(a == null || b == null){
                inv = null;
                armor = null;
                return;
            }
            ItemStack[] inventory = null;
            ItemStack[] armor = null;
            if (a instanceof ItemStack[]){
                inventory = (ItemStack[]) a;
            } else if (a instanceof List){
                List lista = (List) a;
                inventory = (ItemStack[]) lista.toArray(new ItemStack[0]);
            }
            if (b instanceof ItemStack[]){
                armor = (ItemStack[]) b;
            } else if (b instanceof List){
                List listb = (List) b;
                armor = (ItemStack[]) listb.toArray(new ItemStack[0]);
            }
            inv = inventory;
            this.armor = armor;
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getDeath() {
        return death;
    }

    public int getKill() {
        return kill;
    }

    public int getLose() {
        return lose;
    }

    public int getMatch() {
        return match;
    }

    public int getPoint() {
        return point;
    }

    public int getWin() {
        return win;
    }

    public String getUuid_s() {
        return uuid_s;
    }

    public void setDeath(int death) {
        this.death = death;
    }

    public void setKill(int kill) {
        this.kill = kill;
    }

    public void setLose(int lose) {
        this.lose = lose;
    }

    public void setMatch(int match) {
        this.match = match;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public void setUuid_s(String uuid_s) {
        this.uuid_s = uuid_s;
    }

    public void setWin(int win) {
        this.win = win;
    }

    public List<String> getHavecard() {
        return havecard;
    }

    public List<String> getSelectcard() {
        return selectcard;
    }
}

