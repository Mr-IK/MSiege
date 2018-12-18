package red.man10.msiege;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TeamChat implements CommandExecutor {

    SiegeData data;

    public TeamChat(SiegeData data){
        this.data = data;
        data.plugin.getCommand("tc").setExecutor(this);
    }

    public SiegeTeam getTeam(Player p){
        PlayerStats stats = data.getStats(p.getUniqueId().toString());
        if(stats.getJoinarena()==null||data.getArena(stats.getJoinarena())==null||!data.getArena(stats.getJoinarena()).isNowgame()
                ||stats.getJoingameuniqueid()==null||!data.getArena(stats.getJoinarena()).getUniqueGameID().equals(stats.getJoingameuniqueid())){
            return null;
        }
        SiegeArena arena = data.getArena(stats.getJoinarena());
        if(arena.team1.playerlist.contains(p.getUniqueId())){
            return arena.team1;
        }else{
            return arena.team2;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player p = (Player) sender;
        SiegeTeam team = getTeam(p);
        if (!p.hasPermission("siege.use")) {
            p.sendMessage(data.plugin.getPrefix() + "§cあなたには権限がありません！");
            return true;
        }
        if(args.length == 0){
            p.sendMessage(data.plugin.getPrefix() + "§a/tc [内容 内容…]  §e: チームチャット");
            return true;
        }else{
            if(team==null){
                p.sendMessage(data.plugin.getPrefix() + "§cゲーム中ではありません！");
                return true;
            }
            StringBuilder argm = new StringBuilder();
            for(int i = 0;i<args.length;i++){
                if(argm.toString().equalsIgnoreCase("")){
                    argm = new StringBuilder(args[i]);
                }else{
                    argm.append(" ").append(args[i]);
                }
            }
            for(UUID uuid : team.playerlist){
                Player ps = Bukkit.getPlayer(uuid);
                if(ps != null){
                    ps.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP,2.0f,2.0f);
                    ps.sendMessage("§e§l[TEAM]§b"+p.getName()+"§3: §f"+new String(argm));
                }
            }
        }
        return true;
    }
}
