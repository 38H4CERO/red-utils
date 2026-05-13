package net.redct.client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.*;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static net.redct.client.utils.Utils.trimFormatedText;

public class ScoreboardUtils {

    public static List<String> scoreboardLines = null;

    private static List<String> parseScoreboard (Scoreboard scoreboard, Objective sidebar){
        List<String> scoreboardLines = new ArrayList<>();
        Collection<PlayerScoreEntry> scores = scoreboard.listPlayerScores(sidebar);
        for (PlayerScoreEntry scoreEntry : scores) {

            String scoreHolderName = scoreEntry.owner();
            PlayerTeam team = scoreboard.getPlayersTeam(scoreHolderName);
            Component lineComponent = PlayerTeam.formatNameForTeam(team, Component.literal(scoreHolderName));

            String line = trimFormatedText(lineComponent.getString());
            if (line != ""){
                scoreboardLines.add(line);
            }
        }
        return scoreboardLines;
    }

    public static List<String> getScoreboard(){
        Minecraft mc = Minecraft.getInstance();
        @Nullable ClientLevel level = mc.level;
        if (level == null) { return null; }

        Scoreboard scoreboard = level.getScoreboard();

        Objective sidebar = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
        if (sidebar == null) { return null; }
        scoreboardLines = parseScoreboard(scoreboard, sidebar);
        return scoreboardLines;
    }

}
