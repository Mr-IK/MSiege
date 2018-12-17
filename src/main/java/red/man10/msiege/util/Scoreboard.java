package red.man10.msiege.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

public class Scoreboard {

    public static final String OBJECTIVE_NAME = "MSiege";

    public org.bukkit.scoreboard.Scoreboard scoreboard;

    public Scoreboard() {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective sidebar = scoreboard.registerNewObjective(OBJECTIVE_NAME, "dummy");
        sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);
    }
    public void setShowPlayer(Player player) {
        player.setScoreboard(scoreboard);
    }
    public void setMainScoreboard(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }
    public void setTitle(String title) {
        Objective obj = scoreboard.getObjective(OBJECTIVE_NAME);
        if (obj != null) {
            obj.setDisplayName(title);
        }
    }
    public boolean contain() {
        Objective obj = scoreboard.getObjective(OBJECTIVE_NAME);
        return obj != null;
    }
    public String getTitle() {
        Objective obj = scoreboard.getObjective(OBJECTIVE_NAME);
        if (obj != null) {
            return obj.getDisplayName();
        }
        return null;
    }
    public int getpoint(String name) {
        Objective obj = scoreboard.getObjective(OBJECTIVE_NAME);
        Score score = obj.getScore(name);
        return score.getScore();
    }
    public void addScore(String name, int amount) {
        Objective obj = scoreboard.getObjective(OBJECTIVE_NAME);
        Score score = obj.getScore(name);
        int point = score.getScore();
        score.setScore(point + amount);
    }
    public void setScore(String name, int point) {
        Objective obj = scoreboard.getObjective(OBJECTIVE_NAME);
        Score score = obj.getScore(name);
        if (point == 0) {
            score.setScore(1);
        }
        score.setScore(point);
    }
    public void remove() {
        if (scoreboard.getObjective(DisplaySlot.SIDEBAR) != null) {
            scoreboard.getObjective(DisplaySlot.SIDEBAR).unregister();
        }
        scoreboard.clearSlot(DisplaySlot.SIDEBAR);
    }
    public void removeScores(String name) {
        scoreboard.resetScores(name);
    }
}
