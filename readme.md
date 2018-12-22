# InventoryAPI使い方
・GUIManager見れば応用はわかるから基礎を
```java:sample
InventoryAPI inv = new InventoryAPI("タイトル",インベントリサイズ);
ItemStack item = inv.createUnbitem("§6§lアイテムタイトル",new String[]{"§eロア1行目","§eロア2行目"},マテリアル,耐久度,光るかどうかtrue/false);
inv.setItem(セット位置,item);
inv.addOriginalListing(new InvListener(プラグインのインスタンス,InventoryAPIのインスタンス){
            //ここに様々なイベントハンドラを入れられる。
            @EventHandler
            public void onClick(InventoryClickEvent e){
                if(!super.ClickCheck(e)){
                    //基本的にこの周辺のコードは消さないでください
                    //インベントリをクリックしたプレイヤーを自動で判別しているので
                    //消すと多少のコードを自分で実装することになります。
                    return;
                }
                if(e.getSlot()!=11){
                    return;
                }
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                e.setCancelled(true);
                super.unregister(); //イベント関係の処理を次回以降行わなくなります。
            }
            @EventHandler
            public void onClose(InventoryCloseEvent e){
                super.closeCheck(e); //super.unregister(); の対象かどうかを自動判別し実行してくれます。基本削除は非推奨
            }
});
inv.openInv(p); //追加(addOriginalListing)したイベントを一括登録し、プレイヤーにインベントリを開かせます。
```
