package net.momostudios.mediumcore.core.network;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.momostudios.mediumcore.Mediumcore;
import net.momostudios.mediumcore.core.network.message.DeathsSyncMessage;

public class MediumcorePacketHandler
{
    private static final String PROTOCOL_VERSION = "0.1.0";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Mediumcore.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void init()
    {
        INSTANCE.registerMessage(0, DeathsSyncMessage.class, DeathsSyncMessage::encode, DeathsSyncMessage::decode, DeathsSyncMessage::handle);
    }
}
