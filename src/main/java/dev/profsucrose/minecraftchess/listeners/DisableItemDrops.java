package dev.profsucrose.minecraftchess.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;

public class DisableItemDrops implements Listener {

    @EventHandler
    public static void onSkeletonSpawn(ItemSpawnEvent e) {
        e.setCancelled(true);
    }

}
