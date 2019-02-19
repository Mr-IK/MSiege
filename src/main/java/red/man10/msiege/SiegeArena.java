package red.man10.msiege;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import red.man10.msiege.util.SBossBar;
import red.man10.msiege.util.Scoreboard;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class SiegeArena implements Listener {

    boolean canjoin;
    boolean nowgame;
    String name;
    String worldname;
    SiegeTeam team1;
    SiegeTeam team2;
    int hp = 50;
    Location lobbyloc;

    ArrayList<Block> placeblocklist = new ArrayList<>();


    SBossBar bar = null;
    Scoreboard scoreboard = null;

    int wave = -1;
    int respawntime = 5;
    ArrayList<UUID> votedamount = new ArrayList<>();

    static ArrayList<Material> stoneblocks = new ArrayList<>();
    static ArrayList<Material> dirtblocks = new ArrayList<>();
    static ArrayList<Material> woodblocks = new ArrayList<>();

    SiegeCraft craft;
    UUID uniqueGameID = null;
    static{
        stoneblocks.add(Material.COBBLESTONE);
        stoneblocks.add(Material.ENDER_STONE);
        stoneblocks.add(Material.OBSIDIAN);
        dirtblocks.add(Material.DIRT);
        dirtblocks.add(Material.GRAVEL);
        dirtblocks.add(Material.SOUL_SAND);
        woodblocks.add(Material.WOOD);
        woodblocks.add(Material.LOG);
        woodblocks.add(Material.LOG_2);
    }

    public void generateUniqueID(){
        uniqueGameID = UUID.randomUUID();
    }

    SiegeData data;

    public SiegeArena(SiegeData data,String name){
        canjoin = false;
        craft = new SiegeCraft(data);
        this.data = data;
        Bukkit.getServer().getPluginManager().registerEvents(this,data.plugin);
        this.name = name;
        team1 = new SiegeTeam();
        team2 = new SiegeTeam();
        uniqueGameID = UUID.randomUUID();
    }


    @EventHandler
    public void onBlockPlaced(BlockPlaceEvent e){
        if(nowgame){
            if(team1.playerlist.contains(e.getPlayer().getUniqueId())||team2.playerlist.contains(e.getPlayer().getUniqueId())){
                if(e.getBlock().getLocation().getWorld().getName().equalsIgnoreCase(worldname)){
                    if(!placeblocklist.contains(e.getBlock())){
                        if(stoneblocks.contains(e.getBlock().getType())||dirtblocks.contains(e.getBlock().getType())||woodblocks.contains(e.getBlock().getType())||Material.WOOL==e.getBlock().getType()||Material.GLASS==e.getBlock().getType()){
                            if(team1.getLoc().distance(e.getBlock().getLocation())<5&&team2.playerlist.contains(e.getPlayer().getUniqueId())){
                                e.setCancelled(true);
                                data.showMessage(e.getPlayer().getUniqueId().toString(),"§c相手のチームのリス地半径5ブロック以内にはブロックを置けません！");
                                return;
                            }else if(team2.getLoc().distance(e.getBlock().getLocation())<5&&team1.playerlist.contains(e.getPlayer().getUniqueId())){
                                e.setCancelled(true);
                                data.showMessage(e.getPlayer().getUniqueId().toString(),"§c相手のチームのリス地半径5ブロック以内にはブロックを置けません！");
                                return;
                            }
                            placeblocklist.add(e.getBlock());
                        }else{
                            data.showMessage(e.getPlayer().getUniqueId().toString(),"§c置けないブロックです！");
                            e.setCancelled(true);
                        }
                    }
                }
            }
        }
    }


    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if(nowgame) {
            if (team1.playerlist.contains(e.getPlayer().getUniqueId()) || team2.playerlist.contains(e.getPlayer().getUniqueId())) {
                if(team1.getNexusloc().distance(e.getTo())>15&&team1.playerlist.contains(e.getPlayer().getUniqueId())){
                    if (wave == 0) {
                        e.getPlayer().teleport(team1.loc);
                        data.showMessage(e.getPlayer().getUniqueId().toString(),"§c準備ウェーブ中はネクサスから半径15ブロック以上には移動できません！");
                    }
                }else if(team2.getNexusloc().distance(e.getTo())>15&&team2.playerlist.contains(e.getPlayer().getUniqueId())){
                    if (wave == 0) {
                        e.getPlayer().teleport(team2.loc);
                        data.showMessage(e.getPlayer().getUniqueId().toString(),"§c準備ウェーブ中はネクサスから半径15ブロック以上には移動できません！");
                    }
                }
            }
        }
    }

    public boolean checkPickaxe(Material item){
        return item == Material.WOOD_PICKAXE || item == Material.STONE_PICKAXE
                || item == Material.IRON_PICKAXE || item == Material.GOLD_PICKAXE || item == Material.DIAMOND_PICKAXE;
    }

    public boolean checkShovel(Material item){
        return item == Material.WOOD_SPADE || item == Material.STONE_SPADE
                || item == Material.IRON_SPADE || item == Material.GOLD_SPADE || item == Material.DIAMOND_SPADE;
    }

    public boolean checkAxe(Material item){
        return item == Material.WOOD_AXE || item == Material.STONE_AXE
                || item == Material.IRON_AXE || item == Material.GOLD_AXE || item == Material.DIAMOND_AXE;
    }

    public void initScoreBoard(){
        scoreboard = new Scoreboard();
        scoreboard.setTitle("§d§lM§7§lSiege §ev1.0α");
        scoreboard.addScore("§c§lTEAM1 §eネクサスHP: §a"+team1.getNexushp(),10);
        scoreboard.addScore("§b§lTEAM2 §eネクサスHP: §a"+team2.getNexushp(),9);
        scoreboard.addScore("",8);
        scoreboard.addScore("§e§lウェーブ: "+wavetostring(),7);
        for(UUID uuid:team1.playerlist){
            Player p = Bukkit.getPlayer(uuid);
            if(p != null){
                scoreboard.setShowPlayer(p);
            }
        }
        for(UUID uuid:team2.playerlist){
            Player p = Bukkit.getPlayer(uuid);
            if(p != null){
                scoreboard.setShowPlayer(p);
            }
        }
    }

    public void resetScoreboard(){
        for(UUID uuid:team1.playerlist){
            Player p = Bukkit.getPlayer(uuid);
            if(p != null){
                p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            }
        }
        for(UUID uuid:team2.playerlist){
            Player p = Bukkit.getPlayer(uuid);
            if(p != null){
                p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            }
        }
    }

    public synchronized void updateScoreBoard_team1(int old_hp){
        scoreboard.removeScores("§c§lTEAM1 §eネクサスHP: §a"+old_hp);
        scoreboard.addScore("§c§lTEAM1 §eネクサスHP: §a"+team1.getNexushp(),10);
    }

    public synchronized void updateScoreBoard_team2(int old_hp){
        scoreboard.removeScores("§b§lTEAM2 §eネクサスHP: §a"+old_hp);
        scoreboard.addScore("§b§lTEAM2 §eネクサスHP: §a"+team2.getNexushp(),9);
    }

    public void updateScoreBoard_wave(int old_wave){
        scoreboard.removeScores("§e§lウェーブ: "+wavetostring(old_wave));
        scoreboard.addScore("§e§lウェーブ: "+wavetostring(),7);
    }

    public String wavetostring(){
        if(wave==0){
            return "§a§l準備";
        }else if(wave==4){
            return "§4§lサドンデス";
        }
        return "§6§l"+wave;
    }

    public String wavetostring(int wave){
        if(wave==0){
            return "§a§l準備";
        }else if(wave==4){
            return "§4§lサドンデス";
        }
        return "§6§l"+wave;
    }

    public void initBossbar(){
        bar = new SBossBar("§a§l準備ウェーブ: 残り3:00",BarColor.WHITE);
        for(UUID uuid:team1.playerlist){
            Player p = Bukkit.getPlayer(uuid);
            if(p != null){
                bar.addPlayer(p);
            }
        }
        for(UUID uuid:team2.playerlist){
            Player p = Bukkit.getPlayer(uuid);
            if(p != null){
                bar.addPlayer(p);
            }
        }
        bar.setVisible(true);
        BukkitRunnable task = new BukkitRunnable()  {
            int time = 180;
            UUID gameid = getUniqueGameID();
            @Override
            public void run(){
                if(!nowgame||getUniqueGameID()==null||!gameid.equals(getUniqueGameID())){
                    this.cancel();
                    return;
                }
                time--;
                if(time==0){
                    bar.setProgress(1);
                    wave1Bossbar();
                    this.cancel();
                    return;
                }
                bar.setTitle("§a§l準備ウェーブ: 残り"+inttotimestring(time));
                bar.takeProgress(0.005555);
            }
        };
        task.runTaskTimer(data.plugin,0,20);
    }

    public void wave1Bossbar(){
        bar.setTitle("§a§lウェーブ1: 残り10:00");
        BukkitRunnable task = new BukkitRunnable()  {
            int time = 600;
            UUID gameid = getUniqueGameID();
            @Override
            public void run(){
                if(!nowgame||getUniqueGameID()==null||!gameid.equals(getUniqueGameID())){
                    this.cancel();
                    return;
                }
                time--;
                if(time==0){
                    bar.setProgress(1);
                    wave2Bossbar();
                    this.cancel();
                    return;
                }
                bar.setTitle("§a§lウェーブ1: 残り"+inttotimestring(time));
                bar.takeProgress(0.00161);
            }
        };
        task.runTaskTimer(data.plugin,0,20);
    }

    public void wave2Bossbar(){
        bar.setTitle("§a§lウェーブ2: 残り10:00");
        BukkitRunnable task = new BukkitRunnable()  {
            int time = 600;
            UUID gameid = getUniqueGameID();
            @Override
            public void run(){
                if(!nowgame||getUniqueGameID()==null||!gameid.equals(getUniqueGameID())){
                    this.cancel();
                    return;
                }
                time--;
                if(time==0){
                    bar.setProgress(1);
                    wave3Bossbar();
                    this.cancel();
                    return;
                }
                bar.setTitle("§a§lウェーブ2: 残り"+inttotimestring(time));
                bar.takeProgress(0.00161);
            }
        };
        task.runTaskTimer(data.plugin,0,20);
    }

    public void wave3Bossbar(){
        bar.setTitle("§a§lウェーブ3: 残り10:00");
        BukkitRunnable task = new BukkitRunnable()  {
            int time = 600;
            UUID gameid = getUniqueGameID();
            @Override
            public void run(){
                if(!nowgame||getUniqueGameID()==null||!gameid.equals(getUniqueGameID())){
                    this.cancel();
                    return;
                }
                time--;
                if(time==0){
                    bar.setProgress(1);
                    wave4Bossbar();
                    this.cancel();
                    return;
                }
                bar.setTitle("§a§lウェーブ3: 残り"+inttotimestring(time));
                bar.takeProgress(0.00161);
            }
        };
        task.runTaskTimer(data.plugin,0,20);
    }

    public void wave4Bossbar(){
        bar.bar.setColor(BarColor.RED);
        bar.setTitle("§4§lサドンデス: 残り∞");
    }

    public void bossbarend(){
        bar.setVisible(false);
        bar.setProgress(1);
        for(Player p: bar.bar.getPlayers()){
            bar.removePlayer(p);
        }
    }

    public String inttotimestring(int time){
        int minute = 0;
        int second;
        while (time/60>=1){
            minute++;
            time = time - 60;
        }
        second = time;
        String times = minute+":"+second;
        if(second<10){
            times = minute+":0"+second;
        }
        return times;
    }

    @EventHandler
    public void onDeath(PlayerRespawnEvent event){
        if (nowgame) {
            if (team1.playerlist.contains(event.getPlayer().getUniqueId()) || team2.playerlist.contains(event.getPlayer().getUniqueId())) {
                Player p = event.getPlayer();
                p.sendMessage("§c§l死んでしまいました！リスポーンまで… §c残り: " + respawntime + "秒");
                if (team1.playerlist.contains(p.getUniqueId())) {
                    event.setRespawnLocation(team1.loc);
                } else {
                    event.setRespawnLocation(team2.loc);
                }
                p.setGameMode(GameMode.SPECTATOR);
                p.addPotionEffect(PotionEffectType.BLINDNESS.createEffect(respawntime * 20+20, 0));
                Bukkit.getScheduler().scheduleSyncDelayedTask(data.plugin, () -> {
                    if (!nowgame) {
                        p.setGameMode(GameMode.SURVIVAL);
                        Bukkit.dispatchCommand(p,"ewarp minigame_lobby");
                        return;
                    }
                    if (!team1.playerlist.contains(event.getPlayer().getUniqueId()) && !team2.playerlist.contains(event.getPlayer().getUniqueId())) {
                        p.setGameMode(GameMode.SURVIVAL);
                        Bukkit.dispatchCommand(p,"ewarp minigame_lobby");
                        return;
                    }
                    if (team1.playerlist.contains(p.getUniqueId())) {
                        p.teleport(team1.loc);
                    } else {
                        p.teleport(team2.loc);
                    }
                    p.setGameMode(GameMode.SURVIVAL);
                    p.addPotionEffect(PotionEffectType.DAMAGE_RESISTANCE.createEffect(100, 4));
                    p.sendMessage(data.plugin.getPrefix() + "§a§lリスポーンしました！ 無敵が切れるまで5秒…");
                    Bukkit.getScheduler().scheduleSyncDelayedTask(data.plugin, () -> p.sendMessage(data.plugin.getPrefix() + "§a§l無敵が切れました"), 100);
                }, respawntime * 20);
            }
        }
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event){
        if (nowgame) {
            if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
                Player damager = (Player) event.getDamager();
                Player attacker = (Player) event.getEntity();
                SiegeTeam team = data.tc.getTeam(damager);
                if (team == null) {
                    return;
                }
                if(wave == 0){
                    data.showMessage(damager.getUniqueId().toString(), "現在準備ウェーブ中です！");
                    event.setCancelled(true);
                }
                if (team1.playerlist.contains(damager.getUniqueId()) && team1.playerlist.contains(attacker.getUniqueId())) {
                    data.showMessage(damager.getUniqueId().toString(), "チームメイトには攻撃できません！");
                    event.setCancelled(true);
                } else if (team2.playerlist.contains(damager.getUniqueId()) && team2.playerlist.contains(attacker.getUniqueId())) {
                    data.showMessage(damager.getUniqueId().toString(), "チームメイトには攻撃できません！");
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player korosita = e.getEntity().getKiller();
        Player korosareta = e.getEntity();
        if(korosita==null){
            return;
        }
        String korositaname = korosita.getName();
        String korosaretaname = korosareta.getName();
        if (nowgame) {
            if (team1.playerlist.contains(korosareta.getUniqueId()) || team2.playerlist.contains(korosareta.getUniqueId())) {
                data.getStats(korosareta.getUniqueId().toString()).setDeath(data.getStats(korosareta.getUniqueId().toString()).getDeath()+1);
                data.showMessage(korosareta.getUniqueId().toString(),korosita.getName()+"§cにキルされた。");
                if (team1.playerlist.contains(korosareta.getUniqueId())){
                    korosaretaname = "§c[TEAM1]"+korosaretaname;
                }else{
                    korosaretaname = "§b[TEAM2]"+korosaretaname;
                }
            }
            if (team1.playerlist.contains(korosita.getUniqueId()) || team2.playerlist.contains(korosita.getUniqueId())) {
                data.getStats(korosita.getUniqueId().toString()).setKill(data.getStats(korosita.getUniqueId().toString()).getKill()+1);
                data.getStats(korosita.getUniqueId().toString()).setPoint(data.getStats(korosita.getUniqueId().toString()).getPoint()+4);
                data.showMessage(korosita.getUniqueId().toString(),korosaretaname+"§eをキルした。 +4point");
                if (team1.playerlist.contains(korosita.getUniqueId())){
                    korositaname = "§c[TEAM1]"+korositaname;
                }else{
                    korositaname = "§b[TEAM2]"+korositaname;
                }
            }
            e.setDeathMessage(null);
            data.showBattleMessage(name,korosaretaname+" §fは "+korositaname+" §fにキルされた");
        }
    }

    @EventHandler
    public void onBukkitFill(PlayerBucketFillEvent event){
        if (!event.getBucket().equals(Material.WATER_BUCKET)&&!event.getBucket().equals(Material.LAVA_BUCKET))
            return;
        Block water = event.getBlockClicked().getRelative(event.getBlockFace());
        if(!placeblocklist.contains(water)){
            event.setCancelled(true);
            return;
        }
        placeblocklist.remove(water);
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (!event.getBucket().equals(Material.WATER_BUCKET)&&!event.getBucket().equals(Material.LAVA_BUCKET))
            return;
        Block water = event.getBlockClicked().getRelative(event.getBlockFace());
        placeblocklist.add(water);
    }

    @EventHandler
    public void onBlockExpolode(BlockExplodeEvent e){
        if (nowgame) {
            if (e.getBlock().getLocation().getWorld().getName().equalsIgnoreCase(worldname)) {
                if(!placeblocklist.contains(e.getBlock())){
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreaked(BlockBreakEvent e) {
        if (nowgame) {
            if (e.getBlock().getLocation().getWorld().getName().equalsIgnoreCase(worldname)) {
                if (team1.playerlist.contains(e.getPlayer().getUniqueId()) || team2.playerlist.contains(e.getPlayer().getUniqueId())) {
                    if (placeblocklist.contains(e.getBlock())) {
                        if (stoneblocks.contains(e.getBlock().getType()) || dirtblocks.contains(e.getBlock().getType()) || woodblocks.contains(e.getBlock().getType())) {
                            if (stoneblocks.contains(e.getBlock().getType()) && checkPickaxe(e.getPlayer().getInventory().getItemInMainHand().getType())) {
                                placeblocklist.remove(e.getBlock());
                            } else if (dirtblocks.contains(e.getBlock().getType()) && checkShovel(e.getPlayer().getInventory().getItemInMainHand().getType())) {
                                placeblocklist.remove(e.getBlock());
                            } else if (woodblocks.contains(e.getBlock().getType()) && checkAxe(e.getPlayer().getInventory().getItemInMainHand().getType())) {
                                placeblocklist.remove(e.getBlock());
                            }else{
                                data.showMessage(e.getPlayer().getUniqueId().toString(), "§cブロックに対して対応するツールを使用しないと破壊できません！");
                                data.showMessage(e.getPlayer().getUniqueId().toString(), "§7石系: ツルハシ  §6土系: シャベル  §e木材系: 斧  §f羊毛:  ハサミ(その他ツール)");
                                e.setCancelled(true);
                            }
                        } else if (Material.WOOL == e.getBlock().getType()) {
                            if (Material.SHEARS == e.getPlayer().getInventory().getItemInMainHand().getType() || checkAxe(e.getPlayer().getInventory().getItemInMainHand().getType()) || checkPickaxe(e.getPlayer().getInventory().getItemInMainHand().getType()) || checkShovel(e.getPlayer().getInventory().getItemInMainHand().getType())) {
                                placeblocklist.remove(e.getBlock());
                            }else{
                                data.showMessage(e.getPlayer().getUniqueId().toString(), "§cハサミorその他のツールを使用しないと破壊できません！");
                                e.setCancelled(true);
                            }
                        } else if (Material.GLASS == e.getBlock().getType()) {
                            placeblocklist.remove(e.getBlock());
                        }
                    } else if (Material.IRON_ORE.equals(e.getBlock().getType())) {
                        BukkitRunnable task = new BukkitRunnable()  {
                            Material type = e.getBlock().getType();
                            byte b = e.getBlock().getData();
                            @Override
                            public void run(){
                                e.getBlock().setType(type);
                                e.getBlock().setData(b);
                                breakores.remove(this);
                                this.cancel();
                            }
                        };
                        task.runTaskTimer(data.plugin,400,0);
                        breakores.add(task);
                        e.setCancelled(true);
                        e.getBlock().setType(Material.AIR);
                        e.getPlayer().getInventory().addItem(new ItemStack(Material.IRON_INGOT));
                    } else if (team1.nexusloc.equals(e.getBlock().getLocation()) && team1.playerlist.contains(e.getPlayer().getUniqueId())
                            || team2.nexusloc.equals(e.getBlock().getLocation()) & team2.playerlist.contains(e.getPlayer().getUniqueId())) {
                        data.showMessage(e.getPlayer().getUniqueId().toString(), "§c自チームのネクサスは破壊できません！");
                        e.setCancelled(true);
                    } else if (team1.nexusloc.equals(e.getBlock().getLocation()) && team2.playerlist.contains(e.getPlayer().getUniqueId())) {
                        if (wave == 0) {
                            data.showMessage(e.getPlayer().getUniqueId().toString(), "§c現在準備ウェーブ中です!ネクサスの破壊はできません！");
                            e.setCancelled(true);
                            return;
                        }
                        /*
                        ここはチーム1のネクサスがチーム2のメンバーによって破壊された場合の処理
                         */
                        int oldhp = team1.nexushp;
                        team1.damagenexus(1);
                        updateScoreBoard_team1(oldhp);
                        data.showMessage(e.getPlayer().getUniqueId().toString(), "§c相手チームのネクサスにダメージを与えました！ 残りHP: §6" + team1.nexushp);
                        team1.teaminv.addItem(new ItemStack(Material.NETHER_STAR));
                        e.setCancelled(true);
                        if (team1.nexushp <= 0) {
                            data.gameEnd(name, 2);
                        }
                    } else if (team2.nexusloc.equals(e.getBlock().getLocation()) && team1.playerlist.contains(e.getPlayer().getUniqueId())) {
                        if (wave == 0) {
                            data.showMessage(e.getPlayer().getUniqueId().toString(), "§c現在準備ウェーブ中です!ネクサスの破壊はできません！");
                            e.setCancelled(true);
                            return;
                        }
                        /*
                        ここはチーム2のネクサスがチーム1のメンバーによって破壊された場合の処理
                         */
                        int oldhp = team2.nexushp;
                        team2.damagenexus(1);
                        updateScoreBoard_team2(oldhp);
                        e.setCancelled(true);
                        data.showMessage(e.getPlayer().getUniqueId().toString(), "§c相手チームのネクサスにダメージを与えました！ 残りHP: §6" + team2.nexushp);
                        team2.teaminv.addItem(new ItemStack(Material.NETHER_STAR));
                        team1.sendActionBarAllPlayer("§f§l"+e.getPlayer().getName()+"§c§lが相手のネクサスにダメージを与えました！ 残りHP: §6§l" + team2.nexushp);
                        if (team2.nexushp <= 0) {
                            data.gameEnd(name, 1);
                        }
                    }else{
                        data.showMessage(e.getPlayer().getUniqueId().toString(), "§c初期から配置されているブロックは壊せません！");
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    ArrayList<BukkitRunnable> breakores = new ArrayList<>();

    public void resetores(){
        for(BukkitRunnable run : breakores){
            run.run();
            run.cancel();
        }
        breakores = new ArrayList<>();
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent e){
        if (nowgame) {
            if (e.getLocation().getWorld().getName().equalsIgnoreCase(worldname)) {
                e.setCancelled(true);
            }
        }
    }



    public void resetUniqueID(){
        uniqueGameID = null;
    }

    public UUID getUniqueGameID() {
        return uniqueGameID;
    }

    public void saveFile(){
        if(worldname==null||team1==null||team2==null||nowgame){
            return;
        }
        File folder = new File(data.plugin.getDataFolder(), File.separator + "Arenas");
        if(!folder.exists()){
            folder.mkdir();
        }
        File f = new File(folder, File.separator + name + ".yml");
        FileConfiguration data = YamlConfiguration.loadConfiguration(f);

        f.delete();

        if (!f.exists()) {
            try {
                data.set("name", name);
                data.set("worldname", worldname);
                data.set("lobby", lobbyloc);
                data.set("team1.loc", team1.getLoc());
                data.set("team1.nexusloc", team1.getNexusloc());
                data.set("team2.loc", team2.getLoc());
                data.set("team2.nexusloc", team2.getNexusloc());
                data.set("nexushp", hp);
                data.save(f);
                canjoin = true;
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    public void loadFile(){
        if(nowgame){
            return;
        }
        File folder = new File(data.plugin.getDataFolder(), File.separator + "Arenas");
        if(!folder.exists()){
            folder.mkdir();
        }
        File f = new File(folder, File.separator + name + ".yml");
        FileConfiguration data = YamlConfiguration.loadConfiguration(f);

        if (f.exists()) {
            name = data.getString("name");
            worldname = data.getString("worldname");
            lobbyloc = (Location) data.get("lobby");
            SiegeTeam team1 = new SiegeTeam();
            team1.loc = (Location) data.get("team1.loc");
            team1.nexusloc = (Location) data.get("team1.nexusloc");
            SiegeTeam team2 = new SiegeTeam();
            team2.loc = (Location) data.get("team2.loc");
            team2.nexusloc = (Location) data.get("team2.nexusloc");
            hp = data.getInt("nexushp");
            this.team1 = team1;
            this.team2 = team2;
            team1.nexushp = hp;
            team2.nexushp = hp;
        }
    }

    public void resetWorld(){
        canjoin = false;
        BukkitRunnable task = new BukkitRunnable()  {
            @Override
            public void run(){
                if(placeblocklist.size() == 0){
                    canjoin = true;
                    setNowgame(false);
                    this.cancel();
                    return;
                }
                placeblocklist.get(0).setType(Material.AIR);
                placeblocklist.remove(0);
            }
        };

        task.runTaskTimer(data.plugin,0,1);
    }

    public String getName() {
        return name;
    }

    public SiegeTeam getTeam1() {
        return team1;
    }

    public SiegeTeam getTeam2() {
        return team2;
    }

    public String getWorldname() {
        return worldname;
    }

    public void setTeam1(SiegeTeam team1) {
        this.team1 = team1;
    }

    public void setTeam2(SiegeTeam team2) {
        this.team2 = team2;
    }

    public void setWorldname(String worldname) {
        this.worldname = worldname;
    }

    public boolean isCanjoin() {
        return canjoin;
    }

    public int getHp(){
        return hp;
    }

    public boolean isNowgame() {
        return nowgame;
    }

    public void setCanjoin(boolean canjoin) {
        this.canjoin = canjoin;
    }

    public void setNowgame(boolean nowgame) {
        this.nowgame = nowgame;
    }

    public int getWave() {
        return wave;
    }

    public void setWave(int wave) {
        this.wave = wave;
        if(wave == 1){
            setRespawntime(5);
        }else if(wave == 2){
            setRespawntime(10);
        }else if(wave == 3){
            setRespawntime(15);
        }else if(wave == 4){
            setRespawntime(30);
        }else if(wave == -1){
            setRespawntime(5);
        }
    }

    public void setRespawntime(int respawntime){
        this.respawntime = respawntime;
    }

    public int getRespawntime() {
        return respawntime;
    }
}
