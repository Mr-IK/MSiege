package red.man10.msiege.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class NameTag {

    public static NameTag getter;

    static {
        getter = new NameTag();
    }

    public Class<?> getNMSClass(String name) {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            return Class.forName("net.minecraft.server." + version + "." + name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void sendPacket(Player player, Object packet) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setField(Object packet, Field field, Object value) {
        field.setAccessible(true);
        try {
            field.set(packet, value);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        field.setAccessible(!field.isAccessible());
    }

    private Field getField(Class<?> classs, String fieldname) {
        try {
            return classs.getDeclaredField(fieldname);
        } catch (NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void sendNameTag1_12(List<String> p, String prefix, String suffix, List<Player> players) {
        String name = UUID.randomUUID().toString().substring(0, 16);
        net.minecraft.server.v1_12_R1.PacketPlayOutScoreboardTeam packet = new net.minecraft.server.v1_12_R1.PacketPlayOutScoreboardTeam();
        Class<? extends net.minecraft.server.v1_12_R1.PacketPlayOutScoreboardTeam> clas = packet.getClass();
        Field team_name = this.getField(clas, "a");
        Field display_name = this.getField(clas, "b");
        Field prefix2 = this.getField(clas, "c");
        Field suffix2 = this.getField(clas, "d");
        Field members = this.getField(clas, "h");
        Field param_int = this.getField(clas, "i");
        Field pack_option = this.getField(clas, "j");
        this.setField(packet, team_name, name);
        this.setField(packet, display_name, p.get(0));
        if(prefix!=null) {
            this.setField(packet, prefix2, prefix);
        }
        if(suffix!=null) {
            this.setField(packet, suffix2, suffix);
        }
        this.setField(packet, members, p);
        this.setField(packet, param_int, 0);
        this.setField(packet, pack_option, 1);
        for (Player ps : players) {
            if(ps==null){
                continue;
            }
            sendPacket(ps, packet);
        }
    }

    public void send(List<Player> ps, String prefix, String suffix, List<Player> players) {
        List<String> p = new ArrayList<>();
        ps.forEach(key -> p.add(key.getName()));
        if (Bukkit.getVersion().contains("1.12")) {
            sendNameTag1_12(p, prefix, suffix, players);
        }
    }

}