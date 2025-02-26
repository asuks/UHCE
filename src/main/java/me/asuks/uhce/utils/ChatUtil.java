package me.asuks.uhce.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

public class ChatUtil {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static void addChatMessageWithPrefix(String message) {
        addChatMessage(EnumChatFormatting.GOLD + "[UHCE] " + EnumChatFormatting.RESET + message);
    }

    public static void addChatMessage(String msg) {
        addChatMessage(new ChatComponentText(msg));
    }

    public static void addChatMessage(IChatComponent msg) {
        addChatMessage(msg, mc.isCallingFromMinecraftThread());
    }

    private static void addChatMessage(IChatComponent msg, boolean isCallingFromMinecraftThread) {
        if (isCallingFromMinecraftThread) {
            mc.ingameGUI.getChatGUI().printChatMessage(msg);
        } else {
            mc.addScheduledTask(() -> mc.ingameGUI.getChatGUI().printChatMessage(msg));
        }
    }
}
