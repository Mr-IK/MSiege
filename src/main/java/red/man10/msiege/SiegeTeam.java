package red.man10.msiege;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.UUID;

public class SiegeTeam {
    ArrayList<UUID> playerlist = new ArrayList<>();
    int nexushp;
    Location loc;
    Location nexusloc;
    Inventory teaminv;

    public SiegeTeam(){
        nexushp = 50;//200
        teaminv = Bukkit.createInventory(null,54,"§8チームインベントリ");
    }

    public void tptoSpawnAllplayer(){
        for(UUID uuid : playerlist){
            Player p = Bukkit.getPlayer(uuid);
            if(p != null){
                p.teleport(loc);
            }
        }
    }

    public void openteaminv(Player p){
        p.openInventory(teaminv);
    }


    public void tptoLobbyAllplayer(){
        for(UUID uuid : playerlist){
            Player p = Bukkit.getPlayer(uuid);
            if(p != null){
                Bukkit.dispatchCommand(p,"ewarp minigame_lobby");
                p.setGameMode(GameMode.SURVIVAL);
                p.getInventory().clear();
            }
        }
    }

    public void damagenexus(int i){
        nexushp = nexushp - i;
        for(UUID uuid : playerlist){
            Player p = Bukkit.getPlayer(uuid);
            if(p != null){
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP,2.0f,2.0f);
                p.sendMessage("§c§l!警告! §4§lネクサスがダメージを受けています！！ §c§l残りHP: §6"+nexushp);
            }
        }
    }

    public int getNexushp() {
        return nexushp;
    }

    public Location getLoc() {
        return loc;
    }

    public Location getNexusloc() {
        return nexusloc;
    }

    public void setLoc(Location loc) {
        this.loc = loc;
    }

    public void setNexusloc(Location nexusloc) {
        this.nexusloc = nexusloc;
    }
}
