package net.momostudios.mediumcore.core.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.momostudios.mediumcore.common.capability.DeathCapability;

import java.util.UUID;
import java.util.function.Supplier;

public class DeathsSyncMessage
{
    int deaths;
    boolean isDown;
    UUID uuid;
    int downTimeLeft;
    boolean beingRevived;
    BlockPos pos;

    public DeathsSyncMessage(int deaths, boolean isDown, int downTimeLeft, UUID uuid, boolean beingRevived, BlockPos pos)
    {
        this.deaths = deaths;
        this.isDown = isDown;
        this.uuid = uuid;
        this.downTimeLeft = downTimeLeft;
        this.beingRevived = beingRevived;
        this.pos = pos;
    }

    public static void encode(DeathsSyncMessage message, PacketBuffer buf) {
        buf.writeInt(message.deaths);
        buf.writeBoolean(message.isDown);
        buf.writeInt(message.downTimeLeft);
        buf.writeUniqueId(message.uuid);
        buf.writeBoolean(message.beingRevived);
        buf.writeBlockPos(message.pos);
    }

    public static DeathsSyncMessage decode(PacketBuffer buf) {
        return new DeathsSyncMessage(buf.readInt(), buf.readBoolean(), buf.readInt(), buf.readUniqueId(), buf.readBoolean(), buf.readBlockPos());
    }

    public static void handle(DeathsSyncMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient())
        {
            context.enqueueWork(() ->
            {
                if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.world.getPlayerByUuid(message.uuid) != null)
                {
                    Minecraft.getInstance().player.world.getPlayerByUuid(message.uuid).getCapability(DeathCapability.DEATHS).ifPresent(deaths ->
                    {
                        deaths.setDeaths(message.deaths);
                        deaths.setDown(message.isDown);
                        deaths.setDownPos(message.pos);
                        deaths.setDownTimeLeft(message.downTimeLeft);
                        deaths.setBeingRevived(message.beingRevived);
                    });
                }
            });
        }
        context.setPacketHandled(true);
    }
}
