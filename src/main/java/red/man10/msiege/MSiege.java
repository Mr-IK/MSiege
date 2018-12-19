package red.man10.msiege;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import red.man10.man10vaultapiplus.Man10VaultAPI;

public final class MSiege extends JavaPlugin {

    MySQLManager mysql;
    SiegeData data;
    Man10VaultAPI vault;

    FileConfiguration config;

    String prefix = "§3[§dM§7Siege§3]§r";
    String prefix_nosec = "[MSiege]";

    String buildcode = "v1.0-α-0015";

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        config = getConfig();
        mysql = new MySQLManager(this,"MSiege");
        String dbresult = "成功";
        if(!mysql.connected){
            dbresult = "失敗";
        }
        vault = new Man10VaultAPI("MSiege");
        System.out.println(getNprefix()+"データベースアクセス"+dbresult+"。");
        data = new SiegeData(this);
        System.out.println(getNprefix()+"Siegeに関するデータ ロード完了。");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        for(PlayerStats stats : data.playerstats){
            stats.saveFile();
        }
    }

    public String getPrefix(){
        return prefix;
    }

    public String getNprefix(){
        return prefix_nosec;
    }

    public void consoleLog(String msg){
        System.out.println(getNprefix()+"INFO: "+msg);
    }

    public void consoleError(String msg){
        System.out.println(getNprefix()+"ERROR: "+msg);
    }

    public void consoleWarn(String msg){
        System.out.println(getNprefix()+"WARNING: "+msg);
    }
}
