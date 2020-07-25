package dev.profsucrose.minecraftchess.commands;

import dev.profsucrose.minecraftchess.MinecraftChess;
import dev.profsucrose.minecraftchess.models.Board;
import dev.profsucrose.minecraftchess.models.Piece;
import dev.profsucrose.minecraftchess.models.PieceType;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class Chess implements CommandExecutor {

    private static final int[] boardA1Coords = { -37, 5, 38 };
    public enum Turn { WHITE, BLACK }
    public Turn currentTurn = Turn.WHITE;
    private Board board;
    private int selectedBoardX = -1;
    private int selectedBoardY = -1;
    private boolean pieceSelected = false;
    private boolean inCheck = false;
    private int whiteKingX = 4;
    private int whiteKingY = 0;
    private int blackKingX = 4;
    private int blackKingY = 7;
    private boolean blackKingMoved      = false;
    private boolean blackRookMoved      = false;
    private boolean whiteKingMoved      = false;
    private boolean whiteRookMoved      = false;
    private boolean gameEnded           = false;
    private boolean isPawnBeingPromoted = false;
    private int     pawnBeingPromotedX  = -1;
    private int     pawnBeingPromotedY  = -1;
    private List<Entity> selectSpots;

    private void printBoard() {
        for (int y = 0; y < 8; y++) {
            StringBuilder row = new StringBuilder();
            for (int x = 0; x < 8; x++)
                row.append(board.filled(x, y) ? board.get(x, y).getType().name() : '_').append(' ');
            System.out.println(row.toString());
        }
    }

    private void checkCheckAndCheckmate(List<int[]> possibleSpotLocations) {
        printBoard();
        for (int[] possibleSpotLocation : possibleSpotLocations) {
            int cX = possibleSpotLocation[0];
            int cY = possibleSpotLocation[1];
            if (board.filled(cX, cY)
                    && "KING".equals(board.get(cX, cY).getType().name().split("_")[0])
                    && board.get(cX, cY).getSide() == currentTurn) {
                inCheck = true;
            }
        }

        if (inCheck) {
            // currentTurn is the side being attacked as of function call
            boolean checkmate = isSideCheckmated(
                    currentTurn == Turn.WHITE ? whiteKingX : blackKingX,
                    currentTurn == Turn.WHITE ? whiteKingY : blackKingY
            );
            if (checkmate) {
                Bukkit.broadcastMessage(String.format("%s%sCheckmate! %s wins!", currentTurn == Turn.WHITE ? ChatColor.BLACK : ChatColor.WHITE, ChatColor.BOLD, currentTurn == Turn.WHITE ? "BLACK" : "WHITE"));
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendTitle("Checkmate!", "Good game!", 10, 70, 20);
                    gameEnded = true;
                    return;
                }
            } else if (inCheck) {
                Bukkit.broadcastMessage("" + ChatColor.GOLD + ChatColor.BOLD + "Check!");
            }
        }
    }

    public void onClickPawnPromotion(Material clickedItem) {
        String pawnPromotionPieceName = "";
        switch (clickedItem) {
            case IRON_INGOT:
                pawnPromotionPieceName = "KNIGHT";
                break;
            case GOLD_INGOT:
                pawnPromotionPieceName = "BISHOP";
                break;
            case DIAMOND:
                pawnPromotionPieceName = "ROOK";
                break;
            case NETHERITE_INGOT:
                pawnPromotionPieceName = "QUEEN";
                break;
        }
        String side = currentTurn == Turn.WHITE ? "BLACK" : "WHITE";
        PieceType type = PieceType.valueOf(pawnPromotionPieceName + "_" + side);
        board.board[pawnBeingPromotedY][pawnBeingPromotedX].clear();
        board.board[pawnBeingPromotedY][pawnBeingPromotedX] = new Piece(pawnBeingPromotedX, pawnBeingPromotedY, type);

        List<int[]> possibleSpotLocations = new ArrayList<>();
        moveSelectorHelper(possibleSpotLocations, type.toString(), currentTurn == Turn.WHITE ? Turn.BLACK : Turn.WHITE, pawnBeingPromotedX, pawnBeingPromotedY);
        checkCheckAndCheckmate(possibleSpotLocations);
        MinecraftChess.instance.updateScoreboardTurn();
        isPawnBeingPromoted = false;
    }

    private void openPawnSelection(Player player) {
        Inventory pawnPromotionMenu = Bukkit.getServer().createInventory(player, 9, "Promote Pawn");

        String color = "" + ChatColor.GREEN + ChatColor.BOLD;
        ItemStack knight = new ItemStack(Material.IRON_INGOT);
        ItemMeta knightMeta = knight.getItemMeta();
        knightMeta.setDisplayName(color + "Knight");
        knight.setItemMeta(knightMeta);
        pawnPromotionMenu.setItem(1, knight);

        ItemStack bishop = new ItemStack(Material.GOLD_INGOT);
        ItemMeta bishopMeta = bishop.getItemMeta();
        bishopMeta.setDisplayName(color + "Bishop");
        bishop.setItemMeta(bishopMeta);
        pawnPromotionMenu.setItem(3, bishop);

        ItemStack rook = new ItemStack(Material.DIAMOND);
        ItemMeta rookMeta = rook.getItemMeta();
        rookMeta.setDisplayName(color + "Rook");
        rook.setItemMeta(rookMeta);
        pawnPromotionMenu.setItem(5, rook);

        ItemStack queen = new ItemStack(Material.NETHERITE_INGOT);
        ItemMeta queenMeta = queen.getItemMeta();
        queenMeta.setDisplayName(color + "Queen");
        queen.setItemMeta(queenMeta);
        pawnPromotionMenu.setItem(7, queen);

        player.openInventory(pawnPromotionMenu);
    }

    // check if specific king is in check at coordinates
    private boolean isSpotSafeForSide(Turn side, int pieceX, int pieceY) {
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if (!board.filled(x, y)
                        || board.get(x, y).getSide().equals(side))
                    continue;
                Piece piece = board.get(x, y);
                List<int[]> possibleSpotLocations = new ArrayList<>();
                moveSelectorHelper(possibleSpotLocations, piece.getType().name().split("_")[0], piece.getSide(), x, y);
                for (int i = 0; i < possibleSpotLocations.size(); i++) {
                    if (possibleSpotLocations.get(i)[0] == pieceX
                            && possibleSpotLocations.get(i)[1] == pieceY)
                        return false;
                }
            }
        }
        return true;
    }

    private void resetSelecterAttributes() {
        selectedBoardX = -1;
        selectedBoardY = -1;
        pieceSelected = false;
    }

    private boolean isSideCheckmated(int pieceX, int pieceY) {
        // currentTurn is side being attacked
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if (!board.filled(x, y)
                        || !board.get(x, y).getSide().equals(currentTurn))
                    continue;
                Piece piece = board.get(x, y);
                List<int[]> selectSpotLocations = new ArrayList<>();
                String pieceType = piece.getType().name().split("_")[0];
                moveSelectorHelper(selectSpotLocations, pieceType, currentTurn, x, y);

                for (int i = 0; i < selectSpotLocations.size(); i++) {
                    int slotX = selectSpotLocations.get(i)[0];
                    int slotY = selectSpotLocations.get(i)[1];
                    if (Board.outOfBounds(slotX, slotY))
                        continue;
                    // skip if has friendly piece
                    if (board.filled(slotX, slotY)
                            && currentTurn == board.get(slotX, slotY).getSide()) {
                        continue;
                    }
                    Piece temp = board.board[slotY][slotX];
                    board.board[slotY][slotX] = board.board[selectedBoardY][selectedBoardX];
                    board.board[selectedBoardY][selectedBoardX] = null;
                    boolean isSpotSafe = isSpotSafeForSide(currentTurn,
                            "KING".equals(pieceType) ? slotX : (currentTurn == Turn.WHITE ? whiteKingX : blackKingX),
                            "KING".equals(pieceType) ? slotY : (currentTurn == Turn.WHITE ? whiteKingY : blackKingY)
                    );
                    board.board[selectedBoardY][selectedBoardX] = board.board[slotY][slotX];
                    board.board[slotY][slotX] = temp;

                    // skip if moves king into a blocked space
                    if (isSpotSafe)
                        return false;
                }
            }
        }
        return true;
    }

    private void moveSelectorHelper(List<int[]> selectSpotLocations, String t, Turn side, int x, int y) {
        switch (t) {
            case "PAWN":
                int pawnDirection = Turn.WHITE.equals(side) ? 1 : -1;
                if (!board.filled(x, y + pawnDirection))
                    selectSpotLocations.add(new int[] { x, y + pawnDirection });
                if (((y == 1 && pawnDirection == 1)
                        || (y == 6 && pawnDirection == -1))
                        && !board.filled(x, y + pawnDirection)) {
                    selectSpotLocations.add(new int[] { x, y + 2 * pawnDirection });
                }
                if (board.filled(x + 1, y + pawnDirection))
                    selectSpotLocations.add(new int[] { x + 1, y + pawnDirection });
                if (board.filled(x - 1, y + pawnDirection))
                    selectSpotLocations.add(new int[] { x - 1, y + pawnDirection });
                break;
            case "BISHOP":
                // top-right
                for (int i = 1; i < 8; i++) {
                    selectSpotLocations.add(new int[] { x + i, y + i });
                    if (board.filled(x + i, y + i))
                        break;
                }
                // top-left
                for (int i = 1; i < 8; i++) {
                    selectSpotLocations.add(new int[] { x - i, y + i });
                    if (board.filled(x - i, y + i))
                        break;
                }
                // bottom-right
                for (int i = 1; i < 8; i++) {
                    selectSpotLocations.add(new int[] { x + i, y - i });
                    if (board.filled(x + i, y - i))
                        break;
                }
                // bottom-left
                for (int i = 1; i < 8; i++) {
                    selectSpotLocations.add(new int[] { x - i, y - i });
                    if (board.filled(x - i, y - i))
                        break;
                }
                break;
            case "ROOK":
                for (int i = x + 1; i < 8; i++) {
                    selectSpotLocations.add(new int[] { i, y });
                    if (board.filled(i, y))
                        break;
                }
                for (int i = x - 1; i >= 0; i--) {
                    selectSpotLocations.add(new int[] { i, y });
                    if (board.filled(i, y))
                        break;
                }
                for (int i = y - 1; i >= 0; i--) {
                    selectSpotLocations.add(new int[] { x, i });
                    if (board.filled(x, i))
                        break;
                }
                for (int i = y + 1; i < 8; i++) {
                    selectSpotLocations.add(new int[] { x, i });
                    if (board.filled(x, i))
                        break;
                }
                break;
            case "KNIGHT":
                selectSpotLocations.add(new int[] { x + 2, y + 1 });
                selectSpotLocations.add(new int[] { x + 2, y - 1 });

                selectSpotLocations.add(new int[] { x - 2, y + 1 });
                selectSpotLocations.add(new int[] { x - 2, y - 1 });

                selectSpotLocations.add(new int[] { x + 1, y - 2 });
                selectSpotLocations.add(new int[] { x - 1, y - 2 });

                selectSpotLocations.add(new int[] { x - 1, y + 2 });
                selectSpotLocations.add(new int[] { x + 1, y + 2 });
                break;
            case "KING":
                selectSpotLocations.add(new int[] { x - 1, y - 1 });
                selectSpotLocations.add(new int[] { x - 1, y + 1 });
                selectSpotLocations.add(new int[] { x - 1, y });

                selectSpotLocations.add(new int[] { x + 1, y - 1 });
                selectSpotLocations.add(new int[] { x + 1, y + 1 });
                selectSpotLocations.add(new int[] { x + 1, y });

                selectSpotLocations.add(new int[] { x, y + 1 });
                selectSpotLocations.add(new int[] { x, y - 1 });

                if (side == Turn.WHITE
                        && !whiteKingMoved
                        && !board.filled(5, 0)
                        && !board.filled(6, 0)
                        && !whiteRookMoved) {
                    selectSpotLocations.add(new int[] { 6, 0 });
                } else if (!blackKingMoved
                        && !board.filled(5, 7)
                        && !board.filled(6, 7)
                        && !blackRookMoved) {
                    selectSpotLocations.add(new int[] { 6, 7 });
                }
                break;
            case "QUEEN":
                moveSelectorHelper(selectSpotLocations, "BISHOP", side, x, y);
                moveSelectorHelper(selectSpotLocations, "ROOK", side, x, y);
                break;
        }
    }

    private void clearSelectSpots() {
        for (int i = selectSpots.size() - 1; i >= 0; i--) {
            selectSpots.get(i).remove();
            selectSpots.remove(i);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        board = new Board();
        selectSpots = new ArrayList<>();
        return true;
    }

    public Turn getCurrentTurn() {
        return currentTurn;
    }

    public int[] getBoardA1Coords() {
        return boardA1Coords;
    }

    public void makeTurn(int x, int y, Player player) {
        if (!pieceSelected) {
            System.out.println("Turn selected filled slot or invalid turn");
            return;
        }
        Piece piece = board.get(selectedBoardX, selectedBoardY);
        System.out.printf("%d %d %s\n", selectedBoardX, selectedBoardY, piece);
        if (board.filled(x, y))
            board.get(x, y).kill();
        clearSelectSpots();
        piece.stopGlow();
        board.move(selectedBoardX, selectedBoardY, x, y, true);
        inCheck = false;
        currentTurn = currentTurn == Turn.WHITE ? Turn.BLACK : Turn.WHITE; // currentTurn is now opposite to moving piece
        List<int[]> possibleSpotLocations = new ArrayList<>();
        String pieceType = piece.getType().name().split("_")[0];
        moveSelectorHelper(possibleSpotLocations, pieceType, piece.getSide(), x, y);

        switch (pieceType) {
            case "KING":
                MinecraftChess.instance.updateScoreboardTurn();
                if (piece.getSide() == Turn.WHITE) {
                    if (!whiteKingMoved)
                        whiteKingMoved = true;
                    whiteKingX = x;
                    whiteKingY = y;
                    if (x == 6
                            && y == 0) {
                        board.move(7, 0, 5, 0, true);
                    }
                } else {
                    if (!blackKingMoved)
                        blackKingMoved = true;
                    blackKingX = x;
                    blackKingY = y;
                    if (x == 6
                            && y == 7) {
                        board.move(7, 7, 5, 7, true);
                    }
                }
                checkCheckAndCheckmate(possibleSpotLocations);
                resetSelecterAttributes();
                MinecraftChess.instance.updateScoreboardTurn();
                break;
            case "ROOK":
                MinecraftChess.instance.updateScoreboardTurn();
                if (piece.getSide() == Turn.WHITE) {
                    if (!whiteRookMoved)
                        whiteRookMoved = true;
                } else {
                    if (!blackRookMoved)
                        blackRookMoved = true;
                }
                checkCheckAndCheckmate(possibleSpotLocations);
                resetSelecterAttributes();
                MinecraftChess.instance.updateScoreboardTurn();
                break;
            case "PAWN":
                if (y == (piece.getSide() == Turn.WHITE ? 7 : 0)) {
                    isPawnBeingPromoted = true;
                    pawnBeingPromotedX = x;
                    pawnBeingPromotedY = y;
                    openPawnSelection(player);
                } else {
                    MinecraftChess.instance.updateScoreboardTurn();
                }
                resetSelecterAttributes();
                break;
            default:
                checkCheckAndCheckmate(possibleSpotLocations);
                resetSelecterAttributes();
                MinecraftChess.instance.updateScoreboardTurn();
        }
    }

    public void selectPieceSlot(int x, int y) {
        if (gameEnded
                || isPawnBeingPromoted
                || !board.get(x, y).getType().name().split("_")[1].equals(currentTurn.name()))
            return;
        if (selectedBoardX != -1
                && selectedBoardY != -1)
            board.get(selectedBoardX, selectedBoardY).stopGlow();
        selectedBoardX = x;
        selectedBoardY = y;
        pieceSelected = true;
        Piece piece = board.get(x, y);
        piece.glow();
        clearSelectSpots();
        List<int[]> selectSpotLocations = new ArrayList<>();
        String pieceType = piece.getType().name().split("_")[0];
        Turn side = piece.getSide();
        moveSelectorHelper(selectSpotLocations, pieceType, side, x, y);

        for (int i = 0; i < selectSpotLocations.size(); i++) {
            int slotX = selectSpotLocations.get(i)[0];
            int slotY = selectSpotLocations.get(i)[1];
            if (Board.outOfBounds(slotX, slotY))
                continue;
            // skip if has friendly piece
            if (board.filled(slotX, slotY)
                    && side == board.get(slotX, slotY).getSide()) {
                continue;
            }
            Piece temp = board.board[slotY][slotX];
            board.board[slotY][slotX] = board.board[selectedBoardY][selectedBoardX];
            board.board[selectedBoardY][selectedBoardX] = null;
            boolean isSpotSafe = isSpotSafeForSide(currentTurn,
                    "KING".equals(pieceType) ? slotX : (side == Turn.WHITE ? whiteKingX : blackKingX),
                    "KING".equals(pieceType) ? slotY : (side == Turn.WHITE ? whiteKingY : blackKingY)
            );
            board.board[selectedBoardY][selectedBoardX] = board.board[slotY][slotX];
            board.board[slotY][slotX] = temp;

            // skip if moves king into a blocked space
            if (!isSpotSafe) {
                continue;
            }

            Mob selectSpot = (Mob)Bukkit.getWorld("world").spawnEntity(
                    new Location(Bukkit.getWorld("world"), boardA1Coords[0] - 2 * slotX, 5, 2 * slotY + boardA1Coords[2]),
                    piece.getEntity().getType()
            );

            switch (selectSpot.getType()) {
                case SNOWMAN:
                    ((Snowman)selectSpot).setDerp(true);
                    break;
                case EVOKER:
                    ((Evoker)selectSpot).setPatrolLeader(false);
                    ((Evoker)selectSpot).setCanJoinRaid(false);
                    break;
                case SHEEP:
                    ((Sheep)selectSpot).setSheared(false);
                    break;
            }

            if (Turn.BLACK == side)
                selectSpot.setRotation(-180, 0);
            selectSpot.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 255, true, false));
            selectSpot.setAI(false);
            selectSpot.setGlowing(true);

            selectSpots.add(selectSpot);
        }
    }

    public Board getBoard() {
        return board;
    }

    public void clearPieces() {
        clearSelectSpots();
        for (int i = Piece.takenPieceRows.size() - 1; i >= 0; i--) {
            Piece.takenPieceRows.get(i).remove();
            Piece.takenPieceRows.remove(i);
        }
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if (board.get(x, y) != null)
                    board.get(x, y).clear();
            }
        }
    }

}
