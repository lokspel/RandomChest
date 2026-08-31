package me.lokspel.randomchest.task;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.Map;
import java.util.function.Consumer;

public class RespawnTask implements Consumer<WrappedTask> {

    private final Map<Location, Long> respawnDelay;
    private final Location location;

    public RespawnTask(Map<Location, Long> respawnDelay, Location location) {
        this.respawnDelay = respawnDelay;
        this.location = location;
    }

    @Override
    public void accept(WrappedTask task) {
        Long deadline = respawnDelay.get(location);

        if (deadline == null) {
            task.cancel();
            return;
        }

        if (System.currentTimeMillis() >= deadline) {
            respawnDelay.remove(location);
            location.getBlock().setType(Material.CHEST);
            task.cancel();
        }
    }
}
