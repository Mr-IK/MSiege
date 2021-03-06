package red.man10.msiege;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import red.man10.msiege.util.NameTagManager;

import java.io.File;
import java.util.*;

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
    SiegeJob job;

    int min_player;

    public SiegeData(MSiege plugin){
        min_player = plugin.config.getInt("min_player");
        Bukkit.getServer().getPluginManager().registerEvents(this,plugin);
        this.plugin = plugin;
        mysql = plugin.mysql;
        ibm = new ItemBankManager(this);
        arenalist = new ArrayList<>();
        cardlist = new ArrayList<>();
        playerstats = new ArrayList<>();
        job = new SiegeJob(this);
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
        for(String str: joblist()){
            JobData jobdata = new JobData(job,str);
            job.joblist.add(jobdata);
        }
        cmd = new SiegeCommand(this,"msi");
        new SiegeCommand(this,"siege");
        tc = new TeamChat(this);
        ntm = new NameTagManager(this);
        initGameItem();
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
        arena.votedamount.remove(e.getPlayer().getUniqueId());
        e.getPlayer().getInventory().clear();
        stats.saveInv();
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

    public ArrayList<String> joblist() {

        ArrayList<String> list = new ArrayList<>();

        File folder = new File(plugin.getDataFolder(), File.separator + "Jobs");

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
                for (PotionEffect effect :Bukkit.getPlayer(uuid).getActivePotionEffects ()){
                    Bukkit.getPlayer(uuid).removePotionEffect (effect.getType ());
                }
            }
        }
        for(UUID uuid : startarena.team2.playerlist){
            if(Bukkit.getPlayer(uuid)!=null) {
                getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).setMatch(getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).getMatch() + 1);
                getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).setJoingameuniqueid(startarena.getUniqueGameID());
                startarena.craft.playerlist.add(uuid);
                ntm.setTag(Bukkit.getPlayer(uuid),"§a[TEAM]§b",null,player2);
                ntm.setTag(Bukkit.getPlayer(uuid),"§4[Enemy]§b",null,player);
                for (PotionEffect effect :Bukkit.getPlayer(uuid).getActivePotionEffects ()){
                    Bukkit.getPlayer(uuid).removePotionEffect (effect.getType ());
                }
            }
        }
        enabledPlayersCard(startarena.getName());
        UUID gameid = startarena.getUniqueGameID();
        startarena.setCanjoin(false);
        startarena.setNowgame(true);
        startarena.setWave(0);
        Bukkit.broadcastMessage(plugin.getPrefix()+"§aさあ！準備せよ！ 準備ウェーブが開始されました！ §6終了まで: 3分");
        playsoundAll(arenaname,Sound.ENTITY_ENDERDRAGON_AMBIENT,0.5f,1.0f);
        startarena.initScoreBoard();
        showBattleTitle(arenaname,"§a§lゲーム開始！","§6いけえええ！");
        startarena.initBossbar();
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            if(!startarena.nowgame||startarena.getUniqueGameID()==null||!gameid.equals(startarena.getUniqueGameID())){
                return;
            }
            startarena.setWave(1);
            startarena.updateScoreBoard_wave(0);
            Bukkit.broadcastMessage(plugin.getPrefix()+"§a突撃の時だ！ ウェーブ1が開始されました！ §6終了まで: 10分");
            playsoundAll(arenaname,Sound.ENTITY_ENDERDRAGON_AMBIENT,0.5f,1.0f);
            showBattleTitle(arenaname,"§a§lウェーブ1開始!","§e突撃だ！");
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                if(!startarena.nowgame||startarena.getUniqueGameID()==null||!gameid.equals(startarena.getUniqueGameID())){
                    return;
                }
                startarena.setWave(2);
                startarena.updateScoreBoard_wave(1);
                Bukkit.broadcastMessage(plugin.getPrefix()+"§aさあ、倒せ！ ウェーブ2が開始されました！ §6終了まで: 10分");
                playsoundAll(arenaname,Sound.ENTITY_ENDERDRAGON_AMBIENT,0.5f,1.0f);
                showBattleTitle(arenaname,"§a§lウェーブ2開始!","§eブッ倒せ！");
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    if(!startarena.nowgame||startarena.getUniqueGameID()==null||!gameid.equals(startarena.getUniqueGameID())){
                        return;
                    }
                    startarena.setWave(3);
                    startarena.updateScoreBoard_wave(2);
                    Bukkit.broadcastMessage(plugin.getPrefix()+"§a後がないぞ！ ウェーブ3が開始されました！ §6終了まで: 10分");
                    playsoundAll(arenaname,Sound.ENTITY_ENDERDRAGON_AMBIENT,0.5f,1.0f);
                    showBattleTitle(arenaname,"§a§lウェーブ3開始!","§e急げ！");
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                        if(!startarena.nowgame||startarena.getUniqueGameID()==null||!gameid.equals(startarena.getUniqueGameID())){
                            return;
                        }
                        startarena.setWave(4);
                        startarena.updateScoreBoard_wave(3);
                        Bukkit.broadcastMessage(plugin.getPrefix()+"§4§l終わらねぇ！  サドンデスが開始されました！ §c終了まで: ∞分");
                        playsoundAll(arenaname,Sound.ENTITY_ENDERDRAGON_AMBIENT,0.5f,1.0f);
                        showBattleTitle(arenaname,"§4§lサドンデス開始!!","§c§l殺しあえ！！");
                    },12000);
                },12000);
            },12000);
        },3600); //6000
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
        p.teleport(getArena(arenaname).lobbyloc);
        p.getInventory().clear();
        addGameItem(p);
        job.selectjob.put(p.getUniqueId(),"bommer");
    }

    ItemStack leave = null;
    ItemStack startvote = null;
    ItemStack jobset = null;

    public void addGameItem(Player p){
        p.getInventory().setItem(0,startvote);
        p.getInventory().setItem(2,jobset);
        p.getInventory().setItem(8,leave);
    }

    private void initGameItem(){
        leave = createUnbitem("§cゲームを抜ける",new String[]{"§eクリックでゲームを抜けます"},Material.DARK_OAK_DOOR_ITEM,0,false);
        startvote = createUnbitem("§aゲーム開始に投票",new String[]{"§eクリックでゲームを開始に投票します"},Material.DIAMOND,0,false);
        jobset = createUnbitem("§e役職をセット",new String[]{"§eクリックで役職をセットします"},Material.INK_SACK,11,false);
    }

    @EventHandler
    public void commandCancel(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        PlayerStats stats = getStats(p.getUniqueId().toString());
        if(stats.getJoinarena()==null||getArena(stats.getJoinarena())==null
                ||stats.getJoingameuniqueid()==null||!getArena(stats.getJoinarena()).getUniqueGameID().equals(stats.getJoingameuniqueid())){
            return;
        }
        if(e.getMessage().startsWith("/msi")||e.getMessage().startsWith("/siege")||e.getMessage().startsWith("/tc")){
            return;
        }
        if(p.isOp()){
            return;
        }
        e.setCancelled(true);
        showMessage(p.getUniqueId().toString(),"§cゲームに参加中はコマンドを実行できません！");
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent e){
        if(e.getItemDrop()!=null) {
            if (e.getItemDrop().getItemStack().equals(startvote)||e.getItemDrop().getItemStack().equals(leave)||e.getItemDrop().getItemStack().equals(jobset)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void Intract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if(player.getInventory().getItemInMainHand()!=null){
            if(player.getInventory().getItemInMainHand().equals(startvote)){
                e.setCancelled(true);
                //スタートに投票
                SiegeArena arena = getArena(getStats(player.getUniqueId().toString()).getJoinarena());
                if(arena.team1.playerlist.size()+arena.team2.playerlist.size()<min_player){
                    showMessage(player.getUniqueId().toString(),"§cプレイヤー数が足りないためスタートに投票できません!");
                    return;
                }
                if(arena.votedamount.contains(player.getUniqueId())){
                    showMessage(player.getUniqueId().toString(),"§cすでに投票しています！");
                    return;
                }
                arena.votedamount.add(player.getUniqueId());
                showMessage(player.getUniqueId().toString(),"§aスタートに投票しました "+
                        (arena.team1.playerlist.size()+arena.team2.playerlist.size())+"人中"+arena.votedamount.size()+"人投票 " +
                        "§e"+((double)arena.votedamount.size()/(arena.team1.playerlist.size()+arena.team2.playerlist.size())*100)+"%");
                if(((double)arena.votedamount.size()/(arena.team1.playerlist.size()+arena.team2.playerlist.size()))>=0.75){
                    showBattleMessage(arena.name,"§a§l75%以上の人がスタートに投票したので開始します！");
                    gameStart(arena.name);
                }
            }else if(player.getInventory().getItemInMainHand().equals(leave)){
                e.setCancelled(true);
                Bukkit.dispatchCommand(player,"ewarp minigame_lobby");
                Bukkit.dispatchCommand(player,"msi leave");
            }else if(player.getInventory().getItemInMainHand().equals(jobset)){
                Bukkit.dispatchCommand(player,"msi jobset");
            }
        }
    }

    public ItemStack createUnbitem(String name, String[] lore, Material item, int dura, boolean pikapika){
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

    public void gameEnd(String arenaname,int winteam){
        SiegeArena startarena = getArena(arenaname);
        if(startarena==null){
            return;
        }
        showBattleTitle(arenaname,"§a§lゲーム終了！","§eお疲れ！");
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
                    getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).saveFile();
                    ntm.allplayerReset(Bukkit.getPlayer(uuid));
                    Bukkit.getPlayer(uuid).getEnderChest().clear();
                    for (PotionEffect effect :Bukkit.getPlayer(uuid).getActivePotionEffects ()){
                        Bukkit.getPlayer(uuid).removePotionEffect (effect.getType ());
                    }
                }
                Bukkit.broadcastMessage(plugin.getPrefix()+"§b"+Bukkit.getOfflinePlayer(uuid).getName());
            }
            for(UUID uuid : startarena.team2.playerlist){
                if(Bukkit.getPlayer(uuid)!=null) {
                    getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).setPoint(getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).getPoint() + 10);
                    getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).setLose(getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).getLose() + 1);
                    getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).saveFile();
                    ntm.allplayerReset(Bukkit.getPlayer(uuid));
                    Bukkit.getPlayer(uuid).getEnderChest().clear();
                    for (PotionEffect effect :Bukkit.getPlayer(uuid).getActivePotionEffects ()){
                        Bukkit.getPlayer(uuid).removePotionEffect (effect.getType ());
                    }
                }
            }
        }else if(winteam==2){
            for(UUID uuid : startarena.team2.playerlist){
                if(Bukkit.getPlayer(uuid)!=null) {
                    getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).setPoint(getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).getPoint() + 50);
                    getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).setWin(getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).getWin() + 1);
                    getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).saveFile();
                    ntm.allplayerReset(Bukkit.getPlayer(uuid));
                    Bukkit.getPlayer(uuid).getEnderChest().clear();
                    for (PotionEffect effect :Bukkit.getPlayer(uuid).getActivePotionEffects ()){
                        Bukkit.getPlayer(uuid).removePotionEffect (effect.getType ());
                    }
                }
                Bukkit.broadcastMessage(plugin.getPrefix()+"§b"+Bukkit.getOfflinePlayer(uuid).getName());
            }
            for(UUID uuid : startarena.team1.playerlist){
                if(Bukkit.getPlayer(uuid)!=null) {
                    getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).setPoint(getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).getPoint() + 10);
                    getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).setLose(getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).getLose() + 1);
                    getStats(Bukkit.getPlayer(uuid).getUniqueId().toString()).saveFile();
                    ntm.allplayerReset(Bukkit.getPlayer(uuid));
                    Bukkit.getPlayer(uuid).getEnderChest().clear();
                    for (PotionEffect effect :Bukkit.getPlayer(uuid).getActivePotionEffects ()){
                        Bukkit.getPlayer(uuid).removePotionEffect (effect.getType ());
                    }
                }
            }
        }
        startarena.bossbarend();
        startarena.resetScoreboard();
        startarena.resetUniqueID();
        startarena.team1.playerlist.clear();
        startarena.team2.playerlist.clear();
        startarena.team1.teaminv.clear();
        startarena.team2.teaminv.clear();
        startarena.craft.playerlist.clear();
        startarena.generateUniqueID();
        startarena.team1.nexushp = startarena.hp;
        startarena.team2.nexushp = startarena.hp;
        startarena.votedamount.clear();
        startarena.resetores();
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

    void showBattleTitle(String arena,String title,String subtitle){
        for(UUID uuid:getArena(arena).team1.playerlist){
            Player p = Bukkit.getPlayer(uuid);
            if(p != null){
                p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN,1,1);
                p.sendTitle(title,subtitle,10,60,10);
            }
        }
        for(UUID uuid:getArena(arena).team2.playerlist){
            Player p = Bukkit.getPlayer(uuid);
            if(p != null){
                p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN,1,1);
                p.sendTitle(title,subtitle,10,60,10);
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
