package red.man10.msiege.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import red.man10.msiege.SiegeData;

import java.util.*;

public class NameTagManager {
    SiegeData data;
    List<NameTagData> tagdata = new ArrayList<>();


    class NameTagData{
        private UUID uuid;
        private String prefix;
        private String suffix;
        NameTagData(UUID uuid,String prefix,String suffix){
            this.uuid = uuid;
            this.prefix = prefix;
            this.suffix = suffix;
        }
        UUID getUUID(){
            return uuid;
        }
        String getPrefix(){
            return prefix;
        }
        String getSuffix(){
            return suffix;
        }
    }

    public NameTagManager(SiegeData plugin){
        data = plugin;
    }

    public NameTagData setTag(Player tagreceiver,String prefix,String suffix,List<Player> packetereceivers) {
        List<Player> admins = Collections.singletonList(tagreceiver);
        NameTagData data = new NameTagData(tagreceiver.getUniqueId(),prefix,suffix);
        NameTag.getter.send(admins, prefix,suffix, packetereceivers);
        return data;
    }

    public void joinLoad(Player joiner,NameTagData data){
        Player player = Bukkit.getPlayer(data.getUUID());
        if(player == null){
            return;
        }
        List<Player> admins = Collections.singletonList(player);
        List<Player> packetereceivers = Collections.singletonList(joiner);
        NameTag.getter.send(admins, data.prefix, data.suffix, packetereceivers);
    }

    public void allplayerReset(Player p){
        List<Player> reset = Collections.singletonList(p);
        for(Player ps :Bukkit.getOnlinePlayers()){
            List<Player> admins = Collections.singletonList(ps);
            NameTag.getter.send(admins,null,null,reset);
        }
    }


}
