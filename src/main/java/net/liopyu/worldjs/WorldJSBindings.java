package net.liopyu.worldjs;

import dev.latvian.mods.kubejs.util.UtilsJS;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

public class WorldJSBindings {

    public static OreConfiguration.TargetBlockState targetBlockState(RuleTest target, String blockState) {
        return OreConfiguration.target(target, UtilsJS.parseBlockState(blockState));
    }
}
