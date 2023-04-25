package net.momostudios.mediumcore.core.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
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

    public DeathsSyncMessage(int deaths, boolean isDown, int downTimeLeft, UUID uuid, boolean beingRevived, BlockPos blockPosition)
    {
        this.deaths = deaths;
        this.isDown = isDown;
        this.uuid = uuid;
        this.downTimeLeft = downTimeLeft;
        this.beingRevived = beingRevived;
        this.pos = blockPosition;
    }

    public static void encode(DeathsSyncMessage message, FriendlyByteBuf buf) {
        buf.writeInt(message.deaths);
        buf.writeBoolean(message.isDown);
        buf.writeInt(message.downTimeLeft);
        buf.writeUUID(message.uuid);
        buf.writeBoolean(message.beingRevived);
        buf.writeBlockPos(message.pos);
    }

    public static DeathsSyncMessage decode(FriendlyByteBuf buf) {
        return new DeathsSyncMessage(buf.readInt(), buf.readBoolean(), buf.readInt(), buf.readUUID(), buf.readBoolean(), buf.readBlockPos());
    }

    public static void handle(DeathsSyncMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient())
        {
            context.enqueueWork(() ->
            {
                if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.level.getPlayerByUUID(message.uuid) != null)
                {
                    Minecraft.getInstance().player.level.getPlayerByUUID(message.uuid).getCapability(DeathCapability.DEATHS).ifPresent(deaths ->
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
