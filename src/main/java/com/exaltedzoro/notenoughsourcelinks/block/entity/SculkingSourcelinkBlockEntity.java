package com.exaltedzoro.notenoughsourcelinks.block.entity;


import com.exaltedzoro.notenoughsourcelinks.NotEnoughSourcelinks;
import com.hollingsworth.arsnouveau.api.source.ISpecialSourceProvider;
import com.hollingsworth.arsnouveau.api.util.SourceUtil;
import com.hollingsworth.arsnouveau.client.particle.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = NotEnoughSourcelinks.MOD_ID)
public class SculkingSourcelinkBlockEntity extends BlockEntity {
    private int source = 0;
    private final int max_source = 5000;

    private final int EXP_RATIO = 50;

    public SculkingSourcelinkBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.SCULKING_SOURCELINK.get(), pPos, pBlockState);
    }

    @SubscribeEvent
    public static void onLivingEntityDeath(LivingDeathEvent event) {
        int experience = event.getEntity().getExperienceReward();
        Level level = event.getEntity().level();
        BlockPos pos = event.getEntity().blockPosition();
        for(BlockPos pPos : BlockPos.betweenClosed(pos.above(5).north(5).east(5), pos.below(5).south(5).west(5))) {
            BlockEntity entity = level.getBlockEntity(pPos);
            if(entity instanceof SculkingSourcelinkBlockEntity sourceLink && sourceLink.activate(experience)) {
                event.getEntity().skipDropExperience();
                break;
            }
        }
    }

    public boolean activate(int experience) {
        if (!addSource(experience * EXP_RATIO)) return false;
        level.playSound(null, worldPosition, SoundEvents.SCULK_BLOCK_PLACE, SoundSource.BLOCKS, 1, 1);
        return true;
    }

    public boolean addSource(int source) {
        if(this.source + source > max_source) return false;
        this.source += source;
        return true;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SculkingSourcelinkBlockEntity entity) {
        if(level.isClientSide) return;

        if(entity.source > 0) {
            List<ISpecialSourceProvider> nearbyJars = SourceUtil.canGiveSource(pos, level, 5);
            if(!nearbyJars.isEmpty()) {
                int sourceTransferred = nearbyJars.get(0).getSource().addSource(entity.source);
                ParticleUtil.spawnFollowProjectile(level, pos, nearbyJars.get(0).getCurrentPos());
                if(entity.source - sourceTransferred < 0) {
                    entity.source = 0;
                } else {
                    entity.source -= sourceTransferred;
                }
            }
        }
    }
}
