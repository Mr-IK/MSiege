package red.man10.msiege.util;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;

public class SBossBar {
    public org.bukkit.boss.BossBar bar;
    private String title;

    public SBossBar(String title,BarColor barColor){
        this.title = title;
        bar = Bukkit.createBossBar(title, barColor, BarStyle.SOLID);
    }

    public void setTitle(String title){
        this.title = title;
        bar.setTitle(title);
    }

    public String getTitle() {
        return title;
    }

    public void addProgress(double i){
        bar.setProgress(bar.getProgress()+i);
    }

    public void takeProgress(double i){
        bar.setProgress(bar.getProgress()-i);
    }

    public void setProgress(double i){
        bar.setProgress(i);
    }

    public void addPlayer(Player player){
        bar.addPlayer(player);
    }

    public void removePlayer(Player player){
        bar.removePlayer(player);
    }

    public void setVisible(boolean bool){
        bar.setVisible(bool);
    }
}
