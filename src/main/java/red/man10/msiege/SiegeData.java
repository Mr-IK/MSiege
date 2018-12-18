package red.man10.msiege;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import red.man10.msiege.util.NameTagManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class SiegeData implements Listener {

    public MSiege plugin;
    MySQLManager mysql;
    ItemBankManager ibm;
    SiegeCommand cmd;
    TeamChat tc;
    NameTagManager ntm;
    ArrayList<PlayerStats> playerstats;
    ArrayList<SiegeCard> cardlist;
    ArrayList<SiegeArena> arenalist;

    public SiegeData(MSiege plugin){
        Bukkit.getServer().getPluginManager().registerEvents(this,plugin);
        this.plugin = plugin;
        mysql = plugin.mysql;
        ibm = new ItemBankManager(this);
        arenalist = new ArrayList<>();
        cardlist = new ArrayList<>();
        playerstats = new ArrayList<>();
        for(String str: list()){
            SiegeArena arena = new SiegeArena(this,str);
            arena.loadFile();
            arenalist.add(arena);
            arena.generateUniqueID();
        }
        for(String str: playerlist()){
            PlayerStats stats = new PlayerStats(plugin,str,false);
            playerstats.add(stats);
        }
        for(String str: cardlist()){
            SiegeCard card = new SiegeCard(this,str,false);
            cardlist.add(card);
        }
        cmd = new SiegeCommand(this,"msi");
        new SiegeCommand(this,"siege");
        tc = new TeamChat(this);
        ntm = new NameTagManager(this);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        if(playerlist().contains(e.getPlayer().getUniqueId().toString())){
            for(PlayerStats stats : playerstats){
                if(stats.getLogoutloc()!=null){
                    if(stats.getJoinarena()==null||!getArena(stats.getJoinarena()).isNowgame()||getArena(stats.getJoinarena())==null
                            ||getArena(stats.getJoinarena()).getUniqueGameID().equals(stats.getJoingameuniqueid())){
                        e.getPlayer().setGameMode(GameMode.SURVIVAL);
                        e.getPlayer().getInventory().clear();
                        break;
                    }

                    List<Player> player = new ArrayList<>();
                    List<Player> player2 =  new ArrayList<>();
                    for(UUID uuid : getArena(stats.getJoinarena()).team1.playerlist) {
                        if (Bukkit.getPlayer(uuid) != null) {
                            player.add(Bukkit.getPlayer(uuid));
                        }
                    }
                    for(UUID uuid : getArena(stats.getJoinarena()).team2.playerlist) {
                        if (Bukkit.getPlayer(uuid) != null) {
                            player2.add(Bukkit.getPlayer(uuid));
                        }
                    }
                    if(getArena(stats.getJoinarena()).team1.playerlist.contains(e.getPlayer().getUniqueId())){
                        ntm.setTag(Bukkit.getPlayer(e.getPlayer().getUniqueId()),"§a[TEAM]§c",null,player);
                        ntm.setTag(Bukkit.getPlayer(e.getPlayer().getUniqueId()),"§4[Enemy]§b",null,player2);
                    }else{
                        ntm.setTag(Bukkit.getPlayer(e.getPlayer().getUniqueId()),"§a[TEAM]§b",null,player2);
                        ntm.setTag(Bukkit.getPlayer(e.getPlayer().getUniqueId()),"§4[Enemy]§c",null,player);
                    }
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                        stats.updateInv();
                        getArena(stats.getJoinarena()).scoreboard.setShowPlayer(e.getPlayer());
                        getArena(stats.getJoinarena()).bar.addPlayer(e.getPlayer());
                        e.getPlayer().teleport(stats.getLogoutloc());
                        e.getPlayer().setGameMode(GameMode.SURVIVAL);
                        showMessage(e.getPlayer().getUniqueId().toString(),"§aゲームに再参加しました。");
                    },20);
                    break;
                }
            }
        }else{
            PlayerStats stats = new PlayerStats(plugin,e.getPlayer().getUniqueId().toString(),true);
            stats.setDeath(0);
            stats.setKill(0);
            stats.setLose(0);
            stats.setMatch(0);
            stats.setWin(0);
            stats.setPoint(0);
            stats.setName(e.getPlayer().getName());
            stats.saveInv();
            stats.saveFile();
            playerstats.add(stats);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        PlayerStats stats = getStats(e.getPlayer().getUniqueId().toString());
        stats.saveInv();
        stats.setLogoutloc(e.getPlayer().getLocation());
        if(stats.getJoinarena()==null||getArena(stats.getJoinarena()).isNowgame()||getArena(stats.getJoinarena())==null||getArena(stats.getJoinarena()).getUniqueGameID().equals(stats.getJoingameuniqueid())){
            stats.saveFile();
            return;
        }
        SiegeArena arena = getArena(stats.getJoinarena());
        if(arena.team1.playerlist.contains(e.getPlayer().getUniqueId())){
            arena.team1.playerlist.remove(e.getPlayer().getUniqueId());
        }else{
            arena.team2.playerlist.remove(e.getPlayer().getUniqueId());
        }
        stats.resetJoinArena();
        stats.setJoingameuniqueid(null);
        stats.saveFile();
    }


    public int getPlayerHaveCost(Player p){
        int cost = 30;
        for(String str:getStats(p.getUniqueId().toString()).getSelectcard()){
            cost = cost - getCard(str).cost;
        }
        return cost;
    }

    public ArrayList<String> list() {

        ArrayList<String> list = new ArrayList<>();

        File folder = new File(plugin.getDataFolder(), File.separator + "Arenas");

        if(!folder.exists()){
            folder.mkdir();
        }

        File[] files = folder.listFiles();  // (a)
        if (files != null) {
            for (File f : files) {
                if (f.isFile()){  // (c)
                    String filename = f.getName();
                    list.add(remove_yml(filename));
                }
            }
        }

        return list;
    }

    public ArrayList<String> playerlist() {

        ArrayList<String> list = new ArrayList<>();

        File folder = new File(plugin.getDataFolder(), File.separator + "Users");

        if(!folder.exists()){
            folder.mkdir();
        }

        File[] files = folder.listFiles();  // (a)
        if (files != null) {
            for (File f : files) {
                if (f.isFile()){  // (c)
                    String filename = f.getName();
                    list.add(remove_yml(filename));
                }
            }
        }

        return list;
    }

    public ArrayList<String> cardlist() {

        ArrayList<String> list = new ArrayList<>();

        File folder = new File(plugin.getDataFolder(), File.separator + "Cards");

        if(!folder.exists()){
            folder.mkdir();
        }

        File[] files = folder.listFiles();  // (a)
        if (files != null) {
            for (File f : files) {
                if (f.isFile()){  // (c)
                    String filename = f.getName();
                    list.add(remove_yml(filename));
                }
            }
        }

        return list;
    }

    public ArrayList<String> craftlist() {

        ArrayList<String> list = new ArrayList<>();

        File folder = new File(plugin.getDataFolder(), File.separator + "Crafts");

        if(!folder.exists()){
            folder.mkdir();
        }

        File[] files = folder.listFiles();  // (a)
        if (files != null) {
            for (File f : files) {
                if (f.isFile()){  // (c)
                    String filename = f.getName();
                    list.add(remove_yml(filename));
                }
            }
        }

        return list;
    }

    public String remove_yml(String filename){
        if(filename.substring(0,1).equalsIgnoreCase(".")){
            return filename;
        }

        int point = filename.lastIndexOf(".");
        if (point != -1) {
            filename =  filename.substring(0, point);
        }
        return filename;
    }

    /*
    ここから重要なメソッド
     */


    public SiegeArena getArena(String arenaname){
        SiegeArena startarena = null;
        for(SiegeArena arena : arenalist){
            if(arena.getName().equalsIgnoreCase(arenaname)){
                startarena = arena;
                break;
            }
        }
        if(startarena==null){
            return null;
        }
        return startarena;
    }

    public PlayerStats getStats(String uuids){
        PlayerStats stats = null;
        for(PlayerStats statss : playerstats){
            if(statss.getUuid_s().equalsIgnoreCase(uuids)){
                stats = statss;
                break;
            }
        }
        if(stats==null){
            return null;
        }
        return stats;
    }

    public SiegeCard getCard(String name){
        SiegeCard stats = null;
        for(SiegeCard statss : cardlist){
            if(statss.name.equalsIgnoreCase(name)){
                stats = statss;
                break;
            }
        }
        if(stats==null){
            return null;
        }
        return stats;
    }

    public void gameStart(String arenaname){
        SiegeArena startarena = getArena(arenaname);
        if(startarena==null){
            return;
        }
        if(startarena.team1.playerlist.size()<1||startarena.team2.playerlist.size()<1){
            return;
        }
        if(startarena.isNowgame()){
            return;
        }
        startarena.team1.tptoSpawnAllplayer();
        startarena.team2.tptoSpawnAllplayer();
        List<Player> player = new ArrayList<>();
        List<Player> player2 =  new ArrayList<>();
        for(UUID uuid : startarena.team1.playerlist) {
            if (Bukkit.getPlayer(uuid) != null) {
                player.add(Bukkit.getPlayer(uuid));
                Bukkit.getPlayer(uuid).getEnderChest().clear();
            }
        }
        for(UUID uuid : startarena.team2.playerlist) {
            if (Bukkit.getPlayer(uuid) != null) {
                player2.add(Bukkit.getPlayer(uuid));
                Bukkit.getPlayer(uuid).getEnderChest().clear();
            }
        }
        for(UUID uuid : startarena.team1.playerlist){
            if(Bukkit.getPlayer(uuid)!=null) {
                getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).setMatch(getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).getMatch() + 1);
                getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).setJoingameuniqueid(startarena.getUniqueGameID());
                startarena.craft.playerlist.add(uuid);
                ntm.setTag(Bukkit.getPlayer(uuid),"§a[TEAM]§c",null,player);
                ntm.setTag(Bukkit.getPlayer(uuid),"§4[Enemy]§c",null,player2);
            }
        }
        for(UUID uuid : startarena.team2.playerlist){
            if(Bukkit.getPlayer(uuid)!=null) {
                getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).setMatch(getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).getMatch() + 1);
                getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).setJoingameuniqueid(startarena.getUniqueGameID());
                startarena.craft.playerlist.add(uuid);
                ntm.setTag(Bukkit.getPlayer(uuid),"§a[TEAM]§b",null,player2);
                ntm.setTag(Bukkit.getPlayer(uuid),"§4[Enemy]§b",null,player);
            }
        }
        enabledPlayersCard(startarena.getName());
        UUID gameid = startarena.getUniqueGameID();
        startarena.setCanjoin(false);
        startarena.setNowgame(true);
        startarena.setWave(0);
        Bukkit.broadcastMessage(plugin.getPrefix()+"§aさあ！準備せよ！ 準備ウェーブが開始されました！ §6終了まで: 5分");
        playsoundAll(arenaname,Sound.ENTITY_ENDERDRAGON_AMBIENT,0.5f,1.0f);
        startarena.initBossbar();
        startarena.initScoreBoard();
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            if(!startarena.nowgame||startarena.getUniqueGameID()==null||!gameid.equals(startarena.getUniqueGameID())){
                return;
            }
            startarena.setWave(1);
            startarena.updateScoreBoard_wave(0);
            Bukkit.broadcastMessage(plugin.getPrefix()+"§a突撃の時だ！ ウェーブ1が開始されました！ §6終了まで: 10分");
            playsoundAll(arenaname,Sound.ENTITY_ENDERDRAGON_AMBIENT,0.5f,1.0f);
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                if(!startarena.nowgame||startarena.getUniqueGameID()==null||!gameid.equals(startarena.getUniqueGameID())){
                    return;
                }
                startarena.setWave(2);
                startarena.updateScoreBoard_wave(1);
                Bukkit.broadcastMessage(plugin.getPrefix()+"§aさあ、倒せ！ ウェーブ2が開始されました！ §6終了まで: 10分");
                playsoundAll(arenaname,Sound.ENTITY_ENDERDRAGON_AMBIENT,0.5f,1.0f);
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    if(!startarena.nowgame||startarena.getUniqueGameID()==null||!gameid.equals(startarena.getUniqueGameID())){
                        return;
                    }
                    startarena.setWave(3);
                    startarena.updateScoreBoard_wave(2);
                    Bukkit.broadcastMessage(plugin.getPrefix()+"§a後がないぞ！ ウェーブ3が開始されました！ §6終了まで: 10分");
                    playsoundAll(arenaname,Sound.ENTITY_ENDERDRAGON_AMBIENT,0.5f,1.0f);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                        if(!startarena.nowgame||startarena.getUniqueGameID()==null||!gameid.equals(startarena.getUniqueGameID())){
                            return;
                        }
                        startarena.setWave(4);
                        startarena.updateScoreBoard_wave(3);
                        Bukkit.broadcastMessage(plugin.getPrefix()+"§4§l終わらねぇ！  サドンデスが開始されました！ §c終了まで: ∞分");
                        playsoundAll(arenaname,Sound.ENTITY_ENDERDRAGON_AMBIENT,0.5f,1.0f);
                    },12000);
                },12000);
            },12000);
        },6000); //6000
    }

    public void gameJoin(String arenaname,Player p){
        for(String str:list()){
            if(getArena(str).team1.playerlist.contains(p.getUniqueId())||getArena(str).team2.playerlist.contains(p.getUniqueId())){
                showMessage(p.getUniqueId().toString(),"§cあなたはすでに参加しています！");
                return;
            }
        }
        if(getArena(arenaname).nowgame){
            showMessage(p.getUniqueId().toString(),"§cこのアリーナはすでにゲームが開始しています！");
            return;
        }
        if(getArena(arenaname).team1.playerlist.size()<getArena(arenaname).team2.playerlist.size()) {
            getArena(arenaname).team1.playerlist.add(p.getUniqueId());
            showMessage(p.getUniqueId().toString(),"§a"+arenaname+"に参加しました: §bTEAM1");
        }else if(getArena(arenaname).team2.playerlist.size()<getArena(arenaname).team1.playerlist.size()) {
            getArena(arenaname).team2.playerlist.add(p.getUniqueId());
            showMessage(p.getUniqueId().toString(),"§a"+arenaname+"に参加しました: §bTEAM2");
        }else{
            int rnd = new Random().nextInt(2)+1;
            if(rnd == 1){
                getArena(arenaname).team1.playerlist.add(p.getUniqueId());
            }else if(rnd == 2){
                getArena(arenaname).team2.playerlist.add(p.getUniqueId());
            }
            showMessage(p.getUniqueId().toString(),"§a"+arenaname+"に参加しました: §bTEAM"+rnd);
        }
        getStats(p.getUniqueId().toString()).setJoinarena(arenaname);
        getStats(p.getUniqueId().toString()).setJoingameuniqueid(getArena(arenaname).getUniqueGameID());
        for(UUID uuid : getArena(arenaname).team1.playerlist){
            if(Bukkit.getPlayer(uuid)!=null) {
                showMessage(uuid.toString(),"§a"+p.getName()+"さんが参加しました");
            }
        }
        for(UUID uuid : getArena(arenaname).team2.playerlist){
            if(Bukkit.getPlayer(uuid)!=null) {
                showMessage(uuid.toString(),"§a"+p.getName()+"さんが参加しました");
            }
        }
    }

    public void gameEnd(String arenaname,int winteam){
        SiegeArena startarena = getArena(arenaname);
        if(startarena==null){
            return;
        }
        startarena.team1.tptoLobbyAllplayer();
        startarena.team2.tptoLobbyAllplayer();
        startarena.setWave(-1);
        Bukkit.broadcastMessage(plugin.getPrefix()+"§a§l勝者: TEAM"+winteam+"!! おめでとう！");
        Bukkit.broadcastMessage(plugin.getPrefix()+"§a§l～TEAM"+winteam+"のメンバー～");
        if(winteam==1){
            for(UUID uuid : startarena.team1.playerlist){
                if(Bukkit.getPlayer(uuid)!=null) {
                    getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).setPoint(getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).getPoint() + 50);
                    getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).setWin(getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).getWin() + 1);
                    ntm.allplayerReset(Bukkit.getPlayer(uuid));
                    Bukkit.getPlayer(uuid).getEnderChest().clear();
                }
                Bukkit.broadcastMessage(plugin.getPrefix()+"§b"+Bukkit.getOfflinePlayer(uuid).getName());
            }
            for(UUID uuid : startarena.team2.playerlist){
                if(Bukkit.getPlayer(uuid)!=null) {
                    getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).setPoint(getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).getPoint() + 10);
                    getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).setLose(getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).getLose() + 1);
                    ntm.allplayerReset(Bukkit.getPlayer(uuid));
                    Bukkit.getPlayer(uuid).getEnderChest().clear();
                }
            }
        }else if(winteam==2){
            for(UUID uuid : startarena.team2.playerlist){
                if(Bukkit.getPlayer(uuid)!=null) {
                    getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).setPoint(getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).getPoint() + 50);
                    getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).setWin(getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).getWin() + 1);
                    ntm.allplayerReset(Bukkit.getPlayer(uuid));
                    Bukkit.getPlayer(uuid).getEnderChest().clear();
                }
                Bukkit.broadcastMessage(plugin.getPrefix()+"§b"+Bukkit.getOfflinePlayer(uuid).getName());
            }
            for(UUID uuid : startarena.team1.playerlist){
                if(Bukkit.getPlayer(uuid)!=null) {
                    getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).setPoint(getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).getPoint() + 10);
                    getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).setLose(getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).getLose() + 1);
                    ntm.allplayerReset(Bukkit.getPlayer(uuid));
                    Bukkit.getPlayer(uuid).getEnderChest().clear();
                }
            }
        }
        startarena.bossbarend();
        startarena.resetScoreboard();
        startarena.resetUniqueID();
        startarena.team1.playerlist.clear();
        startarena.team2.playerlist.clear();
        startarena.team1.nexushp = 200;
        startarena.team1.teaminv = Bukkit.createInventory(null,54,"§8チームインベントリ");
        startarena.team2.nexushp = 200;
        startarena.team2.teaminv = Bukkit.createInventory(null,54,"§8チームインベントリ");
        startarena.craft.playerlist.clear();
        startarena.generateUniqueID();
        startarena.resetWorld();
    }

    void showMessage(String uuid,String message){
        Bukkit.getPlayer(UUID.fromString(uuid)).sendMessage(plugin.getPrefix()+message);
    }

    void showBattleMessage(String arena,String message){
        for(UUID uuid:getArena(arena).team1.playerlist){
            Player p = Bukkit.getPlayer(uuid);
            if(p != null){
                showMessage(uuid.toString(),message);
            }
        }
        for(UUID uuid:getArena(arena).team2.playerlist){
            Player p = Bukkit.getPlayer(uuid);
            if(p != null){
                showMessage(uuid.toString(),message);
            }
        }
    }

    void playsoundAll(String arena,Sound sound,float ps,float p2){
        for(UUID uuid:getArena(arena).team1.playerlist){
            Player p = Bukkit.getPlayer(uuid);
            if(p != null){
                p.playSound(p.getLocation(),sound,ps,p2);
            }
        }
        for(UUID uuid:getArena(arena).team2.playerlist){
            Player p = Bukkit.getPlayer(uuid);
            if(p != null){
                p.playSound(p.getLocation(),sound,ps,p2);
            }
        }
    }

    void enabledPlayersCard(String arena){
        for(UUID uuid:getArena(arena).team1.playerlist){
            Player p = Bukkit.getPlayer(uuid);
            if(p != null){
                p.getInventory().clear();
                PlayerStats stats = getStats(p.getUniqueId().toString());
                for(String cardname:stats.getSelectcard()){
                    getCard(cardname).enablePower(p);
                }
            }
        }
        for(UUID uuid:getArena(arena).team2.playerlist){
            Player p = Bukkit.getPlayer(uuid);
            if(p != null){
                p.getInventory().clear();
                PlayerStats stats = getStats(p.getUniqueId().toString());
                for(String cardname:stats.getSelectcard()){
                    getCard(cardname).enablePower(p);
                }
            }
        }
    }

    public void playSound(String uuid,Sound sound){
        Player p = Bukkit.getPlayer(UUID.fromString(uuid));
        if(p == null){
            return;
        }
        if(p.isOnline()){
            p.playSound(p.getLocation(),sound,1,1);
        }
    }

    public String getColoredItemString(long amount){
        return String.format("§b§l%,d個",amount);
    }

}
