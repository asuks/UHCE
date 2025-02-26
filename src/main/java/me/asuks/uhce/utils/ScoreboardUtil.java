package me.asuks.uhce.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;

public class ScoreboardUtil {

    public static String getSidebarTitle() {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.theWorld == null) return "";
        Scoreboard scoreboard = minecraft.theWorld.getScoreboard();
        if (scoreboard == null) return "";
        final ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        if (objective == null) return "";
        return objective.getDisplayName();
    }

    public static boolean isInUHC() {
        String title = StringUtil.removeFormattingCodes(getSidebarTitle());
        if (title.isEmpty()) return false;
        return title.equals("HYPIXEL UHC") || title.equals("UHC CHAMPIONS");
    }
}
