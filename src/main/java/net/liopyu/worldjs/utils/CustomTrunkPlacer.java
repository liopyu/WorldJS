package net.liopyu.worldjs.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class CustomTrunkPlacer extends TrunkPlacer {
    // Define custom parameters to control trunk size
    private final int trunkRadius;

    public CustomTrunkPlacer(int baseHeight, int heightRandA, int heightRandB, int trunkRadius) {
        super(baseHeight, heightRandA, heightRandB);
        this.trunkRadius = trunkRadius;
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return null;
    }

    // Override the placeTrunk method to adjust trunk size
    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter, RandomSource random, int freeTreeHeight, BlockPos pos, TreeConfiguration config) {
        List<FoliagePlacer.FoliageAttachment> attachments = new ArrayList<>();

        // Modify the logic to place logs in a larger radius
        for (int x = -trunkRadius; x <= trunkRadius; x++) {
            for (int z = -trunkRadius; z <= trunkRadius; z++) {
                // Adjust the offset based on trunk radius
                BlockPos offsetPos = pos.offset(x, 0, z);
                placeLogIfFree(level, blockSetter, random, offsetPos.mutable(), config);
            }
        }

        // Add foliage attachment for the top of the tree
        attachments.add(new FoliagePlacer.FoliageAttachment(pos.above(freeTreeHeight), -2, false));

        return attachments;
    }

    // Other methods as needed
}

