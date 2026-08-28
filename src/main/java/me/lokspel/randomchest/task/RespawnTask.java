package me.lokspel.randomchest.task;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.concurrent.ConcurrentHashMap;

public class RespawnTask implements Runnable {

    private final ConcurrentHashMap<String, Long[]> respawnDelay;

    public RespawnTask(ConcurrentHashMap<String, Long[]> respawnDelay) {
        this.respawnDelay = respawnDelay;
    }

    @Override
    public void run() {
        for (String key : respawnDelay.keySet()) {
            Long[] data = respawnDelay.get(key);
            if (System.currentTimeMillis() - data[0] > data[1]) {
                respawnDelay.remove(key);
                Block block = toBlock(key);
                if (block != null) {
                    block.setType(Material.CHEST);
                }
            }
        }
    }

    private Block toBlock(String key) {
        String[] parts = key.split(",");
        World world = Bukkit.getWorld(parts[0]);
        if (world == null) return null;
        return world.getBlockAt(
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2]),
                Integer.parseInt(parts[3])
        );
    }
}
