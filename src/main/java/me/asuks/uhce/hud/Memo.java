package me.asuks.uhce.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Memo extends CommandBase {
    private String text;
    private int color;
    private boolean toggle;

    public Memo() {
        text = "Memo"; // from config
        color = 0xFFFFFF; // from config
        toggle = true; // from config
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (!toggle) return;
        if (event.type == RenderGameOverlayEvent.ElementType.TEXT) {
            ScaledResolution resolution = event.resolution;
            final FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
            fr.drawStringWithShadow(text, resolution.getScaledWidth() - fr.getStringWidth(text), 0, color);
        }
    }

    @Override
    public String getCommandName() {
        return "memo";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/" + getCommandName();
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        int argsLength = args.length;
        if (argsLength == 0) {
            sender.addChatMessage(new ChatComponentText("arguments-missing"));
            return;
        }
        switch (args[0].toLowerCase()) {
            case "toggle":
                toggle = !toggle;
                sender.addChatMessage(new ChatComponentText("toggle"));
                return;
            case "color":
                if (argsLength == 1) {
                    sender.addChatMessage(new ChatComponentText("arguments-missing"));
                    return;
                }
                int color;
                try {
                    color = Integer.parseInt(args[1]);
                } catch (NumberFormatException ignored) {
                    sender.addChatMessage(new ChatComponentText("invalid-input"));
                    return;
                }
                this.color = color;
            default:
                break;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String arg : args) stringBuilder.append(arg).append(" ");
        text = stringBuilder.substring(0, stringBuilder.length() - 1);
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }
}
