package net.momostudios.mediumcore.common.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class DeathCapability
{
    public static Capability<DeathCapability> DEATHS = CapabilityManager.get(new CapabilityToken<>() {});

    int deaths = 0;
    boolean isDown = false;
    int downTimeLeft;
    BlockPos downPos = null;
    boolean isSpectator = false;
    boolean isBeingRevived = false;
    Player revivingPlayer = null;

    // Number of deaths
    public int getDeaths()
    {
        return deaths;
    }
    public void setDeaths(int deaths)
    {
        this.deaths = deaths;
    }
    public void addDeaths(int deaths)
    {
        this.deaths += deaths;
    }

    // Is player down
    public boolean isDown()
    {
        return isDown;
    }
    public void setDown(boolean isDown)
    {
        this.isDown = isDown;
    }

    // Down time left
    public int getDownTimeLeft()
    {
        return downTimeLeft;
    }
    public void setDownTimeLeft(int downTimeLeft)
    {
        this.downTimeLeft = downTimeLeft;
    }
    public void addDownTimeLeft(int downTimeLeft)
    {
        this.downTimeLeft += downTimeLeft;
    }

    // Down position
    public BlockPos getDownPos()
    {
        return downPos;
    }
    public void setDownPos(BlockPos downPos)
    {
        this.downPos = downPos;
    }

    // Is player spectator
    public boolean isSpectator()
    {
        return isSpectator;
    }
    public void setSpectator(boolean isSpectator)
    {
        this.isSpectator = isSpectator;
    }

    // Is player being revived
    public boolean isBeingRevived()
    {
        return isBeingRevived;
    }
    public void setBeingRevived(boolean isBeingRevived)
    {
        this.isBeingRevived = isBeingRevived;
    }

    // Get player reviving this entity
    public Player getRevivingPlayer()
    {
        return revivingPlayer;
    }
    public void setRevivingPlayer(Player revivingPlayer)
    {
        this.revivingPlayer = revivingPlayer;
    }

    public CompoundTag serializeNBT()
    {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("Deaths", deaths);
        nbt.putBoolean("IsDown", isDown);
        nbt.putInt("DownTimeLeft", downTimeLeft);
        if (downPos != null)
        {   nbt.putLong("DownPos", downPos.asLong());
        }
        nbt.putBoolean("IsSpectator", isSpectator);
        return nbt;
    }

    public void deserializeNBT(CompoundTag nbt)
    {
        deaths = nbt.getInt("Deaths");
        isDown = nbt.getBoolean("IsDown");
        downTimeLeft = nbt.getInt("DownTimeLeft");
        if (nbt.contains("DownPos"))
        {   downPos = BlockPos.of(nbt.getLong("DownPos"));
        }
        isSpectator = nbt.getBoolean("IsSpectator");
    }
}