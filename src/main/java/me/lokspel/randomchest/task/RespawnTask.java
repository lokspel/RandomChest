package me.lokspel.randomchest.task;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.Iterator;
import java.util.Map;

public class RespawnTask implements Runnable {

    private final Map<Location, Long> respawnDelay;

    public RespawnTask(Map<Location, Long> respawnDelay) {
        this.respawnDelay = respawnDelay;
    }

    @Override
    public void run() {
        long now = System.currentTimeMillis();
        for (Iterator<Map.Entry<Location, Long>> it = respawnDelay.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Location, Long> entry = it.next();
            if (now >= entry.getValue()) {
                it.remove();
                entry.getKey().getBlock().setType(Material.CHEST);
            }
        }
    }
}