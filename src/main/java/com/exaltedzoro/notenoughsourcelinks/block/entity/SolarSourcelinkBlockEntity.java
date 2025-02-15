package com.exaltedzoro.notenoughsourcelinks.block.entity;

import com.hollingsworth.arsnouveau.api.source.ISpecialSourceProvider;
import com.hollingsworth.arsnouveau.api.util.SourceUtil;
import com.hollingsworth.arsnouveau.client.particle.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class SolarSourcelinkBlockEntity extends BlockEntity {
    private static final int distance = 5;
    private int source = 0;
    private final int max_source = 100;

    public SolarSourcelinkBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOLAR_SOURCELINK.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SolarSourcelinkBlockEntity pEntity) {
        if(level.isClientSide) return;

        if(pEntity.source > 0) {
            List<ISpecialSourceProvider> nearbyJars = SourceUtil.canGiveSource(pos, level, 5);
            if(!nearbyJars.isEmpty()) {
                int sourceTransferred = nearbyJars.get(0).getSource().addSource(pEntity.source);
                ParticleUtil.spawnFollowProjectile(level, pos, nearbyJars.get(0).getCurrentPos());
                if(pEntity.source - sourceTransferred < 0) {
                    pEntity.source = 0;
                } else {
                    pEntity.source -= sourceTransferred;
                }
            }
        }

        if(level.getGameTime() % 400 == 0 && level.isDay() && pEntity.canSeeSky(pos, level)) {
            int nearbyLinks = -1;
            for(BlockPos pPos : BlockPos.betweenClosed(pos.above(distance).north(distance).east(distance), pos.below(distance).south(distance).west(distance))) {
                if(level.getBlockEntity(pPos) instanceof SolarSourcelinkBlockEntity) {
                    nearbyLinks++;
                }
            }
            if(pEntity.source + (int) (100 / Math.pow(2, nearbyLinks)) < pEntity.max_source) {
                pEntity.source += (int) (100 / Math.pow(2, nearbyLinks));
            } else {
                pEntity.source = pEntity.max_source;
            }
        }
    }

    private boolean canSeeSky(BlockPos pos, Level level) {
        int x = pos.getX();
        int z = pos.getZ();
        for(int y = pos.getY() + 1; y <= level.getMaxBuildHeight(); y++) {
            if(!level.isEmptyBlock(new BlockPos(x, y, z))) {
                return false;
            }
        }
        return true;
    }
}
