package org.mcadminToolkit.serverStats;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

public class serverStats {
    private static String convert(long bytes) {
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absB < 1024) {
            return bytes + " B";
        }
        long value = absB;
        CharacterIterator ci = new StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(bytes);
        return String.format("%.1f %ciB", value / 1024.0, ci.current());
    }

    public static String cpuUsage (JavaPlugin plugin){
        Server server = plugin.getServer();
        OperatingSystemMXBean system = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        double cpuUsageDouble = system.getSystemCpuLoad();
        String cpuUsage = Double.toString(cpuUsageDouble) + "%";

        return cpuUsage;
    }

    public static String ramUsage (JavaPlugin plugin) {
        Server server = plugin.getServer();
        OperatingSystemMXBean system = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        long totalMemoryB = system.getTotalPhysicalMemorySize();
        String totalMemory = convert(totalMemoryB);

        long freeMemoryB = system.getFreePhysicalMemorySize();
        long usedMemoryB = totalMemoryB - freeMemoryB;
        String usedMemory = convert(usedMemoryB);

        return usedMemory + "/" + totalMemory;
    }

    public static String playersOnline (JavaPlugin plugin){
        Server server = plugin.getServer();

        Player[] onlinePlayers = server.getOnlinePlayers().toArray(new Player[0]);
        int onlinePlayersCount = onlinePlayers.length;

        int offlinePlayers = server.getMaxPlayers();

        return Integer.toString(onlinePlayersCount) + "/" + Integer.toString(offlinePlayers);
    }
}
