package me.asuks.uhce.feature;

import me.asuks.uhce.utils.ChatUtil;
import me.asuks.uhce.utils.ScoreboardUtil;
import me.asuks.uhce.utils.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class KillTracker extends CommandBase {
    private static final Collection<DeathMessagePattern> KILL_MESSAGE_PATTERNS;
    private static final Pattern gameStartPattern;
    private static final String commandName;
    private static final String positionUsage;
    private static final String xOffsetUsage;
    private static final String yOffsetUsage;
    private static final String colorUsage;
    private static final List<String> subcommands;
    private static final List<String> positionOptions;
    private final Configuration configuration;
    private final Map<String, UHCPlayer> playerMap;
    private final Map<UHCPlayer, Integer> killCountMap;
    private Collection<Map.Entry<UHCPlayer, Integer>> naturalOrderKillCountMap;
    private Collection<Map.Entry<UHCPlayer, Integer>> reverseOrderKillCountMap;
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
    private final KeyBinding keyBinding;

    static {
        KILL_MESSAGE_PATTERNS = new ArrayList<>();
        KILL_MESSAGE_PATTERNS.add(new DeathMessagePattern(Pattern.compile("^(\\w{1,16}) was slain by \\w{1,16} .*"), 5));
        KILL_MESSAGE_PATTERNS.add(new DeathMessagePattern(Pattern.compile("^(\\w{1,16}) was shot by \\w{1,16} .*"), 5));
        KILL_MESSAGE_PATTERNS.add(new DeathMessagePattern(Pattern.compile("^(\\w{1,16}) was knocked off a cliff by \\w{1,16}!"), 8));
        KILL_MESSAGE_PATTERNS.add(new DeathMessagePattern(Pattern.compile("^(\\w{1,16}) tried to swim in lava to escape \\w{1,16}"), 9));
        gameStartPattern = Pattern.compile("^We are teleporting players, please wait!");
        commandName = "kill-tracker";
        positionUsage = "Usage: " + commandName + " position [top-right, top-left, bottom-right, bottom-left]";
        xOffsetUsage = "Usage: " + commandName + " x-offset <int>";
        yOffsetUsage = "Usage: " + commandName + " y-offset <int>";
        colorUsage = "Usage: " + commandName + " color <int>";
        subcommands = new ArrayList<>();
        subcommands.add("clear");
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

    public KillTracker(Configuration configuration) {
        this.configuration = configuration;
        playerMap = new HashMap<>();
        killCountMap = new HashMap<>();
        naturalOrderKillCountMap = new ArrayList<>();
        reverseOrderKillCountMap = new ArrayList<>();
        showProperty = configuration.get("kill-tracker", "show", true);
        show = showProperty.getBoolean();
        positionProperty = configuration.get("kill-tracker", "position", "top-right");
        position = positionProperty.getString();
        xOffsetProperty = configuration.get("kill-tracker", "x-offset", 2);
        xOffset = xOffsetProperty.getInt();
        yOffsetProperty = configuration.get("kill-tracker", "y-offset", 2);
        yOffset = yOffsetProperty.getInt();
        colorProperty = configuration.get("kill-tracker", "color", 0xFFFFFF);
        color = colorProperty.getInt();
        keyBinding = new KeyBinding("Toggle Kill Tracker", Keyboard.KEY_NONE, "UHCE");
        ClientRegistry.registerKeyBinding(keyBinding);
    }

    @SubscribeEvent
    public void onClientDeathMessageReceived(ClientChatReceivedEvent event) {
        if (event.type != 0) return;
        if (!ScoreboardUtil.isInUHC()) return;
        String message = StringUtil.removeFormattingCodes(event.message.getFormattedText());
        for (DeathMessagePattern deathMessagePattern : KILL_MESSAGE_PATTERNS) {
            if (!deathMessagePattern.matches(message)) continue;
            if (playerMap.containsKey(deathMessagePattern.killedName)) {
                killCountMap.remove(playerMap.get(deathMessagePattern.killedName));
            }
            UHCPlayer killer;
            if (deathMessagePattern.killerName == null) return;
            if (playerMap.containsKey(deathMessagePattern.killerName)) killer = playerMap.get(deathMessagePattern.killerName);
            else {
                killer = new UHCPlayer(deathMessagePattern.killerName);
                playerMap.put(deathMessagePattern.killerName, killer);
            }
            UHCItem item = UHCItem.parse(deathMessagePattern.itemName);
            if (item != null) killer.addUHCItem(item);
            killCountMap.merge(killer, 1, Integer::sum);
            reverseOrderKillCountMap = killCountMap.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).collect(Collectors.toList());
            naturalOrderKillCountMap = killCountMap.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.naturalOrder())).collect(Collectors.toList());
            return;
        }
    }

    @SubscribeEvent
    public void onClientGameStartMessageReceived(ClientChatReceivedEvent event) {
        if (event.type != 0) return;
        String message = StringUtil.removeFormattingCodes(event.message.getFormattedText());
        Matcher matcher = gameStartPattern.matcher(message);
        if (!matcher.matches()) return;
        killCountMap.clear();
        naturalOrderKillCountMap.clear();
        reverseOrderKillCountMap.clear();
        ChatUtil.addChatMessageWithPrefix(EnumChatFormatting.GREEN + "Cleared the kill tracker automatically because the new game started.");
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (!show) return;
        if (event.type == RenderGameOverlayEvent.ElementType.TEXT) {
            ScaledResolution resolution = event.resolution;
            FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
            int i = 0;
            switch (position) {
                case "top-right":
                    for (Map.Entry<UHCPlayer, Integer> entry : reverseOrderKillCountMap) {
                        String text = entry.getKey().toString() + ": " + entry.getValue() + " Kills";
                        fr.drawStringWithShadow(
                                text,
                                resolution.getScaledWidth() - xOffset - fr.getStringWidth(text),
                                yOffset + (i * fr.FONT_HEIGHT),
                                color
                        );
                        ++i;
                    }
                    break;
                case "top-left":
                    for (Map.Entry<UHCPlayer, Integer> entry : reverseOrderKillCountMap) {
                        String text = entry.getValue() + " Kills :" + entry.getKey().toString();
                        fr.drawStringWithShadow(
                                text,
                                xOffset,
                                yOffset + (i * fr.FONT_HEIGHT),
                                color
                        );
                        ++i;
                    }
                    break;
                case "bottom-right":
                    for (Map.Entry<UHCPlayer, Integer> entry : naturalOrderKillCountMap) {
                        String text = entry.getKey().toString() + ": " + entry.getValue() + " Kills";
                        fr.drawStringWithShadow(
                                text,
                                resolution.getScaledWidth() - xOffset - fr.getStringWidth(text),
                                resolution.getScaledHeight() - yOffset - ((i + 1) * fr.FONT_HEIGHT),
                                color
                        );
                        ++i;
                    }
                    break;
                case "bottom-left":
                    for (Map.Entry<UHCPlayer, Integer> entry : naturalOrderKillCountMap) {
                        String text = entry.getValue() + " Kills :" + entry.getKey().toString();
                        fr.drawStringWithShadow(
                                text,
                                xOffset,
                                resolution.getScaledHeight() - yOffset - ((i + 1) * fr.FONT_HEIGHT),
                                color
                        );
                        ++i;
                    }
                    break;
            }
        }
    }

    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event) {
        if (keyBinding.isPressed()) {
            show = !show;
        }
    }

    @Override
    public String getCommandName() {
        return commandName;
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
            case "clear":
                killCountMap.clear();
                naturalOrderKillCountMap.clear();
                reverseOrderKillCountMap.clear();
                ChatUtil.addChatMessageWithPrefix(EnumChatFormatting.GREEN + "Cleared the kill tracker.");
                break;
            case "toggle":
                show = !show;
                showProperty.set(show);
                configuration.save();
                ChatUtil.addChatMessageWithPrefix(EnumChatFormatting.GREEN + "Toggled the kill tracker.");
                break;
            case "show":
                show = true;
                showProperty.set(true);
                configuration.save();
                ChatUtil.addChatMessageWithPrefix(EnumChatFormatting.GREEN + "Showed the kill tracker.");
                break;
            case "hide":
                show = false;
                showProperty.set(false);
                configuration.save();
                ChatUtil.addChatMessageWithPrefix(EnumChatFormatting.GREEN + "Hid the kill tracker.");
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
                        ChatUtil.addChatMessageWithPrefix(EnumChatFormatting.GREEN + "Changed the position of the kill tracker.");
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
                    ChatUtil.addChatMessageWithPrefix(EnumChatFormatting.GREEN + "Changed the x offset of the kill tracker.");
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
                    ChatUtil.addChatMessageWithPrefix(EnumChatFormatting.GREEN + "Changed the y offset of the kill tracker.");
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
                    ChatUtil.addChatMessageWithPrefix(EnumChatFormatting.GREEN + "Changed the color of the kill tracker.");
                } catch (NumberFormatException ignored) {
                    ChatUtil.addChatMessageWithPrefix(EnumChatFormatting.RED + "Argument is invalid. " + colorUsage);
                }
                break;
            default:
                ChatUtil.addChatMessageWithPrefix(EnumChatFormatting.RED + "Subcommand is invalid. Usage: " + getCommandUsage(sender));
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

    public static class DeathMessagePattern {
        private static final Pattern PLAYER_NAME_PATTERN;
        private static final Pattern ITEM_NAME_PATTERN;
        private final Pattern pattern;
        private final int killerPosition;
        private String killedName;
        private String killerName;
        private String itemName;

        static {
            PLAYER_NAME_PATTERN = Pattern.compile("\\w{1,16}");
            ITEM_NAME_PATTERN = Pattern.compile("\\[.*\\]");
        }

        public DeathMessagePattern(Pattern pattern, int killerPosition) {
            this.pattern = pattern;
            this.killerPosition = killerPosition;
        }

        // return whether someone died or not
        public boolean matches(String message) {
            killedName = null;
            killerName = null;
            itemName = null;
            Matcher matcher = pattern.matcher(message);
            if (!matcher.matches()) return false;
            Matcher playerNameMatcher = PLAYER_NAME_PATTERN.matcher(message);
            boolean matchPlayerName;
            matchPlayerName = playerNameMatcher.find();
            if (!matchPlayerName) return false;
            killedName = playerNameMatcher.group();
            for (int i = 1; i < killerPosition; ++i) matchPlayerName = playerNameMatcher.find();
            if (!matchPlayerName) return true;
            String killerName = playerNameMatcher.group();
            switch (killerName) {
                case "Zombie":
                case "Skeleton":
                case "Spider":
                case "Enderman":
                    return true;
                default:
                    this.killerName = killerName;
                    Matcher itemNameMatcher = ITEM_NAME_PATTERN.matcher(message);
                    boolean ignored = itemNameMatcher.find();
                    if (!ignored) return true;
                    itemName = itemNameMatcher.group();
                    return true;
            }
        }
    }

    public static class UHCPlayer {
        private final String name;
        private final Collection<UHCItem> items;

        public UHCPlayer(String name) {
            this.name = name;
            items = new ArrayList<>();
        }

        public void addUHCItem(UHCItem item) {
            if (!items.contains(item)) items.add(item);
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof UHCPlayer)) return false;
            UHCPlayer compare = (UHCPlayer) object;
            return this.name.equals(compare.name);
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            for (UHCItem item : items) stringBuilder.append(item.texture).append(" ");
            stringBuilder.append(name);
            return stringBuilder.toString();
        }
    }

    public enum UHCItem {
        APPRENTICE_SWORD("[Apprentice Sword]", EnumChatFormatting.GREEN + "AP" + EnumChatFormatting.RESET),
        DRAGON_SWORD("[Dragon Sword]", EnumChatFormatting.DARK_PURPLE + "DR" + EnumChatFormatting.RESET),
        AXE_OF_PERUN("[Axe of Perun]", EnumChatFormatting.GOLD + "PE" + EnumChatFormatting.RESET),
        EXCALIBUR("[Excalibur]", EnumChatFormatting.GOLD + "EX" + EnumChatFormatting.RESET),
        ANDURIL("[Andúril]", EnumChatFormatting.YELLOW + "AN" + EnumChatFormatting.RESET),
        BLOODLUST("[Bloodlust]", EnumChatFormatting.RED + "BL" + EnumChatFormatting.RESET),
        ;

        private final String displayName;
        private final String texture;

        UHCItem(String displayName, String texture) {
            this.displayName = displayName;
            this.texture = texture;
        }

        public static UHCItem parse(String displayName) {
            for (UHCItem item : UHCItem.values()) {
                if (item.displayName.equals(displayName)) return item;
            }
            return null;
        }
    }
}
