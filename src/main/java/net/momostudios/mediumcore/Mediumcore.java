package net.momostudios.mediumcore;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.momostudios.mediumcore.core.init.ItemInit;
import net.momostudios.mediumcore.core.network.MediumcorePacketHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("mediumcore")
public class Mediumcore
{
    private static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "mediumcore";

    public Mediumcore()
    {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(this::commonSetup);
        ItemInit.ITEMS.register(bus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static ItemGroup MEDIUMCORE_GROUP = new ItemGroup("mediumcore")
    {
        @Override
        public ItemStack createIcon()
        {
            return new ItemStack(ItemInit.REBIRTH_TOTEM.get());
        }
    };

    @SubscribeEvent
    public void commonSetup(final FMLCommonSetupEvent event)
    {
        MediumcorePacketHandler.init();
    }
}
