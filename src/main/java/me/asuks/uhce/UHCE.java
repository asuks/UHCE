package me.asuks.uhce;

import me.asuks.uhce.feature.KillTracker;
import me.asuks.uhce.feature.Memo;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;

@Mod(modid = UHCE.MODID, name = UHCE.MODNAME, version = UHCE.VERSION)
public class UHCE {

    public static final String MODID = "uhcenhancements";
    public static final String MODNAME = "UHCE";
    public static final String VERSION = "0.1";
    private Configuration configuration;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        configuration = new Configuration(new File(event.getModConfigurationDirectory(), "uhce.cfg"));
        configuration.load();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        Memo memo = new Memo(configuration);
        ClientCommandHandler.instance.registerCommand(memo);
        MinecraftForge.EVENT_BUS.register(memo);
        KillTracker killTracker = new KillTracker(configuration);
        ClientCommandHandler.instance.registerCommand(killTracker);
        MinecraftForge.EVENT_BUS.register(killTracker);
    }
}
