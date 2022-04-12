package net.momostudios.mediumcore.common.event;

import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;
import net.momostudios.mediumcore.common.capability.DeathCapability;
import net.momostudios.mediumcore.core.network.MediumcorePacketHandler;
import net.momostudios.mediumcore.core.network.message.DeathsSyncMessage;
import net.momostudios.mediumcore.core.util.MediumcoreDamageSources;

@Mod.EventBusSubscriber
public class PlayerHandler
{
    public static boolean showAnimation = false;
    public static ItemStack animationStack = ItemStack.EMPTY;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (showAnimation)
        {
            Minecraft.getInstance().gameRenderer.displayItemActivation(animationStack);
            showAnimation = false;
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            if (event.player.getPersistentData().getInt("resistanceTime") > 0)
            {
                event.player.getPersistentData().putInt("resistanceTime", event.player.getPersistentData().getInt("resistanceTime") - 1);
            }
            PlayerEntity player = event.player;
            if (!player.world.isRemote && player.ticksExisted % 20 == 0)
            {
                updateDeathState(player);
            }
            player.getCapability(DeathCapability.DEATHS).ifPresent(cap ->
            {
                cap.setSpectator(player.getMaxHealth() < 2 && cap.getDeaths() >= 10);

                if (cap.isDown())
                {
                    player.setPose(Pose.STANDING);
                    player.abilities.isFlying = false;
                    player.sendPlayerAbilities();

                    if (player.isSneaking() && player.ticksExisted % 3 == 0)
                    {
                        cap.addDownTimeLeft(-1);
                    }

                    if (cap.getDownPos() != null)
                    {
                        if (player.isInWaterOrBubbleColumn())
                        {
                            player.setPosition(cap.getDownPos().getX() + 0.5, player.getPosY(), cap.getDownPos().getZ() + 0.5);
                            double waterLevel = player.world.getBlockState(player.getPosition().up()).getBlock() != Blocks.WATER ?
                                    ((player.getPosition().getY() + 1.4) - player.getPosY()) :
                                    ((player.getPosition().getY() + 2) - player.getPosY());
                            player.addVelocity(0, 0.01 * waterLevel, 0);
                        }
                        else
                        {
                            player.setPosition(cap.getDownPos().getX() + 0.5, player.getPosY(), cap.getDownPos().getZ() + 0.5);
                        }
                    }

                    player.setGlowing(true);
                    if (player.ticksExisted % 20 == 0)
                    {
                        boolean beingRevived = false;
                        AxisAlignedBB bb = new AxisAlignedBB(player.getPositionVec().add(-1.5, -1.5, -1.5), player.getPositionVec().add(1.5, 1.5, 1.5));
                        for (Entity entity : player.world.getEntitiesWithinAABBExcludingEntity(player, bb))
                        {
                            if (entity instanceof PlayerEntity && entity.isSneaking())
                            {
                                beingRevived = true;
                                break;
                            }
                        }
                        cap.setBeingRevived(beingRevived);

                        if (cap.getDownTimeLeft() <= 0)
                        {
                            if (!player.abilities.isCreativeMode)
                            {
                                player.abilities.disableDamage = false;
                            }
                            player.sendPlayerAbilities();
                            player.attackEntityFrom(MediumcoreDamageSources.BLEED_OUT, Float.MAX_VALUE);
                        }
                        if (!cap.isBeingRevived() && !player.isSneaking())
                        {
                            cap.addDownTimeLeft(-1);
                        }
                    }
                }
                else
                {
                    boolean spec = cap.isSpectator() && !player.abilities.isCreativeMode;
                    if (spec) player.abilities.isFlying = true;

                    if (player.ticksExisted % 20 == 0)
                    {
                        player.abilities.disableDamage = player.abilities.isCreativeMode;
                        player.setGlowing(false);
                        //player.abilities.setFlySpeed(spec ? 0.03f : 0.045f);
                        player.abilities.allowEdit = !spec;
                        player.sendPlayerAbilities();
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public static void onHeal(LivingHealEvent event)
    {
        if (event.getEntityLiving().getCapability(DeathCapability.DEATHS).map(DeathCapability::isDown).orElse(false))
        {
            event.setCanceled(true);
        }
    }

    // Prevent mining if down
    @SubscribeEvent
    public static void onPlayerMine(PlayerEvent.BreakSpeed event)
    {
        event.getPlayer().getCapability(DeathCapability.DEATHS).ifPresent(cap ->
        {
            if (cap.isDown() || cap.isSpectator())
            {
                event.setNewSpeed(0f);
            }
        });
    }

    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event)
    {
        if (event.getEntityLiving() instanceof PlayerEntity && event.getEntityLiving().getPersistentData().getInt("resistanceTime") > 0
        && event.getSource() != DamageSource.OUT_OF_WORLD)
        {
            event.setCanceled(true);
        }
    }

    // Prevent attacking if down
    @SubscribeEvent
    public static void onPlayerAttack(LivingAttackEvent event)
    {
        if (event.getSource().getTrueSource() instanceof PlayerEntity &&
        event.getSource().getTrueSource().getCapability(DeathCapability.DEATHS).map(DeathCapability::isDown).orElse(false))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (event.getEntity() instanceof PlayerEntity)
        {
            PlayerEntity player = (PlayerEntity) event.getEntity();
            player.getCapability(DeathCapability.DEATHS).ifPresent(cap ->
            {
                cap.setDeaths(player.getPersistentData().getInt("deaths"));
                cap.setDown(player.getPersistentData().getBoolean("down"));
                cap.setDownTimeLeft(player.getPersistentData().getInt("downTimeLeft"));
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event)
    {
        if (event.getEntity() instanceof PlayerEntity)
        {
            PlayerEntity player = (PlayerEntity) event.getEntity();
            player.getCapability(DeathCapability.DEATHS).ifPresent(cap ->
            {
                player.getPersistentData().putInt("deaths", cap.getDeaths());
                player.getPersistentData().putBoolean("down", cap.isDown());
                player.getPersistentData().putInt("downTimeLeft", cap.getDownTimeLeft());
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.Clone event)
    {
        PlayerEntity player = event.getPlayer();
        PlayerEntity oldPlayer = event.getOriginal();
        oldPlayer.getCapability(DeathCapability.DEATHS).ifPresent(cap ->
        {
            player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(player.getMaxHealth() - cap.getDeaths() * 2);
            player.getCapability(DeathCapability.DEATHS).ifPresent(cap2 ->
            {
                cap2.setDeaths(cap.getDeaths());
            });
        });
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event)
    {
        if (event.getEntityLiving() instanceof PlayerEntity && !event.getEntityLiving().world.isRemote)
        {
            PlayerEntity player = (PlayerEntity) event.getEntityLiving();
            if (player.world.getPlayers().size() > 1 && !player.abilities.isCreativeMode)
            {
                player.getCapability(DeathCapability.DEATHS).ifPresent(cap ->
                {
                    if (!cap.isDown())
                    {
                        player.setHealth(1);
                        event.setCanceled(true);
                        cap.setDown(true);
                        cap.setDownTimeLeft(60);
                        cap.setDownPos(player.getPosition());
                        player.abilities.disableDamage = true;
                    }
                    else
                    {
                        cap.setDown(false);

                        if (player.getMaxHealth() > 1)
                        {
                            cap.addDeaths(1);
                        }
                    }
                });
                updateDeathState(player);
            }
            else
            {
                player.getCapability(DeathCapability.DEATHS).ifPresent(cap ->
                {
                    if (player.getMaxHealth() > 1)
                    {
                        cap.addDeaths(1);
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public static void revivePlayer(TickEvent.PlayerTickEvent event)
    {
        if (!event.player.world.isRemote && event.phase == TickEvent.Phase.START
        && !event.player.getCapability(DeathCapability.DEATHS).map(DeathCapability::isDown).orElse(false))
        {
            AxisAlignedBB bb = new AxisAlignedBB(event.player.getPositionVec().add(-1.5, -1.5, -1.5), event.player.getPositionVec().add(1.5, 1.5, 1.5));
            for (Entity entity : event.player.world.getEntitiesWithinAABBExcludingEntity(event.player, bb))
            {
                if (entity instanceof PlayerEntity)
                {
                    PlayerEntity downPlayer = (PlayerEntity) entity;
                    if (event.player.isSneaking())
                    {
                        downPlayer.getCapability(DeathCapability.DEATHS).ifPresent(cap ->
                        {
                            if (cap.isDown() && (cap.getRevivingPlayer() == null || !cap.isBeingRevived() || cap.getRevivingPlayer() == event.player))
                            {
                                cap.setBeingRevived(true);
                                cap.setRevivingPlayer(event.player);
                                if (event.player.ticksExisted % 20 == 0)
                                {
                                    cap.setDownTimeLeft(Math.min(Math.max(50, cap.getDownTimeLeft() + 1), 60));
                                    updateDeathState(downPlayer);
                                }
                                if (cap.getDownTimeLeft() >= 60)
                                {
                                    downPlayer.getPersistentData().putInt("resistanceTime", 200);
                                    cap.setBeingRevived(false);
                                    cap.setDown(false);
                                    downPlayer.setGlowing(false);
                                    updateDeathState(downPlayer);
                                    downPlayer.abilities.disableDamage = downPlayer.abilities.isCreativeMode;
                                }
                                StringTextComponent message = new StringTextComponent("Reviving... (§e" + Math.min(10, 60 - cap.getDownTimeLeft()) + " seconds left§r)");
                                downPlayer.sendStatusMessage(message, true);
                                event.player.sendStatusMessage(message, true);
                            }
                        });
                    }
                    else
                    {
                        downPlayer.getCapability(DeathCapability.DEATHS).ifPresent(cap -> cap.setBeingRevived(false));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickPlayer(PlayerInteractEvent.EntityInteractSpecific event)
    {
        if (event.getPlayer().getCapability(DeathCapability.DEATHS).map(DeathCapability::isDown).orElse(false))
        {
            event.setCanceled(true);
            return;
        }
        if (event.getTarget() instanceof PlayerEntity && !event.getTarget().world.isRemote)
        {
            PlayerEntity target = (PlayerEntity) event.getTarget();
            target.getCapability(DeathCapability.DEATHS).ifPresent(cap ->
            {
                if (cap.isDown())
                {
                    target.abilities.disableDamage = false;
                    target.sendPlayerAbilities();
                    target.attackEntityFrom(MediumcoreDamageSources.gimpPlayerFrom(event.getPlayer()), Float.MAX_VALUE);
                    target.world.playSound(null, target.getPosition(), SoundEvents.ENTITY_GENERIC_BIG_FALL, SoundCategory.PLAYERS, 1, 1);
                }
            });
        }
    }

    public static void updateDeathState(PlayerEntity player)
    {
        if (!player.world.isRemote)
        {
            player.getCapability(DeathCapability.DEATHS).ifPresent(cap ->
            {
                MediumcorePacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                new DeathsSyncMessage(cap.getDeaths(), cap.isDown(), cap.getDownTimeLeft(), player.getUniqueID(), cap.isBeingRevived(), player.getPosition()));
            });
        }
    }
}
