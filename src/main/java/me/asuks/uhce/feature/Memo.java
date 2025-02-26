package me.asuks.uhce.feature;

import me.asuks.uhce.utils.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Memo extends CommandBase {
    private static final String commandName;
    private static final String positionUsage;
    private static final String xOffsetUsage;
    private static final String yOffsetUsage;
    private static final String colorUsage;
    private static final List<String> subcommands;
    private static final List<String> positionOptions;
    private final Configuration configuration;
    private final Property textProperty;
    private String text;
    private final Property showProperty;
    private boolean show;
    private final Property positionProperty;
    private String position;
    private final Property xOffsetProperty;
    private int xOffset;
    private final Property yOffsetProperty;
    private int yOffset;
    private final Property colorProperty;
    private int color;

    static {
        commandName = "memo";
        positionUsage = "Usage: " + commandName + " position [top-right, top-left, bottom-right, bottom-left]";
        xOffsetUsage = "Usage: " + commandName + " x-offset <int>";
        yOffsetUsage = "Usage: " + commandName + " y-offset <int>";
        colorUsage = "Usage: " + commandName + " color <int>";
        subcommands = new ArrayList<>();
        subcommands.add("toggle");
        subcommands.add("show");
        subcommands.add("hide");
        subcommands.add("position");
        subcommands.add("x-offset");
        subcommands.add("y-offset");
        subcommands.add("color");
        positionOptions = new ArrayList<>();
        positionOptions.add("top-right");
        positionOptions.add("top-left");
        positionOptions.add("bottom-right");
        positionOptions.add("bottom-left");
    }

    public Memo(Configuration configuration) {
        this.configuration = configuration;
        textProperty = configuration.get("memo", "text", "UHCE Memo Functionality, try "+ EnumChatFormatting.GREEN + "/" + commandName + "my memo");
        text = textProperty.getString();
        showProperty = configuration.get("memo", "show", true);
        show = showProperty.getBoolean();
        positionProperty = configuration.get("memo", "position", "top-right");
        position = positionProperty.getString();
        xOffsetProperty = configuration.get("memo", "x-offset", 2);
        xOffset = xOffsetProperty.getInt();
        yOffsetProperty = configuration.get("memo", "y-offset", 2);
        yOffset = yOffsetProperty.getInt();
        colorProperty = configuration.get("memo", "color", 0xFFFFFF);
        color = colorProperty.getInt();
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (!show) return;
        if (event.type == RenderGameOverlayEvent.ElementType.TEXT) {
            ScaledResolution resolution = event.resolution;
            FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
            switch (position) {
                case "top-right":
                    fr.drawStringWithShadow(text, resolution.getScaledWidth()- xOffset - fr.getStringWidth(text), yOffset, color);
                    break;
                case "top-left":
                    fr.drawStringWithShadow(text, resolution.getScaledWidth() - fr.getStringWidth(text), yOffset, color);
                    break;
                case "bottom-right":
                    fr.drawStringWithShadow(text, resolution.getScaledWidth()- xOffset - fr.getStringWidth(text), resolution.getScaledHeight() - yOffset - fr.FONT_HEIGHT, color);
                    break;
                case "bottom-left":
                    fr.drawStringWithShadow(text, resolution.getScaledWidth() - fr.getStringWidth(text), resolution.getScaledHeight() - yOffset - fr.FONT_HEIGHT, color);
                    break;
            }
        }
    }

    @Override
    public String getCommandName() {
        return "memo";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/" + getCommandName() + " " + Arrays.toString(subcommands.toArray(new String[0]));
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        int argsLength = args.length;
        if (argsLength == 0) {
            ChatUtil.addChatMessageWithPrefix(EnumChatFormatting.RED + "Argument is needed. Usage: " + getCommandUsage(sender));
            return;
        }
        switch (args[0].toLowerCase()) {
            case "toggle":
                show = !show;
                showProperty.set(show);
                configuration.save();
                ChatUtil.addChatMessageWithPrefix(EnumChatFormatting.GREEN + "Toggled memo.");
                break;
            case "show":
                show = true;
                showProperty.set(true);
                configuration.save();
                ChatUtil.addChatMessageWithPrefix(EnumChatFormatting.GREEN + "Showed memo.");
                break;
            case "hide":
                show = false;
                showProperty.set(false);
                configuration.save();
                ChatUtil.addChatMessageWithPrefix(EnumChatFormatting.GREEN + "Hid memo.");
                break;
            case "position":
                if (argsLength == 1) {
                    ChatUtil.addChatMessageWithPrefix(EnumChatFormatting.RED + "Argument is needed. " + positionUsage);
                    break;
                }
                String position = args[1].toLowerCase();
                switch (position) {
                    case "top-right":
                    case "top-left":
                    case "bottom-right":
                    case "bottom-left":
                        this.position = position;
                        positionProperty.set(position);
                        configuration.save();
                        ChatUtil.addChatMessageWithPrefix(EnumChatFormatting.GREEN + "Changed the position of memo.");
                        break;
                    default:
                        ChatUtil.addChatMessageWithPrefix(EnumChatFormatting.RED + "Argument is invalid." + positionUsage);
                        break;
                }
                break;
            case "x-offset":
                if (argsLength == 1) {
                    ChatUtil.addChatMessageWithPrefix(EnumChatFormatting.RED + "Argument is needed. " + xOffsetUsage);
                    break;
                }
                try {
                    xOffset = Integer.parseInt(args[1]);
                    xOffsetProperty.set(xOffset);
                    configuration.save();
                    ChatUtil.addChatMessageWithPrefix(EnumChatFormatting.GREEN + "Changed the x offset of memo.");
                } catch (NumberFormatException ignored) {
                    ChatUtil.addChatMessageWithPrefix(EnumChatFormatting.RED + "Argument is invalid. " + xOffsetUsage);
                }
                break;
            case "y-offset":
                if (argsLength == 1) {
                    ChatUtil.addChatMessageWithPrefix(EnumChatFormatting.RED + "Argument is needed. " + yOffsetUsage);
                    break;
                }
                try {
                    yOffset = Integer.parseInt(args[1]);
                    yOffsetProperty.set(yOffset);
                    configuration.save();
                    ChatUtil.addChatMessageWithPrefix(EnumChatFormatting.GREEN + "Changed the y offset of memo.");
                } catch (NumberFormatException ignored) {
                    ChatUtil.addChatMessageWithPrefix(EnumChatFormatting.RED + "Argument is invalid. " + yOffsetUsage);
                }
                break;
            case "color":
                if (argsLength == 1) {
                    ChatUtil.addChatMessageWithPrefix(EnumChatFormatting.RED + "Argument is needed. " + colorUsage);
                    break;
                }
                try {
                    color = Integer.parseInt(args[1]);
                    colorProperty.set(color);
                    configuration.save();
                    ChatUtil.addChatMessageWithPrefix(EnumChatFormatting.GREEN + "Changed the color of memo.");
                } catch (NumberFormatException ignored) {
                    ChatUtil.addChatMessageWithPrefix(EnumChatFormatting.RED + "Argument is invalid. " + colorUsage);
                }
                break;
            default:
                StringBuilder stringBuilder = new StringBuilder();
                for (String arg : args) stringBuilder.append(arg).append(" ");
                text = stringBuilder.substring(0, stringBuilder.length() - 1);
                textProperty.set(text);
                configuration.save();
                ChatUtil.addChatMessageWithPrefix(EnumChatFormatting.GREEN + "Changed memo contents.");
                break;
        }
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (0 < args.length && "position".equalsIgnoreCase(args[0])) {
            return getListOfStringsMatchingLastWord(args, positionOptions);
        }
        return getListOfStringsMatchingLastWord(args, subcommands);
    }
}
