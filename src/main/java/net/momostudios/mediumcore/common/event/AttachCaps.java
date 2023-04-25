package net.momostudios.mediumcore.common.event;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.momostudios.mediumcore.Mediumcore;
import net.momostudios.mediumcore.common.capability.DeathCapability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class AttachCaps
{
    @SubscribeEvent
    public static void attachCapabilityToEntityHandler(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof Player)
        {
            DeathCapability cap = new DeathCapability();
            LazyOptional<DeathCapability> optionalStorage = LazyOptional.of(() -> cap);

            ICapabilityProvider provider = new ICapabilitySerializable<CompoundTag>()
            {
                @Nonnull
                @Override
                public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
                {
                    if (cap == DeathCapability.DEATHS)
                    {   return optionalStorage.cast();
                    }
                    return LazyOptional.empty();
                }

                @Override
                public CompoundTag serializeNBT()
                {   return cap.serializeNBT();
                }

                @Override
                public void deserializeNBT(CompoundTag nbt)
                {   cap.deserializeNBT(nbt);
                }
            };

            event.addCapability(new ResourceLocation(Mediumcore.MOD_ID, "deaths"), provider);
        }
    }
}
