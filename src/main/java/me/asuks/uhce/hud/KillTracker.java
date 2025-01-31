package me.asuks.uhce.hud;

import me.asuks.uhce.utils.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class KillTracker extends CommandBase {
    private static final Pattern PLAYER_NAME_PATTERN;
    private static final Map<Pattern, Integer> KILL_MESSAGE_PATTERNS;
    private final Map<String, Integer> killCountMap;
    private int color;
    private boolean toggle;
    private final KeyBinding keyBinding;

    static {
        PLAYER_NAME_PATTERN = Pattern.compile("\\w{1,16}");
        KILL_MESSAGE_PATTERNS = new HashMap<>();
        KILL_MESSAGE_PATTERNS.put(Pattern.compile("^(\\w{1,16}) was slain by \\w{1,16} .*"), 5);
        KILL_MESSAGE_PATTERNS.put(Pattern.compile("^(\\w{1,16}) was shot by \\w{1,16} .*"), 5);
        KILL_MESSAGE_PATTERNS.put(Pattern.compile("^(\\w{1,16}) was knocked off a cliff by \\w{1,16}!"), 8);
        KILL_MESSAGE_PATTERNS.put(Pattern.compile("^(\\w{1,16}) tried to swim in lava to escape \\w{1,16}"), 9);
    }

    public KillTracker() {
        killCountMap = new HashMap<>();
        color = 0xFFFFFF; // from config
        toggle = true; // from config
        keyBinding = new KeyBinding("kill tracker toggle", Keyboard.KEY_NONE, "UHCEnhancements"); // from config
        ClientRegistry.registerKeyBinding(keyBinding);
    }

    @SubscribeEvent
    public void onClientChatReceived(ClientChatReceivedEvent event) {
        if (event.type != 0) return;
        String message = StringUtil.removeFormattingCodes(event.message.getFormattedText());
        for (Map.Entry<Pattern, Integer> entry : KILL_MESSAGE_PATTERNS.entrySet()) {
            Matcher matcher = entry.getKey().matcher(message);
            if (!matcher.matches()) continue;
            Matcher playerNameMatcher = PLAYER_NAME_PATTERN.matcher(message);
            boolean ignored = false;
            for (int i = 0; i < entry.getValue(); ++i) ignored = playerNameMatcher.find();
            if (!ignored) continue;
            String killerName = playerNameMatcher.group();
            switch (killerName) {
                case "Zombie":
                case "Skeleton":
                case "Spider":
                case "Enderman":
                    return;
            }
            killCountMap.merge(killerName, 1, Integer::sum);
        }
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (!toggle) return;
        if (event.type == RenderGameOverlayEvent.ElementType.TEXT) {
            ScaledResolution resolution = event.resolution;
            FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
            int i = 0;
            for (Map.Entry<String, Integer> entry : killCountMap.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).collect(Collectors.toList())) {
                String text = entry.getKey() + ": " + entry.getValue() + " Kills";
                fr.drawStringWithShadow(text, resolution.getScaledWidth() - fr.getStringWidth(text), (i + 1) * fr.FONT_HEIGHT, color);
                ++i;
            }
        }
    }

    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event) {
        if (keyBinding.isPressed()) {
            toggle = !toggle;
        }
    }

    @Override
    public String getCommandName() {
        return "kill-tracker";
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
            case "clear":
                killCountMap.clear();
                sender.addChatMessage(new ChatComponentText("clear"));
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
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }
}
