package net.momostudios.mediumcore.common.capability;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class DeathCapability
{
    @CapabilityInject(DeathCapability.class)
    public static Capability<DeathCapability> DEATHS;

    int deaths = 0;
    boolean isDown = false;
    int downTimeLeft;
    BlockPos downPos = null;
    boolean isSpectator = false;
    boolean isBeingRevived = false;
    PlayerEntity revivingPlayer = null;

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
    public PlayerEntity getRevivingPlayer()
    {
        return revivingPlayer;
    }
    public void setRevivingPlayer(PlayerEntity revivingPlayer)
    {
        this.revivingPlayer = revivingPlayer;
    }
}