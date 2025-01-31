package me.asuks.uhce;

import me.asuks.uhce.hud.KillTracker;
import me.asuks.uhce.hud.Memo;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = UHCE.MODID, name = UHCE.MODNAME, version = UHCE.VERSION)
public class UHCE {

    public static final String MODID = "uhcenhancements";
    public static final String MODNAME = "UHCE";
    public static final String VERSION = "0.0";

    @EventHandler
    public void init(FMLInitializationEvent event) {
        Memo memo = new Memo();
        ClientCommandHandler.instance.registerCommand(memo);
        MinecraftForge.EVENT_BUS.register(memo);
        KillTracker killTracker = new KillTracker();
        ClientCommandHandler.instance.registerCommand(killTracker);
        MinecraftForge.EVENT_BUS.register(killTracker);
    }
}
