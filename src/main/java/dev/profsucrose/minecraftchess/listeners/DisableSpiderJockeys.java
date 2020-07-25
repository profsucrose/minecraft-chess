package dev.profsucrose.minecraftchess.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

public class DisableSpiderJockeys implements Listener {

    @EventHandler
    public static void onSkeletonSpawn(EntitySpawnEvent e) {
        if (e.getEntityType() == EntityType.SKELETON)
            e.setCancelled(true);
    }

}
