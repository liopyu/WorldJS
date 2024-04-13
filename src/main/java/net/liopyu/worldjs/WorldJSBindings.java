package net.liopyu.worldjs;

import net.liopyu.worldjs.utils.Builders;
import net.minecraft.Util;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.BlockColumnConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RuleBasedBlockStateProvider;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

public class WorldJSBindings {

    public static OreConfiguration.TargetBlockState targetBlockState(RuleTest target, BlockState blockState) {
        return OreConfiguration.target(target, blockState);
    }

    public static RuleBasedBlockStateProvider ruleBasedBlockStateProvider(BlockStateProvider fallback, Builders.BlockStateProviderRules.Builder rules) {
        return new RuleBasedBlockStateProvider(fallback, Util.make(new Builders.BlockStateProviderRules(), rules).rules);
    }

    public static BlockColumnConfiguration.Layer blockColumnLayer(IntProvider height, BlockStateProvider state) {
        return new BlockColumnConfiguration.Layer(height, state);
    }
}
