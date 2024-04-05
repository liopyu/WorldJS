package net.liopyu.worldjs.utils;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.typings.Generics;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import net.liopyu.worldjs.api.IPFeatureMethodHolder;
import net.minecraft.core.Direction;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import org.jetbrains.annotations.Nullable;

/**
 * A collection of JS-friendly wrappers around registered {@link net.minecraft.world.level.levelgen.placement.PlacementModifier PlacementModifiers}
 */
public class PlacedFeatureBuilder {

    public transient final String feature;
    public transient final JsonArray placements;

    public PlacedFeatureBuilder(String name) {
        final String[] loc = DataUtils.normalizeName(name);
        feature = loc[0] + ":" + loc[1];
        placements = new JsonArray();
        DataUtils.setPfb(this);
    }

    @Generics(String.class)
    public ImmutableMap<String, IPFeatureMethodHolder> getMods() {
        return DataUtils.modPlacements.get();
    }

    @Info(value = "Adds the given json object to the placed feature's list of placement modifiers")
    public void addJsonPlacement(JsonObject json) {
        placements.add(json);
    }

    @Info(value = "Adds a placement modifier of the given type to the placed feature's list of placement modifiers")
    public void addSimplePlacement(String type) {
        placements.add(initModifier(type));
    }

    @Info(value = "Adds a `minecraft:count` placement modifier to the placed feature", params = {
            @Param(name = "intProvider", value = "The int provider used by the modifier")
    })
    public void count(IntProvider intProvider) {
        final JsonObject json = initModifier("count");
        json.add("count", DataUtils.encode(IntProvider.CODEC, intProvider));
        placements.add(json);
    }

    @Info(value = "Adds a `minecraft:biome` placement modifier to the placed feature")
    public void biomeFilter() {
        addSimplePlacement("biome");
    }

    // TODO: Either make a binding for BlockPredicate or make a wrapper for it
    @Info(value = "Adds a `minecraft:block_predicate_filter` placement modifier to the placed feature", params = {
            @Param(name = "blockPredicate", value = "The block predicate used by the modifier")
    })
    public void blockPredicateFilter(BlockPredicate blockPredicate) {
        final JsonObject json = initModifier("block_predicate_filter");
        json.add("predicate", DataUtils.encode(BlockPredicate.CODEC, blockPredicate));
        placements.add(json);
    }

    @Info(value = "Adds a `minecraft:carving_mask` placement modifier to the placed feature", params = {
            @Param(name = "carvingStep", value = "The carving step which this placed feature should apply")
    })
    public void carvingMask(GenerationStep.Carving carvingStep) {
        final JsonObject json = initModifier("carving_mask");
        json.addProperty("step", carvingStep.getSerializedName());
        placements.add(json);
    }

    @Info(value = "Adds a `minecraft:count_on_every_layer` modifier to the placed feature", params = {
            @Param(name = "intProvider", value = "The int provider used by the modifier")
    })
    public void countOnEveryLayer(IntProvider intProvider) {
        final JsonObject json = initModifier("count_on_every_layer");
        json.add("count", DataUtils.encode(IntProvider.CODEC, intProvider));
        placements.add(json);
    }

    @Info(value = "Adds a new `minecraft:environment_scan` placement modifier to the placed feature", params = {
            @Param(name = "direction", value = "The direction to search in, may be with `up` or `down`"),
            @Param(name = "targetCondition", value = ""), // TODO: What is this?
            @Param(name = "maxSteps", value = "The maximum number of steps, in the range [1, 32], that the modifier will run for"),
            @Param(name = "allowedSearchCondition", value = ", may be null") // TODO: What is this
    })
    public void environmentScan(Direction direction, BlockPredicate targetCondition, int maxSteps, @Nullable BlockPredicate allowedSearchCondition) {
        final JsonObject json = initModifier("environment_scan");
        json.addProperty("direction_of_search", direction.getSerializedName());
        json.add("target_condition", DataUtils.encode(BlockPredicate.CODEC, targetCondition));
        json.addProperty("max_steps", maxSteps);
        if (allowedSearchCondition != null) {
            json.add("allowed_search_condition", DataUtils.encode(BlockPredicate.CODEC, allowedSearchCondition));
        }
        placements.add(json);
    }

    // TODO: Make a binding/wrapper for HeightProviders
    @Info(value = "Adds a `minecraft:height_range` placement modifier to the placed feature", params = {
            @Param(name = "heightProvider", value = "The height provider used by the modifier")
    })
    public void heightRange(HeightProvider heightProvider) {
        final JsonObject json = initModifier("height_range");
        json.add("height", DataUtils.encode(HeightProvider.CODEC, heightProvider));
        placements.add(json);
    }

    @Info(value = "Adds a `minecraft:heightmap` placement modifier to the placed feature", params = {
            @Param(name = "heightmapType", value = "The heightmap to use for placement")
    })
    public void heightmap(Heightmap.Types heightmapType) {
        final JsonObject json = initModifier("heightmap");
        json.addProperty("heightmap", heightmapType.getSerializedName());
        placements.add(json);
    }
    
    @Info(value = "Adds a `minecraft:in_square` placement modifier to the placed feature")
    public void inSquare() {
        addSimplePlacement("in_square");
    }
    
    // TODO: Explain these args
    public void noiseBasedCount(int noiseToCountRatio, double noiseFactor, double noiseOffset) {
        final JsonObject json = initModifier("noise_based_count");
        json.addProperty("noise_to_count_ratio", noiseToCountRatio);
        json.addProperty("noise_factor", noiseFactor);
        json.addProperty("noiseOffset", noiseOffset);
        placements.add(json);
    }
    
    // TODO: Explain these args
    public void noiseThresholdCount(double noiseLevel, int belowNoise, int aboveNoise) {
        final JsonObject json = initModifier("noise_threshold_count");
        json.addProperty("noise_level", noiseLevel);
        json.addProperty("below_noise", belowNoise);
        json.addProperty("above_noise", aboveNoise);
        placements.add(json);
    }

    @Info(value = "Adds a `minecraft:random_offset` placement modifier to the placed feature", params = {
            @Param(name = "xzSpread", value = "The horizontal spread, may span values between `-16` and `16`"),
            @Param(name = "ySpread", value = "The vertical spread, may span values between `-16` and `16`")
    })
    public void randomOffset(IntProvider xzSpread, IntProvider ySpread) {
        final JsonObject json = initModifier("random_offset");
        json.add("xz_spread", DataUtils.encode(IntProvider.CODEC, xzSpread));
        json.add("y_spread", DataUtils.encode(IntProvider.CODEC, ySpread));
        placements.add(json);
    }

    @Info(value = "Adds a `minecraft:rarity_filter` placement modifier to the placed feature", params = {
            @Param(name = "chance", value = "A positive integer")
    })
    public void rarityFilter(int chance) {
        final JsonObject json = initModifier("rarity_filter");
        json.addProperty("chance", chance);
        placements.add(json);
    }

    @Info(value = "Adds a `minecraft:surface_relative_threshold_filter` placement modifier to the placed feature", params = {
            @Param(name = "heightmapType", value = "The height map to use for placement"),
            @Param(name = "minOffset", value = "The minimum vertical offset from the heightmap the feature may place"),
            @Param(name = "maxOffset", value = "The maximum vertical offset from the heightmap the feature may place")
    })
    public void surfaceRelativeThresholdFilter(Heightmap.Types heightmapType, @Nullable Integer minOffset, @Nullable Integer maxOffset) {
        final JsonObject json = initModifier("surface_relative_threshold_filter");
        json.addProperty("heightmap", heightmapType.getSerializedName());
        if (minOffset != null) {
            json.addProperty("min_inclusive", minOffset);
        }
        if (maxOffset != null) {
            json.addProperty("max_inclusive", maxOffset);
        }
        placements.add(json);
    }

    @Info(value = "Adds a `minecraft:surface_water_depth_filter` placement modifier to the placed feature", params = {
            @Param(name = "maxDepth", value = "The maximum water depth that the feature may place in")
    })
    public void surfaceWaterDepthFilter(int maxDepth) {
        final JsonObject json = initModifier("surface_water_depth_filter");
        json.addProperty("max_water_depth", maxDepth);
        placements.add(json);
    }

    private JsonObject initModifier(String type) {
        final JsonObject json = new JsonObject();
        json.addProperty("type", type);
        return json;
    }

    public JsonObject toJson() {
        DataUtils.setPfb(null);
        final JsonObject json = new JsonObject();
        json.addProperty("feature", feature);
        json.add("placement", placements);
        return json;
    }
}
