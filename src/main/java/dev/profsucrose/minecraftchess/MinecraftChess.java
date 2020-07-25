package dev.profsucrose.minecraftchess;

import dev.profsucrose.minecraftchess.commands.Chess;
import dev.profsucrose.minecraftchess.listeners.ChessInput;
import dev.profsucrose.minecraftchess.listeners.DisableItemDrops;
import dev.profsucrose.minecraftchess.listeners.DisableSpiderJockeys;
import jdk.nashorn.internal.runtime.regexp.joni.encoding.CharacterType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

public final class MinecraftChess extends JavaPlugin {

    public static Chess chess = new Chess();
    private ScoreboardManager manager = Bukkit.getScoreboardManager();

    public static MinecraftChess instance;

    public Scoreboard genScoreboard() {
        Scoreboard scoreboard = manager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("main", "dummy", "" + ChatColor.GREEN + ChatColor.BOLD + "MC Chess");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        Score currentTurn = objective.getScore(""
                + ChatColor.BOLD
                + "Turn: "
                + (chess.getCurrentTurn() == Chess.Turn.WHITE
                ? ChatColor.WHITE
                : ChatColor.BLACK)
                + ChatColor.BOLD
                + chess.getCurrentTurn().name()
        );
        Score filler = objective.getScore("");
        filler.setScore(1);
        currentTurn.setScore(0);
        return scoreboard;
    }

    public void updateScoreboardTurn() {
        Scoreboard scoreboard = genScoreboard();
        for (Player p : Bukkit.getOnlinePlayers())
            p.setScoreboard(scoreboard);
    }

    @Override
    public void onEnable() {
        instance = this;
        updateScoreboardTurn();

        getCommand("chess").setExecutor(chess);

        getServer().getPluginManager().registerEvents(new ChessInput(), this);
        getServer().getPluginManager().registerEvents(new DisableSpiderJockeys(), this);
        getServer().getPluginManager().registerEvents(new DisableItemDrops(), this);
    }

    @Override
    public void onDisable() {
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                chess.clearPieces();
            }
        }
    }

}
