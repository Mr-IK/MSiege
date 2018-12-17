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

import java.util.*;

public class SiegeCommand implements CommandExecutor,Listener{

    SiegeData data;
    String cmd;
    GUIManager gui;
    HashMap<UUID,SiegeArena> arenasetup = new HashMap<>();
    HashMap<UUID,SiegeCard> cardsetup = new HashMap<>();
    HashMap<UUID,String> arenasetups = new HashMap<>();

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
                arena.team1.playerlist.remove(p.getUniqueId());
                arena.team2.playerlist.remove(p.getUniqueId());
                data.showMessage(p.getUniqueId().toString(),"§a"+arena.getName()+"から抜けました");
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
                    p.sendMessage("§c-------------Card関連-------------");
                    p.sendMessage("§c/" + cmd + " card create [カード名] : カードを作成する");
                    p.sendMessage("§c/" + cmd + " card lore [lore1] [lore2] …: カードのloreを設定する");
                    p.sendMessage("§c/" + cmd + " card type item,command,craft : カードのタイプを設定する");
                    p.sendMessage("§c/" + cmd + " card additem : カードのアイテムを追加する");
                    p.sendMessage("§c/" + cmd + " card clearitem : カードのアイテムを追加する");
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
                }
                if (p.hasPermission("siege.forcestart")) {
                    p.sendMessage("§4------------Admin/Vip用-------------");
                    p.sendMessage("§c/" + cmd + " start [アリーナ名] : ゲームを強制開始する");
                }
                p.sendMessage("§e------------User用-------------");
                p.sendMessage("§a/" + cmd + " join [アリーナ名] : アリーナに参加");
                p.sendMessage("§a/" + cmd + " leave : アリーナから抜ける");
                p.sendMessage("§a/" + cmd + " list : アリーナのリスト");
                return true;
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
                if(!data.getStats(p.getUniqueId().toString()).getSelectcard().contains(args[1])) {
                    data.showMessage(p.getUniqueId().toString(), "§cこのカードは所持していません！");
                    return true;
                }
                data.getStats(p.getUniqueId().toString()).removehavecard(args[1]);
                data.getStats(p.getUniqueId().toString()).removeselectcard(args[1]);
                data.showMessage(p.getUniqueId().toString(),"§aカード「"+args[1]+"」を捨てました。");
                return true;
            }else if(args[0].equalsIgnoreCase("cardget")){
                if(data.cardlist().contains(args[1])){
                    if(data.getStats(p.getUniqueId().toString()).getHavecard().contains(args[1])){
                        data.showMessage(p.getUniqueId().toString(),"§cこのカードはすでに取得済みです！");
                        return true;
                    }
                    data.getStats(p.getUniqueId().toString()).addhavecard(args[1]);
                    data.showMessage(p.getUniqueId().toString(),"§aカード「"+args[1]+"」を取得しました。");
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
                    data.showMessage(p.getUniqueId().toString(),"§aカード「"+args[1]+"」をセットしました。");
                    return true;
                }
            }else if(args[0].equalsIgnoreCase("cardunset")){
                if(!data.getStats(p.getUniqueId().toString()).getSelectcard().contains(args[1])){
                    data.showMessage(p.getUniqueId().toString(),"§cこのカードはセットされていません！");
                    return true;
                }
                data.getStats(p.getUniqueId().toString()).removeselectcard(args[1]);
                data.showMessage(p.getUniqueId().toString(),"§aカード「"+args[1]+"」をアンセットしました。");
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
                    if(!args[2].equalsIgnoreCase("command")&&!args[2].equalsIgnoreCase("item")&&!args[2].equalsIgnoreCase("craft")){
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
