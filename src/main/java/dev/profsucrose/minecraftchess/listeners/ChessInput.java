package dev.profsucrose.minecraftchess.listeners;

import dev.profsucrose.minecraftchess.MinecraftChess;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class ChessInput implements Listener {

    private void makeTurnInLocation(Location loc, Player player) {
        int blockYRelativeToBoard = (int)loc.getZ() - MinecraftChess.chess.getBoardA1Coords()[2] + 1;
        int blockXRelativeToBoard = MinecraftChess.chess.getBoardA1Coords()[0] - (int)loc.getX();
        int pieceY = (blockYRelativeToBoard - (blockYRelativeToBoard % 2)) / 2;
        int pieceX = (blockXRelativeToBoard - (blockXRelativeToBoard % 2)) / 2;
        MinecraftChess.chess.makeTurn(pieceX, pieceY, player);
    }

    @EventHandler
    public void onPlayerRightClickInventory(InventoryClickEvent e) {
        if (e.getView().getTitle().equals("Promote Pawn")) {
            e.setCancelled(true);
            e.getView().close();
            MinecraftChess.chess.onClickPawnPromotion(e.getCurrentItem().getType());
        }
    }

    @EventHandler
    public void onPlayerRightClickEntity(PlayerInteractEntityEvent e) {
        System.out.println("Right clicked entity");
        e.setCancelled(true);
        Location loc = e.getRightClicked().getLocation();
        Player player = e.getPlayer();

        int pieceY = ((int)loc.getZ() - MinecraftChess.chess.getBoardA1Coords()[2]) / 2;
        int pieceX = (MinecraftChess.chess.getBoardA1Coords()[0] - (int)loc.getX()) / 2;

        if (MinecraftChess.chess.getBoard().filled(pieceX, pieceY)
                && MinecraftChess.chess.getCurrentTurn().equals(MinecraftChess.chess.getBoard().get(pieceX, pieceY).getSide())) {
            MinecraftChess.chess.selectPieceSlot(pieceX, pieceY);
            return;
        }

        if (((Mob)e.getRightClicked()).hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            makeTurnInLocation(loc, player);
            return;
        }
        List<Entity> nearbyMobs = e.getRightClicked().getNearbyEntities(1, 1, 1);
        for (int i = 0; i < nearbyMobs.size(); i++) {
            Entity entity = nearbyMobs.get(i);
            if (!(entity instanceof Mob))
                continue;
            if (((Mob)entity).hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                makeTurnInLocation(loc, player);
                return;
            }
        }

    }

}
