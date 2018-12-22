package red.man10.msiege;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import red.man10.msiege.util.InventoryAPI;

import java.util.*;

public class SiegeCommand implements CommandExecutor,Listener{

    SiegeData data;
    String cmd;
    GUIManager gui;
    HashMap<UUID,SiegeArena> arenasetup = new HashMap<>();
    HashMap<UUID,SiegeCard> cardsetup = new HashMap<>();
    HashMap<UUID,String> arenasetups = new HashMap<>();
    HashMap<UUID,CraftData> craftsetup = new HashMap<>();

    public SiegeCommand(SiegeData data,String cmd){
        this.data = data;
        this.cmd = cmd;
        data.plugin.getCommand(cmd).setExecutor(this);
        Bukkit.getServer().getPluginManager().registerEvents(this,data.plugin);
        gui = new GUIManager(data);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if(arenasetups.containsKey(p.getUniqueId())){
                if(arenasetups.get(p.getUniqueId()).equalsIgnoreCase("nexus1")){
                    arenasetup.get(p.getUniqueId()).team1.nexusloc = e.getClickedBlock().getLocation();
                }else{
                    arenasetup.get(p.getUniqueId()).team2.nexusloc = e.getClickedBlock().getLocation();
                }
                data.showMessage(p.getUniqueId().toString(),"§a"+arenasetups.get(p.getUniqueId())+"をセットしました。");
                arenasetups.remove(p.getUniqueId());
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player p = (Player) sender;
        if (!p.hasPermission("siege.use")) {
            p.sendMessage(data.plugin.getPrefix() + "§cあなたには権限がありません！");
            return true;
        }
        if(args.length == 0) {
            gui.openMain(p);
            return true;
        }else if(args.length == 1){
            if(args[0].equalsIgnoreCase("list")){
                p.sendMessage("§e------------アリーナリスト-------------");
                for(String str:data.list()){
                    data.showMessage(p.getUniqueId().toString(),"§a"+str);
                }
            }else if(args[0].equalsIgnoreCase("leave")){
                SiegeArena arena = null;
                for(String str:data.list()){
                    if(data.getArena(str).team1.playerlist.contains(p.getUniqueId())||data.getArena(str).team2.playerlist.contains(p.getUniqueId())){
                        arena = data.getArena(str);
                        break;
                    }
                }
                if(arena==null){
                    data.showMessage(p.getUniqueId().toString(),"§cあなたはどのゲームにも参加していません！");
                    return true;
                }
                if(arena.isNowgame()) {
                    data.showMessage(p.getUniqueId().toString(),"§c"+arena.getName()+"は現在ゲーム中です！");
                    return true;
                }
                if(arena.team1.playerlist.contains(p.getUniqueId())){
                    arena.team1.playerlist.remove(p.getUniqueId());
                }else{
                    arena.team2.playerlist.remove(p.getUniqueId());
                }
                p.getInventory().clear();
                arena.votedamount.remove(p.getUniqueId());
                PlayerStats stats = data.getStats(p.getUniqueId().toString());
                stats.resetJoinArena();
                stats.setJoingameuniqueid(null);
                stats.saveFile();
                data.showMessage(p.getUniqueId().toString(),"§a"+arena.getName()+"から抜けました");
            }else if(args[0].equalsIgnoreCase("stop1")){
                if (!p.hasPermission("siege.setup")) {
                    data.showMessage(p.getUniqueId().toString(),"§cあなたには権限がありません！");
                    return true;
                }
                SiegeArena arena = null;
                for(String str:data.list()){
                    if(data.getArena(str).team1.playerlist.contains(p.getUniqueId())||data.getArena(str).team2.playerlist.contains(p.getUniqueId())){
                        arena = data.getArena(str);
                        break;
                    }
                }
                if(arena==null){
                    data.showMessage(p.getUniqueId().toString(),"§cあなたはどのゲームにも参加していません！");
                    return true;
                }
                data.gameEnd(arena.getName(),1);
            }else if(args[0].equalsIgnoreCase("stop2")){
                if (!p.hasPermission("siege.forcestart")) {
                    data.showMessage(p.getUniqueId().toString(),"§cあなたには権限がありません！");
                    return true;
                }
                SiegeArena arena = null;
                for(String str:data.list()){
                    if(data.getArena(str).team1.playerlist.contains(p.getUniqueId())||data.getArena(str).team2.playerlist.contains(p.getUniqueId())){
                        arena = data.getArena(str);
                        break;
                    }
                }
                if(arena==null){
                    data.showMessage(p.getUniqueId().toString(),"§cあなたはどのゲームにも参加していません！");
                    return true;
                }
                data.gameEnd(arena.getName(),2);
            }else if(args[0].equalsIgnoreCase("help")){
                if (p.hasPermission("siege.setup")) {
                    p.sendMessage("§4------------Admin用-------------");
                    p.sendMessage("§c/" + cmd + " create/update [アリーナ名] : [アリーナ名]のアリーナのセットアップ/再設定を開始します。");
                    p.sendMessage("§c/" + cmd + " loc1 [アリーナ名] : アリーナのスポーン1をセット");
                    p.sendMessage("§c/" + cmd + " loc2 [アリーナ名] : アリーナのスポーン2をセット");
                    p.sendMessage("§c/" + cmd + " nexusloc1 [アリーナ名] : アリーナのネクサス1をセット");
                    p.sendMessage("§c/" + cmd + " nexusloc2 [アリーナ名] : アリーナのネクサス2をセット");
                    p.sendMessage("§c/" + cmd + " world [アリーナ名] : 今いるワールドをマップとしてセット");
                    p.sendMessage("§c/" + cmd + " save [アリーナ名] : アリーナのデータをセーブ");
                    p.sendMessage("§c/" + cmd + " setlobby [アリーナ名] : アリーナのデータをセーブ");
                    p.sendMessage("§c/" + cmd + " stop1 : ゲームを強制終了(team1勝利)する");
                    p.sendMessage("§c/" + cmd + " stop2 : ゲームを強制終了(team2勝利)する");
                    p.sendMessage("§c-------------Card関連-------------");
                    p.sendMessage("§c/" + cmd + " card create [カード名] : カードを作成する");
                    p.sendMessage("§c/" + cmd + " card lore [lore1] [lore2] …: カードのloreを設定する");
                    p.sendMessage("§c/" + cmd + " card type item,command,armor : カードのタイプを設定する");
                    p.sendMessage("§c/" + cmd + " card additem : カードのアイテムを追加する");
                    p.sendMessage("§c/" + cmd + " card clearitem : カードのアイテムをクリアする");
                    p.sendMessage("§c/" + cmd + " card setcost [数字]: カードのコストを設定する");
                    p.sendMessage("§c/" + cmd + " card arg [arg1] [arg2] [arg3] …: カードのargを設定する");
                    p.sendMessage("§c/" + cmd + " card save : カードを保存する");
                    p.sendMessage("§c/" + cmd + " card list : カードリストを表示する");
                    p.sendMessage("§c/" + cmd + " card sellist : セットカードリストを表示する");
                    p.sendMessage("§c/" + cmd + " card havelist : 所持カードリストを表示する");
                    p.sendMessage("§c/" + cmd + " cardget [カード名] : カードを所持します。");
                    p.sendMessage("§c/" + cmd + " cardset [カード名] : カードをセットします。");
                    p.sendMessage("§c/" + cmd + " carddrop [カード名] : カードを捨てます。");
                    p.sendMessage("§c/" + cmd + " cardunset [カード名] : カードをアンセットします。");
                    p.sendMessage("§c-------------Shop関連-------------");
                    p.sendMessage("§c/" + cmd + " shoprel : shopをリロードします。");
                    p.sendMessage("§c/" + cmd + " addshop [name] [buy] : shopの品を追加します");
                    p.sendMessage("§c-------------Craft関連-------------");
                    p.sendMessage("§c/" + cmd + " craftnew [name] : craftをセットアップします");
                    p.sendMessage("§c/" + cmd + " craft setitem: craftの発動アイテムをセットします");
                    p.sendMessage("§c/" + cmd + " craft adduse: craftの材料をセットします");
                    p.sendMessage("§c/" + cmd + " craft clearuse: craftの材料をリセットします");
                    p.sendMessage("§c/" + cmd + " craft setresult: craftで得られるアイテムをセットします");
                    p.sendMessage("§c/" + cmd + " craft save : craftをセーブ");
                }
                if (p.hasPermission("siege.forcestart")) {
                    p.sendMessage("§4------------Admin/Vip用-------------");
                    p.sendMessage("§c/" + cmd + " start [アリーナ名] : ゲームを強制開始する");
                }
                p.sendMessage("§e------------User用-------------");
                p.sendMessage("§a/" + cmd + " join [アリーナ名] : アリーナに参加");
                p.sendMessage("§a/" + cmd + " leave : アリーナから抜ける");
                p.sendMessage("§a/" + cmd + " list : アリーナのリスト");
                p.sendMessage("§a/" + cmd + " stats : マイステータスを見る");
                p.sendMessage("§e-----------ゲーム中-------------");
                p.sendMessage("§a/" + cmd + " teamchest : チームのチェストを開く");
                p.sendMessage("§a/" + cmd + " mychest : マイチェスト(エンチェス)を開く");
                p.sendMessage("§a/" + cmd + " ib : アイテムバンクを開く");
                p.sendMessage("§bCreated by Mr_IK");
                p.sendMessage("§bBuildID: "+data.plugin.buildcode);
                return true;
            }else if(args[0].equalsIgnoreCase("teamchest")){
                SiegeTeam team = data.tc.getTeam(p);
                if(team == null){
                    data.showMessage(p.getUniqueId().toString(),"§aゲームに参加していません");
                    return true;
                }
                team.openteaminv(p);
            }else if(args[0].equalsIgnoreCase("mychest")) {
                SiegeTeam team = data.tc.getTeam(p);
                if (team == null) {
                    data.showMessage(p.getUniqueId().toString(), "§aゲームに参加していません");
                    return true;
                }
                p.openInventory(p.getEnderChest());
            }else if(args[0].equalsIgnoreCase("ib")) {
                SiegeTeam team = data.tc.getTeam(p);
                if (team == null) {
                    data.showMessage(p.getUniqueId().toString(), "§aゲームに参加していません");
                    return true;
                }
                gui.openItemBank(p, new InventoryAPI(data.plugin, "§d§lM§7§lSiege §3§lItemBank", 27),false);
            }else if(args[0].equalsIgnoreCase("stats")){
                p.sendMessage("§e------------"+p.getName()+"のステータス-------------");
                PlayerStats stats = data.getStats(p.getUniqueId().toString());
                data.showMessage(p.getUniqueId().toString(),"§aWin: "+stats.getWin());
                data.showMessage(p.getUniqueId().toString(),"§cLose: "+stats.getLose());
                data.showMessage(p.getUniqueId().toString(),"§aKill: "+stats.getKill());
                data.showMessage(p.getUniqueId().toString(),"§cDeath: "+stats.getDeath());
                data.showMessage(p.getUniqueId().toString(),"§eMatch: "+stats.getMatch());
                data.showMessage(p.getUniqueId().toString(),"§ePoint: "+stats.getPoint());
            }else if(args[0].equalsIgnoreCase("shoprel")){
                if (!p.hasPermission("siege.setup")) {
                    p.sendMessage(data.plugin.getPrefix() + "§cあなたには権限がありません！");
                    return true;
                }
                data.plugin.reloadConfig();
                data.plugin.config = data.plugin.getConfig();
                data.showMessage(p.getUniqueId().toString(),"§ashopを再読み込みしました。");
            }
        }else if(args.length == 2){
            if(args[0].equalsIgnoreCase("create")){
                if (!p.hasPermission("siege.setup")) {
                    p.sendMessage(data.plugin.getPrefix() + "§cあなたには権限がありません！");
                    return true;
                }
                if(data.list().contains(args[1])){
                    data.showMessage(p.getUniqueId().toString(),"§cその名前のアリーナはすでに存在します！ もしかして: /"+cmd+" update "+args[1]);
                    return true;
                }
                arenasetup.put(p.getUniqueId(),new SiegeArena(data,args[1]));
                data.showMessage(p.getUniqueId().toString(),"§aセットアップを開始しました。");
            }else if(args[0].equalsIgnoreCase("craftnew")){
                if (!p.hasPermission("siege.craft")) {
                    p.sendMessage(data.plugin.getPrefix() + "§cあなたには権限がありません！");
                    return true;
                }
                craftsetup.put(p.getUniqueId(),new CraftData(data,args[1]));
                data.showMessage(p.getUniqueId().toString(),"§aセットアップを開始しました。");
            }else if(args[0].equalsIgnoreCase("craft")){
                if (!p.hasPermission("siege.craft")) {
                    p.sendMessage(data.plugin.getPrefix() + "§cあなたには権限がありません！");
                    return true;
                }
                if(args[1].equalsIgnoreCase("save")) {
                    if(!craftsetup.containsKey(p.getUniqueId())){
                        data.showMessage(p.getUniqueId().toString(),"§cセットアップ/再設定が開始されていません！ もしかして: /"+cmd+" craftnew");
                        return true;
                    }
                    craftsetup.get(p.getUniqueId()).saveFile();
                    craftsetup.remove(p.getUniqueId());
                    data.showMessage(p.getUniqueId().toString(), "§aクラフトをセーブし、セットアップを終了しました。");
                }else if(args[1].equalsIgnoreCase("adduse")) {
                    if(!craftsetup.containsKey(p.getUniqueId())){
                        data.showMessage(p.getUniqueId().toString(),"§cセットアップ/再設定が開始されていません！ もしかして: /"+cmd+" craftnew");
                        return true;
                    }
                    if(p.getInventory().getItemInMainHand()==null){
                        data.showMessage(p.getUniqueId().toString(), "§cアイテムを持ってください。");
                        return true;
                    }
                    craftsetup.get(p.getUniqueId()).useitems.add( p.getInventory().getItemInMainHand());
                    data.showMessage(p.getUniqueId().toString(), "§aクラフトの使用アイテムを追加しました。");
                }else if(args[1].equalsIgnoreCase("setitem")) {
                    if(!craftsetup.containsKey(p.getUniqueId())){
                        data.showMessage(p.getUniqueId().toString(),"§cセットアップ/再設定が開始されていません！ もしかして: /"+cmd+" craftnew");
                        return true;
                    }
                    if(p.getInventory().getItemInMainHand()==null){
                        data.showMessage(p.getUniqueId().toString(), "§cアイテムを持ってください。");
                        return true;
                    }
                    craftsetup.get(p.getUniqueId()).book = p.getInventory().getItemInMainHand();
                    data.showMessage(p.getUniqueId().toString(), "§aクラフトの本をセットしました。");
                }else if(args[1].equalsIgnoreCase("setresult")) {
                    if(!craftsetup.containsKey(p.getUniqueId())){
                        data.showMessage(p.getUniqueId().toString(),"§cセットアップ/再設定が開始されていません！ もしかして: /"+cmd+" craftnew");
                        return true;
                    }
                    if(p.getInventory().getItemInMainHand()==null){
                        data.showMessage(p.getUniqueId().toString(), "§cアイテムを持ってください。");
                        return true;
                    }
                    craftsetup.get(p.getUniqueId()).result = p.getInventory().getItemInMainHand();
                    data.showMessage(p.getUniqueId().toString(), "§aクラフトのリザルトをセットしました。");
                }else if(args[1].equalsIgnoreCase("clearuse")) {
                    if(!craftsetup.containsKey(p.getUniqueId())){
                        data.showMessage(p.getUniqueId().toString(),"§cセットアップ/再設定が開始されていません！ もしかして: /"+cmd+" craftnew");
                        return true;
                    }
                    craftsetup.get(p.getUniqueId()).useitems.clear();
                    data.showMessage(p.getUniqueId().toString(), "§aクラフトの使用アイテムをクリアしました。");
                }
            }else if(args[0].equalsIgnoreCase("stats")){
                if (!p.hasPermission("siege.otherstats")) {
                    p.sendMessage(data.plugin.getPrefix() + "§cあなたには権限がありません！");
                    return true;
                }
                if(Bukkit.getPlayer(args[1])==null||data.getStats(Bukkit.getPlayer(args[1]).getUniqueId().toString())==null){
                    data.showMessage(p.getUniqueId().toString(),"§cそのプレイヤーはオフラインです！");
                    return true;
                }
                p.sendMessage("§e------------"+args[1]+"のステータス-------------");
                PlayerStats stats = data.getStats(p.getUniqueId().toString());
                data.showMessage(p.getUniqueId().toString(),"§aWin: "+stats.getWin());
                data.showMessage(p.getUniqueId().toString(),"§cLose: "+stats.getLose());
                data.showMessage(p.getUniqueId().toString(),"§aKill: "+stats.getKill());
                data.showMessage(p.getUniqueId().toString(),"§cDeath: "+stats.getDeath());
                data.showMessage(p.getUniqueId().toString(),"§eMatch: "+stats.getMatch());
                data.showMessage(p.getUniqueId().toString(),"§ePoint: "+stats.getPoint());
            }else if(args[0].equalsIgnoreCase("update")){
                if (!p.hasPermission("siege.setup")) {
                    p.sendMessage(data.plugin.getPrefix() + "§cあなたには権限がありません！");
                    return true;
                }
                if(!data.list().contains(args[1])){
                    data.showMessage(p.getUniqueId().toString(),"§cその名前のアリーナは存在しません！ もしかして: /"+cmd+" create "+args[1]);
                    return true;
                }
                arenasetup.put(p.getUniqueId(),data.getArena(args[1]));
                data.showMessage(p.getUniqueId().toString(),"§a再セットアップを開始しました。");
            }else if(args[0].equalsIgnoreCase("setlobby")) {
                if (!p.hasPermission("siege.setup")) {
                    p.sendMessage(data.plugin.getPrefix() + "§cあなたには権限がありません！");
                    return true;
                }
                if(!arenasetup.containsKey(p.getUniqueId())){
                    data.showMessage(p.getUniqueId().toString(),"§cセットアップ/再設定が開始されていません！ もしかして: /"+cmd+" create/update "+args[1]);
                    return true;
                }
                arenasetup.get(p.getUniqueId()).lobbyloc = p.getLocation();
                data.showMessage(p.getUniqueId().toString(),"§aロビーをセットしました。");
            }else if(args[0].equalsIgnoreCase("loc1")) {
                if (!p.hasPermission("siege.setup")) {
                    p.sendMessage(data.plugin.getPrefix() + "§cあなたには権限がありません！");
                    return true;
                }
                if(!arenasetup.containsKey(p.getUniqueId())){
                    data.showMessage(p.getUniqueId().toString(),"§cセットアップ/再設定が開始されていません！ もしかして: /"+cmd+" create/update "+args[1]);
                    return true;
                }
                arenasetup.get(p.getUniqueId()).team1.loc = p.getLocation();
                data.showMessage(p.getUniqueId().toString(),"§aloc1をセットしました。");
            }else if(args[0].equalsIgnoreCase("loc2")){
                if (!p.hasPermission("siege.setup")) {
                    p.sendMessage(data.plugin.getPrefix() + "§cあなたには権限がありません！");
                    return true;
                }
                if(!arenasetup.containsKey(p.getUniqueId())){
                    data.showMessage(p.getUniqueId().toString(),"§cセットアップ/再設定が開始されていません！ もしかして: /"+cmd+" create/update "+args[1]);
                    return true;
                }
                arenasetup.get(p.getUniqueId()).team2.loc = p.getLocation();
                data.showMessage(p.getUniqueId().toString(),"§aloc2をセットしました。");
            }else if(args[0].equalsIgnoreCase("nexusloc1")){
                if (!p.hasPermission("siege.setup")) {
                    p.sendMessage(data.plugin.getPrefix() + "§cあなたには権限がありません！");
                    return true;
                }
                if(!arenasetup.containsKey(p.getUniqueId())){
                    data.showMessage(p.getUniqueId().toString(),"§cセットアップ/再設定が開始されていません！ もしかして: /"+cmd+" create/update "+args[1]);
                    return true;
                }
                arenasetups.put(p.getUniqueId(),"nexus1");
                data.showMessage(p.getUniqueId().toString(),"§aネクサス1にしたいブロックを右クリックしてください！");
            }else if(args[0].equalsIgnoreCase("nexusloc2")){
                if (!p.hasPermission("siege.setup")) {
                    p.sendMessage(data.plugin.getPrefix() + "§cあなたには権限がありません！");
                    return true;
                }
                if(!arenasetup.containsKey(p.getUniqueId())){
                    data.showMessage(p.getUniqueId().toString(),"§cセットアップ/再設定が開始されていません！ もしかして: /"+cmd+" create/update "+args[1]);
                    return true;
                }
                arenasetups.put(p.getUniqueId(),"nexus2");
                data.showMessage(p.getUniqueId().toString(),"§aネクサス2にしたいブロックを右クリックしてください！");
            }else if(args[0].equalsIgnoreCase("world")){
                if (!p.hasPermission("siege.setup")) {
                    p.sendMessage(data.plugin.getPrefix() + "§cあなたには権限がありません！");
                    return true;
                }
                if(!arenasetup.containsKey(p.getUniqueId())){
                    data.showMessage(p.getUniqueId().toString(),"§cセットアップ/再設定が開始されていません！ もしかして: /"+cmd+" create/update "+args[1]);
                    return true;
                }
                arenasetup.get(p.getUniqueId()).worldname = p.getLocation().getWorld().getName();
                data.showMessage(p.getUniqueId().toString(),"§a"+p.getLocation().getWorld().getName()+"をマップとして登録しました。");
            }else if(args[0].equalsIgnoreCase("save")){
                if (!p.hasPermission("siege.setup")) {
                    p.sendMessage(data.plugin.getPrefix() + "§cあなたには権限がありません！");
                    return true;
                }
                if(!arenasetup.containsKey(p.getUniqueId())){
                    data.showMessage(p.getUniqueId().toString(),"§cセットアップ/再設定が開始されていません！ もしかして: /"+cmd+" create/update "+args[1]);
                    return true;
                }
                arenasetup.get(p.getUniqueId()).saveFile();
                data.arenalist.add(arenasetup.get(p.getUniqueId()));
                arenasetup.remove(p.getUniqueId());
                arenasetups.remove(p.getUniqueId());
                data.showMessage(p.getUniqueId().toString(),"§aアリーナをセーブしセットアップモードを解除しました。");
            }else if(args[0].equalsIgnoreCase("start")){
                if (!p.hasPermission("siege.forcestart")) {
                    p.sendMessage(data.plugin.getPrefix() + "§cあなたには権限がありません！");
                    return true;
                }
                if(!data.list().contains(args[1])){
                    data.showMessage(p.getUniqueId().toString(),"§cその名前のアリーナは存在しません！ もしかして: /"+cmd+" create "+args[1]);
                    return true;
                }
                data.gameStart(args[1]);
            }else if(args[0].equalsIgnoreCase("join")){
                if(!data.list().contains(args[1])){
                    data.showMessage(p.getUniqueId().toString(),"§cその名前のアリーナは存在しません！ もしかして: /"+cmd+" create "+args[1]);
                    return true;
                }
                data.gameJoin(args[1],p);
            }else if(args[0].equalsIgnoreCase("carddrop")){
                if(!data.getStats(p.getUniqueId().toString()).getHavecard().contains(args[1])) {
                    data.showMessage(p.getUniqueId().toString(), "§cこのカードは所持していません！");
                    return true;
                }
                data.getStats(p.getUniqueId().toString()).removehavecard(args[1]);
                data.getStats(p.getUniqueId().toString()).removeselectcard(args[1]);
                data.showMessage(p.getUniqueId().toString(),"§aカード「"+args[1]+"」を捨てました。");
                data.getStats(p.getUniqueId().toString()).saveFile();
                return true;
            }else if(args[0].equalsIgnoreCase("cardget")){
                if (!p.hasPermission("siege.getcard")) {
                    p.sendMessage(data.plugin.getPrefix() + "§cあなたには権限がありません！");
                    return true;
                }
                if(data.cardlist().contains(args[1])){
                    if(data.getStats(p.getUniqueId().toString()).getHavecard().contains(args[1])){
                        data.showMessage(p.getUniqueId().toString(),"§cこのカードはすでに取得済みです！");
                        return true;
                    }
                    data.getStats(p.getUniqueId().toString()).addhavecard(args[1]);
                    data.showMessage(p.getUniqueId().toString(),"§aカード「"+args[1]+"」を取得しました。");
                    data.getStats(p.getUniqueId().toString()).saveFile();
                    return true;
                }
            }else if(args[0].equalsIgnoreCase("cardset")){
                if(data.getStats(p.getUniqueId().toString()).getHavecard().contains(args[1])){
                    if(data.getPlayerHaveCost(p)-data.getCard(args[1]).cost<0){
                        data.showMessage(p.getUniqueId().toString(),"§cコストオーバーです！ 残りコスト: §e"+data.getPlayerHaveCost(p));
                        return true;
                    }else if(data.getStats(p.getUniqueId().toString()).getSelectcard().contains(args[1])){
                        data.showMessage(p.getUniqueId().toString(),"§cこのカードはすでにセット済みです！ 残りコスト: §e"+data.getPlayerHaveCost(p));
                        return true;
                    }
                    data.getStats(p.getUniqueId().toString()).addselectcard(args[1]);
                    data.showMessage(p.getUniqueId().toString(),"§aカード「"+args[1]+"」をセットしました。 残りコスト: §e"+data.getPlayerHaveCost(p));
                    data.getStats(p.getUniqueId().toString()).saveFile();
                    return true;
                }
            }else if(args[0].equalsIgnoreCase("cardunset")){
                if(!data.getStats(p.getUniqueId().toString()).getSelectcard().contains(args[1])){
                    data.showMessage(p.getUniqueId().toString(),"§cこのカードはセットされていません！");
                    return true;
                }
                data.getStats(p.getUniqueId().toString()).removeselectcard(args[1]);
                data.showMessage(p.getUniqueId().toString(),"§aカード「"+args[1]+"」をアンセットしました。");
                data.getStats(p.getUniqueId().toString()).saveFile();
                return true;
            }else if(args[0].equalsIgnoreCase("card")){
                if(args[1].equalsIgnoreCase("list")) {
                    p.sendMessage("§e------------カードリスト-------------");
                    for(String str:data.cardlist()){
                        data.showMessage(p.getUniqueId().toString(),"§a"+str);
                    }
                }else if(args[1].equalsIgnoreCase("havelist")) {
                    p.sendMessage("§e------------所持カードリスト-------------");
                    for(String str:data.getStats(p.getUniqueId().toString()).getHavecard()){
                        data.showMessage(p.getUniqueId().toString(),"§a"+str);
                    }
                }else if(args[1].equalsIgnoreCase("sellist")) {
                    p.sendMessage("§e------------セットカードリスト-------------");
                    for(String str:data.getStats(p.getUniqueId().toString()).getSelectcard()){
                        data.showMessage(p.getUniqueId().toString(),"§a"+str);
                    }
                }
                if (!p.hasPermission("siege.setupcard")) {
                    p.sendMessage(data.plugin.getPrefix() + "§cあなたには権限がありません！");
                    return true;
                }
                if(args[1].equalsIgnoreCase("save")) {
                    cardsetup.get(p.getUniqueId()).saveFile();
                    data.cardlist.add(cardsetup.get(p.getUniqueId()));
                    cardsetup.remove(p.getUniqueId());
                    data.showMessage(p.getUniqueId().toString(), "§aカードをセーブし、セットアップを終了しました。");
                }else if(args[1].equalsIgnoreCase("additem")) {
                    if(!cardsetup.containsKey(p.getUniqueId())){
                        data.showMessage(p.getUniqueId().toString(),"§cセットアップ/再設定が開始されていません！ もしかして: /"+cmd+" card create");
                        return true;
                    }
                    if(p.getInventory().getItemInMainHand()==null){
                        data.showMessage(p.getUniqueId().toString(), "§cアイテムを持ってください。");
                        return true;
                    }
                    cardsetup.get(p.getUniqueId()).item.add( p.getInventory().getItemInMainHand());
                    data.showMessage(p.getUniqueId().toString(), "§aカードのアイテムを追加しました。");
                }else if(args[1].equalsIgnoreCase("clearitem")) {
                    if(!cardsetup.containsKey(p.getUniqueId())){
                        data.showMessage(p.getUniqueId().toString(),"§cセットアップ/再設定が開始されていません！ もしかして: /"+cmd+" card create");
                        return true;
                    }
                    cardsetup.get(p.getUniqueId()).item.clear();
                    data.showMessage(p.getUniqueId().toString(), "§aカードのアイテムをクリアしました。");
                }else if(args[1].equalsIgnoreCase("reload")) {
                    data.cardlist.clear();
                    for(String str: data.cardlist()){
                        SiegeCard card = new SiegeCard(data,str,false);
                        data.cardlist.add(card);
                    }
                    data.showMessage(p.getUniqueId().toString(), "§aカードを再読み込みしました。");
                }
            }
        }else if(args.length == 3){
            if(args[0].equalsIgnoreCase("card")){
                if (!p.hasPermission("siege.setupcard")) {
                    p.sendMessage(data.plugin.getPrefix() + "§cあなたには権限がありません！");
                    return true;
                }
                if(args[1].equalsIgnoreCase("create")) {
                    cardsetup.put(p.getUniqueId(), new SiegeCard(data, args[2],true));
                    data.showMessage(p.getUniqueId().toString(), "§aカードのセットアップを開始しました。");
                }else if(args[1].equalsIgnoreCase("type")) {
                    if(!cardsetup.containsKey(p.getUniqueId())){
                        data.showMessage(p.getUniqueId().toString(),"§cセットアップ/再設定が開始されていません！ もしかして: /"+cmd+" card create");
                        return true;
                    }
                    if(!args[2].equalsIgnoreCase("command")&&!args[2].equalsIgnoreCase("item")&&!args[2].equalsIgnoreCase("armor")){
                        data.showMessage(p.getUniqueId().toString(), "§cタイプ名が不明です。");
                        return true;
                    }
                    cardsetup.get(p.getUniqueId()).type = SiegeCard.CardType.valueOf(args[2]);
                    data.showMessage(p.getUniqueId().toString(), "§aカードのタイプをセットしました。");
                }else if(args[1].equalsIgnoreCase("setcost")) {
                    if(!cardsetup.containsKey(p.getUniqueId())){
                        data.showMessage(p.getUniqueId().toString(),"§cセットアップ/再設定が開始されていません！ もしかして: /"+cmd+" card create");
                        return true;
                    }
                    int cost;
                    try {
                        cost = Integer.parseInt(args[2]);
                    }catch (NumberFormatException e){
                        data.showMessage(p.getUniqueId().toString(), "§cコストが数字ではありません！");
                        return true;
                    }
                    cardsetup.get(p.getUniqueId()).cost = cost;
                    data.showMessage(p.getUniqueId().toString(), "§aカードのコストをセットしました。");
                }else if(args[1].equalsIgnoreCase("lore")) {
                    if(!cardsetup.containsKey(p.getUniqueId())){
                        data.showMessage(p.getUniqueId().toString(),"§cセットアップ/再設定が開始されていません！ もしかして: /"+cmd+" card create");
                        return true;
                    }
                    List<String> lore = new ArrayList<>();
                    lore.add(args[2]);
                    cardsetup.get(p.getUniqueId()).lore = lore;
                    data.showMessage(p.getUniqueId().toString(), "§aカードのloreをセットしました。");
                }else if(args[1].equalsIgnoreCase("arg")) {
                    if(!cardsetup.containsKey(p.getUniqueId())){
                        data.showMessage(p.getUniqueId().toString(),"§cセットアップ/再設定が開始されていません！ もしかして: /"+cmd+" card create");
                        return true;
                    }
                    cardsetup.get(p.getUniqueId()).arg = args[2];
                    data.showMessage(p.getUniqueId().toString(), "§aカードのargをセットしました。");
                }
            }else if(args[0].equalsIgnoreCase("addshop")) {
                if (!p.hasPermission("siege.setup")) {
                    p.sendMessage(data.plugin.getPrefix() + "§cあなたには権限がありません！");
                    return true;
                }
                try{
                    Integer.parseInt(args[2]);
                }catch (NumberFormatException e){
                    p.sendMessage(data.plugin.getPrefix() + "§c値段が数字ではありません！");
                    return true;
                }
                List<String> shop =  data.plugin.config.getStringList("shop");
                shop.add(args[1]+" = "+args[2]);
                data.plugin.config.set("shop",shop);
                data.plugin.saveConfig();
                p.sendMessage(data.plugin.getPrefix() + "§a追加しました。");
                return true;
            }
        }else{
            if(args[0].equalsIgnoreCase("card")) {
                if (!p.hasPermission("siege.setupcard")) {
                    p.sendMessage(data.plugin.getPrefix() + "§cあなたには権限がありません！");
                    return true;
                }
                if(args[1].equalsIgnoreCase("lore")) {
                    if(!cardsetup.containsKey(p.getUniqueId())){
                        data.showMessage(p.getUniqueId().toString(),"§cセットアップ/再設定が開始されていません！ もしかして: /"+cmd+" card create");
                        return true;
                    }
                    cardsetup.get(p.getUniqueId()).lore = new ArrayList<>(Arrays.asList(args).subList(2, args.length));
                    data.showMessage(p.getUniqueId().toString(), "§aカードのloreをセットしました。");
                }else if(args[1].equalsIgnoreCase("arg")) {
                    if(!cardsetup.containsKey(p.getUniqueId())){
                        data.showMessage(p.getUniqueId().toString(),"§cセットアップ/再設定が開始されていません！ もしかして: /"+cmd+" card create");
                        return true;
                    }
                    StringBuilder argm = new StringBuilder();
                    for(int i = 2;i<args.length;i++){
                        if(argm.toString().equalsIgnoreCase("")){
                            argm = new StringBuilder(args[i]);
                        }else{
                            argm.append(" ").append(args[i]);
                        }
                    }
                    cardsetup.get(p.getUniqueId()).arg = new String(argm);
                    data.showMessage(p.getUniqueId().toString(), "§aカードのargをセットしました。");
                }
            }
        }
        return true;
    }

}
