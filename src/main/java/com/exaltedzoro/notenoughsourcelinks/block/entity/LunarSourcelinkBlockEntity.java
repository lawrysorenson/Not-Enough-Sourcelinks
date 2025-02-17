package com.exaltedzoro.notenoughsourcelinks.block.entity;

import com.hollingsworth.arsnouveau.api.source.ISpecialSourceProvider;
import com.hollingsworth.arsnouveau.api.util.SourceUtil;
import com.hollingsworth.arsnouveau.client.particle.ParticleColor;
import com.hollingsworth.arsnouveau.client.particle.ParticleLineData;
import com.hollingsworth.arsnouveau.client.particle.ParticleUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class LunarSourcelinkBlockEntity extends BlockEntity {
    private static final int distance = 5;
    private int source = 0;
    private final int max_source = 5000;

    public LunarSourcelinkBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LUNAR_SOURCELINK.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LunarSourcelinkBlockEntity pEntity) {

        long timeOfDay = level.getDayTime() % 24000;

        int peakTime = 18500;
        int peakWidth = 1500;

        boolean isDay = timeOfDay < 13000;
        boolean primetime = Math.abs(timeOfDay - peakTime) < peakWidth;

        if (level.isClientSide) {
            // 13000 % 24000
            if (isDay) return;

            int baseAge = primetime ? 30 : 60;
            int randBound = primetime ? 4 : 8;
            int numParticles = primetime ? 2 : 1;
            float scaleAge = primetime ? (float) ParticleUtil.inRange(0.1, 0.2) : (float) ParticleUtil.inRange(0.05, 0.15);
            if (level.random.nextInt(randBound) == 0 && !Minecraft.getInstance().isPaused()) {
                for (int i = 0; i < numParticles; i++) {
                    Vec3 particlePos = new Vec3(pos.getX(), pos.getY(), pos.getZ()).add(0.5, 0.8, 0.5);
                    particlePos = particlePos.add(ParticleUtil.pointInSphere());
                    level.addParticle(ParticleLineData.createData(new ParticleColor(255, 25, 180), scaleAge, baseAge + level.random.nextInt(20)),
                            particlePos.x(), particlePos.y(), particlePos.z(),
                            pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5);
                }
            }

            return;
        }

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

        if(level.getGameTime() % 400 == 0 && !isDay && pEntity.canSeeSky(pos, level)) {
            int nearbyLinks = -1;
            for(BlockPos pPos : BlockPos.betweenClosed(pos.above(distance).north(distance).east(distance), pos.below(distance).south(distance).west(distance))) {
                if(level.getBlockEntity(pPos) instanceof LunarSourcelinkBlockEntity) {
                    nearbyLinks++;
                }
            }
            int toAdd = (int) (100 / Math.pow(2, nearbyLinks));
            if (primetime) toAdd = toAdd * 3 / 2;
            pEntity.source = Math.min(pEntity.source + toAdd, pEntity.max_source);
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
