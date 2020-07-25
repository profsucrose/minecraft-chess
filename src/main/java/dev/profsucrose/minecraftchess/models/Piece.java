package dev.profsucrose.minecraftchess.models;

import dev.profsucrose.minecraftchess.MinecraftChess;
import dev.profsucrose.minecraftchess.commands.Chess;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;

public class Piece {

    public static List<Entity> takenPieceRows;
    private static int takenPiecesWhite = 0;
    private static int takenPiecesBlack = 0;
    private static int[] boardA1Coords = MinecraftChess.chess.getBoardA1Coords();
    private static World world = Objects.requireNonNull(Bukkit.getWorld("world"));
    private PieceType type;
    private Mob entity;

    private static EnumMap<PieceType, EntityType> pieceTypeToEntity = new EnumMap<PieceType, EntityType>(PieceType.class) {{
        put(PieceType.PAWN_BLACK, EntityType.SPIDER);
        put(PieceType.ROOK_BLACK, EntityType.ENDERMAN);
        put(PieceType.KNIGHT_BLACK, EntityType.SKELETON_HORSE);
        put(PieceType.BISHOP_BLACK, EntityType.WITCH);
        put(PieceType.KING_BLACK, EntityType.EVOKER);
        put(PieceType.QUEEN_BLACK, EntityType.RAVAGER);

        put(PieceType.PAWN_WHITE, EntityType.WOLF);
        put(PieceType.ROOK_WHITE, EntityType.IRON_GOLEM);
        put(PieceType.KNIGHT_WHITE, EntityType.HORSE);
        put(PieceType.BISHOP_WHITE, EntityType.VILLAGER);
        put(PieceType.KING_WHITE, EntityType.SNOWMAN);
        put(PieceType.QUEEN_WHITE, EntityType.SHEEP);
    }};

    private static Location boardCoordsToWorldSpace(int x, int y) {
        return new Location(world,
                boardA1Coords[0] - 2 * x,
                boardA1Coords[1],
                boardA1Coords[2] + 2 * y
        );
    }

    public Chess.Turn getSide() {
        return "WHITE".equals(type.name().split("_")[1]) ? Chess.Turn.WHITE : Chess.Turn.BLACK;
    }

    public void kill() {
        entity.setHealth(0);
        boolean isWhite = getSide() == Chess.Turn.WHITE;
        Mob ent = (Mob)world.spawnEntity(
                boardCoordsToWorldSpace(
                        (isWhite ? -2 : 9) + (isWhite ? -takenPiecesWhite : takenPiecesBlack),
                        isWhite ? 0 : 7).subtract(new Vector(0, 1, 0)),
                entity.getType()
        );
        takenPieceRows.add(ent);
        if (isWhite) {
            takenPiecesWhite++;
        } else {
            takenPiecesBlack++;
        }
        ent.setAI(false);

        if (type.name().contains("BLACK"))
            ent.setRotation(-180, 0);

        // entity-specific properties
        switch (ent.getType()) {
            case SNOWMAN:
                ((Snowman)ent).setDerp(true);
                break;
            case HORSE:
                ((Horse)ent).setColor(Horse.Color.WHITE);
                ((Horse)ent).setStyle(Horse.Style.WHITE);
                break;
            case VILLAGER:
                ((Villager)ent).setProfession(Villager.Profession.CLERIC);
                break;
            case SHEEP:
                ((Sheep)ent).setColor(DyeColor.WHITE);
        }
    }

    public void clear() {
        entity.remove();
    }

    public void glow() {
        entity.setGlowing(true);
    }

    public void stopGlow() {
        entity.setGlowing(false);
    }

    public Piece(int x, int y, PieceType type) {
        this.type = type;
        takenPieceRows = new ArrayList<>();
        EntityType entityType = pieceTypeToEntity.get(type);
        entity = (Mob)world.spawnEntity(boardCoordsToWorldSpace(x, y), entityType);
        entity.setAI(false);

        if (type.name().contains("BLACK"))
            entity.setRotation(-180, 0);

        // entity-specific properties
        switch (entityType) {
            case SNOWMAN:
                ((Snowman)entity).setDerp(true);
                break;
            case HORSE:
                ((Horse)entity).setColor(Horse.Color.WHITE);
                ((Horse)entity).setStyle(Horse.Style.WHITE);
                break;
            case VILLAGER:
                ((Villager)entity).setProfession(Villager.Profession.CLERIC);
                break;
            case SHEEP:
                ((Sheep)entity).setColor(DyeColor.WHITE);
        }
        ChatColor color = type.name().split("_")[1].equals("WHITE") ? ChatColor.WHITE : ChatColor.BLACK;
        entity.setCustomName("" + color + ChatColor.BOLD + type.name().split("_")[0]);
        entity.setCustomNameVisible(true);
    }

    public PieceType getType() {
        return type;
    }

    public Entity getEntity() { return entity; }

    public void moveEntity(int x, int y) {
        entity.teleport(new Location(world, boardA1Coords[0] - 2 * x, boardA1Coords[1], 2 * y + boardA1Coords[2]));
        if (type.name().contains("BLACK"))
            entity.setRotation(-180, 0);
    }

}
