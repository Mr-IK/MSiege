package red.man10.msiege;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class ItemBankManager {
    SiegeData data;


    class ItemStorage{
        int item_id;
        String item_key;
        long amount;
    }

    //      プレイヤーの持っているストレージリストを得る
    public ArrayList<ItemStorage> getStorageList(String uuid){

        //MarketData data = new MarketData(plugin);
        String sql = "select * from item_storage where uuid= '"+uuid+"' order by item_id;";


        ArrayList<ItemStorage> list = new ArrayList<>();

        ResultSet rs = data.mysql.query(sql);
        //  Bukkit.getLogger().info(sql);
        if(rs == null){
            return list;
        }
        try
        {
            while(rs.next())
            {
                ItemStorage storage = new ItemStorage();
                storage.item_id = rs.getInt("item_id");
                storage.item_key = rs.getString("key");
                storage.amount = rs.getLong("amount");
                list.add(storage);
            }
            rs.close();
        }
        catch (SQLException e)
        {
            Bukkit.getLogger().info("Error executing a query: " + e.getErrorCode());
            return list;
        }


        data.mysql.close();
        return list;

    }



    //      アイテムアイテムバンクから取得
    public ItemStorage getItemStorage(String uuid, int item_id){
        String sql = "select * from item_storage where item_id = "+item_id +" and uuid= '"+uuid+"';";

        ItemStorage ret = new ItemStorage();
        ret.item_id = 0;
        ret.item_key = null;
        ret.amount = 0;


        ResultSet rs = data.mysql.query(sql);
        //  Bukkit.getLogger().info(sql);
        if(rs == null){
            return ret;
        }
        try
        {
            while(rs.next())
            {
                ret.item_id = rs.getInt("item_id");
                ret.item_key = rs.getString("key");
                ret.amount = rs.getLong("amount");
            }
            rs.close();
        }
        catch (SQLException e)
        {
            Bukkit.getLogger().info("Error executing a query: " + e.getErrorCode());
            return ret;
        }


        data.mysql.close();
        return ret;

    }


    void UpdateUserAsset(String uuid){
        //      ユーザの評価を更新
        Player p = Bukkit.getPlayer(UUID.fromString(uuid));
        if(p != null){
            updateUserAssetsHistory(p);
        }
    }

    int updateUserAssetsHistory(Player p){

        if(p == null){
            Bukkit.getLogger().info("updateUserAssetsHistory nullt");
            return 0;
        }
        String uuid = p.getUniqueId().toString();


        //
        ArrayList<ItemStorage> list = getStorageList(p.getUniqueId().toString());

        if(list.size() == 0){
            return 0;
        }



        long totalAmount = 0;
        double estimatedValue = 0;
        String itemList = "";
        for(ItemStorage storage:list){
            totalAmount += storage.amount;
            ItemIndex index  = getItemPrice(storage.item_id);
            estimatedValue += index.price * storage.amount;

            if(!itemList.isEmpty()){
                itemList += " ";
            }
            itemList += index.id+":"+storage.amount;
        }



        Calendar datetime = Calendar.getInstance();
        datetime.setTime(new Date());
        int year = datetime.get(Calendar.YEAR);
        int month = datetime.get(Calendar.MONTH) + 1;
        int day = datetime.get(Calendar.DAY_OF_MONTH);

        String where = "where uuid = '"+uuid+"' and year ="+year+" and month="+month+" and day="+day;

        //  ユーザーの本日のデータを一旦消去
        data.mysql.execute("delete from user_assets_history "+where+";");

        double bal = data.plugin.vault.getBalance(UUID.fromString(uuid));


        String sql = "insert into user_assets_history values(0,'"+uuid+"','"+ p.getName()+"',"+bal+","
                +estimatedValue+","
                +totalAmount+","
                +year+","
                +month+","
                +day+",'"
                +itemList+"');";


        if(!data.mysql.execute(sql)){
            data.plugin.consoleError("個人データの更新に失敗: "+p.getName());
            return  -1;
        }


        data.plugin.consoleLog("ユーザーデータ更新成功"+p.getName());


        return list.size();
    }


    public boolean reduceItem(String uuid,int item_id,long amount) {

        //      アイテムストレージになければ初期登録
        ItemStorage store = getItemStorage(uuid, item_id);
        if (store.item_key == null) {
            return false;
        }

        if (store.amount < amount) {
            return false;
        }

        boolean ret = data.mysql.execute("update item_storage set amount = amount - " + amount + " where uuid='" + uuid + "' and item_id=" + item_id + ";");
        UpdateUserAsset(uuid);

        data.showMessage(uuid, "§f§lアイテムバンクから" + store.item_key + "が" + data.getColoredItemString(amount) + "§f§l引き出されました");

        data.playSound(uuid, Sound.BLOCK_NOTE_PLING);

        return ret;
    }

    public ItemIndex  getItemPrice(int item_id){
        ArrayList<ItemIndex>  list = getItemIndexList(data.mysql,"select * from item_index where  id = "+item_id+" and disabled = 0;");
        if(list == null){
            return null;
        }
        if(list.size() == 0){
            return null;
        }
        return list.get(0);
    }

    public ArrayList<ItemIndex> getItemIndexList(MySQLManager mysql,String sql){
        ArrayList<ItemIndex> ret = new ArrayList<>();

        ResultSet rs = mysql.query(sql);
        if(rs == null){
            Bukkit.getLogger().warning("rs = null");
            return null;
        }
        try
        {
            while(rs.next())
            {
                ItemIndex item = new ItemIndex();
                item.result = false;
                item.id = rs.getInt("id");
                item.key = rs.getString("item_key");
                item.price = rs.getDouble("price");
                item.last_price = rs.getDouble("last_price");
                item.sell = rs.getInt("sell");
                item.buy = rs.getInt("buy");
                item.minPrice = rs.getDouble("min_price");
                item.maxPrice = rs.getDouble("max_price");
                item.bid = rs.getDouble("bid");
                item.ask = rs.getDouble("ask");
                item.disabled = rs.getInt("disabled");
                item.lot = rs.getInt("lot");
                item.base64 = rs.getString("base64");
                item.result = true;
                ret.add(item);
            }
            rs.close();
        }
        catch (SQLException e)
        {
            //   plugin.showError(p,"データ取得失敗");
            Bukkit.getLogger().warning(e.getMessage());
            Bukkit.getLogger().warning("Error executing a query code:" + e.getErrorCode() + " sql:"+sql);
            return null;
        }
        mysql.close();
        return ret;
    }

    class ItemIndex{
        int id;
        String key;
        double price;
        double last_price;
        double maxPrice;
        double minPrice;
        double bid;
        double ask;
        int sell;
        int buy;
        boolean result;
        int disabled;
        String base64;
        int lot;
        String getString(){
            return "ItemIndex:"+id+" "+key+" price:"+price+" bid:"+bid+" ask:"+ask + " sell:"+sell + " buy:"+buy;
        }
    }
}
