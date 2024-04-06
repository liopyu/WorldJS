package net.liopyu.worldjs.events;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.event.EventResult;
import dev.latvian.mods.kubejs.script.data.DataPackEventJS;
import dev.latvian.mods.kubejs.typings.Generics;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.liopyu.worldjs.api.ICFeatureMethodHolder;
import net.liopyu.worldjs.utils.DataUtils;
import net.liopyu.worldjs.utils.PlacedFeatureBuilder;
import net.liopyu.worldjs.utils.Placement;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import org.jetbrains.annotations.Nullable;

/**
 * Methods for creating configured features found in {@link net.minecraft.world.level.levelgen.feature.Feature}
 * 
 * TODO:
 * {@link net.minecraft.world.level.levelgen.feature.Feature#TREE}
 * {@link net.minecraft.world.level.levelgen.feature.Feature#FLOWER}
 * {@link net.minecraft.world.level.levelgen.feature.Feature#NO_BONEMEAL_FLOWER}
 * {@link net.minecraft.world.level.levelgen.feature.Feature#RANDOM_PATCH}
 * {@link net.minecraft.world.level.levelgen.feature.Feature#BLOCK_PILE}
 * {@link net.minecraft.world.level.levelgen.feature.Feature#SPRING}
 * {@link net.minecraft.world.level.levelgen.feature.Feature#FOSSIL}
 * {@link net.minecraft.world.level.levelgen.feature.Feature#HUGE_RED_MUSHROOM}
 * {@link net.minecraft.world.level.levelgen.feature.Feature#HUGE_BROWN_MUSHROOM}
 * {@link net.minecraft.world.level.levelgen.feature.Feature#BLOCK_COLUMN}
 * {@link net.minecraft.world.level.levelgen.feature.Feature#VEGETATION_PATCH}
 * {@link net.minecraft.world.level.levelgen.feature.Feature#WATERLOGGED_VEGETATION_PATCH}
 * {@link net.minecraft.world.level.levelgen.feature.Feature#ROOT_SYSTEM}
 * {@link net.minecraft.world.level.levelgen.feature.Feature#MULTIFACE_GROWTH}
 * {@link net.minecraft.world.level.levelgen.feature.Feature#UNDERWATER_MAGMA}
 * {@link net.minecraft.world.level.levelgen.feature.Feature#DISK}
 * {@link net.minecraft.world.level.levelgen.feature.Feature#LAKE}
 * {@link net.minecraft.world.level.levelgen.feature.Feature#END_SPIKE}
 * {@link net.minecraft.world.level.levelgen.feature.Feature#SEA_PICKLE}
 * {@link net.minecraft.world.level.levelgen.feature.Feature#SIMPLE_BLOCK}
 * {@link net.minecraft.world.level.levelgen.feature.Feature#HUGE_FUNGUS}
 * {@link net.minecraft.world.level.levelgen.feature.Feature#NETHER_FOREST_VEGETATION}
 * {@link net.minecraft.world.level.levelgen.feature.Feature#RANDOM_SELECTOR}
 * {@link net.minecraft.world.level.levelgen.feature.Feature#SIMPLE_RANDOM_SELECTOR}
 * {@link net.minecraft.world.level.levelgen.feature.Feature#RANDOM_BOOLEAN_SELECTOR}
 * {@link net.minecraft.world.level.levelgen.feature.Feature#GEODE}
 * {@link net.minecraft.world.level.levelgen.feature.Feature#DRIPSTONE_CLUSTER}
 * {@link net.minecraft.world.level.levelgen.feature.Feature#LARGE_DRIPSTONE}
 * {@link net.minecraft.world.level.levelgen.feature.Feature#POINTED_DRIPSTONE}
 *
 * Some things to keep in mind:
 * - It should be fine to use registry objects here
 * - RuleTest has a type wrapper
 * - BlockPos has a type wrapper
 * - IntProvider has a type wrapper
 * - BlockState doesn't have a wrapper for some reason --> Use {@link DataUtils#encodeBlockState(String)}
 * 
 * Need:
 * - Wrapper/binding for
 *      - BlockPredicate
 *      - BlockStateProvider
 *      - FloatProvider (have int & number provider, but not float)
 *      - RuleBasedBlockStateProvider
 */
@SuppressWarnings("unused")
public class JsonDataEventJS extends EventJS {

    @HideFromJS
    public static final String NAME_DESC = "The name of the feature, the namespace will default to 'worldjs' if none is provided";
    @HideFromJS
    public static final String PLACEMENT_DESC = "The placement properties";

    private final DataPackEventJS parent;

    public JsonDataEventJS(DataPackEventJS parent) {
        this.parent = parent;
        DataUtils.setJde(this);
    }

    /**
     * Allows for
     * <pre>{@code
     * WorldJSEvents.worldgenData(event => {
     *     let { tfc, ae2 } = event.mods;
     *     tfc.soilDisc(<blah blah blah>)
     *     ae2.meteor(<blah blah blah>)
     * }
     * }</pre>
     */
    @Generics(String.class)
    public ImmutableMap<String, ICFeatureMethodHolder> getMods() {
        return DataUtils.modConfiguredFeatures.get();
    }

    @HideFromJS
    public void add(ResourceLocation id, JsonElement data) {
        parent.addJson(id, data);
    }

    @HideFromJS
    public void finishFeature(String name, String type, JsonObject config, Placement placement) {
        final JsonObject json = new JsonObject();
        json.addProperty("type", type);
        json.add("config", config);
        finishFeature(name, json, placement);
    }

    @HideFromJS
    public void finishFeature(String name, String type, JsonObject config) {
        final JsonObject json = new JsonObject();
        json.addProperty("type", type);
        json.add("config", config);
        add(DataUtils.configuredFeatureName(name), json);
    }

    @HideFromJS
    public void finishFeature(String name, JsonObject configured, Placement placement) {
        add(DataUtils.configuredFeatureName(name), configured);
        add(DataUtils.placedFeatureName(name), Util.make(new PlacedFeatureBuilder(name), placement).toJson());
    }

    @Info(value = "Creates a configured feature of the given type with the given config and the matching placed feature", params = {
            @Param(name = "name", value = NAME_DESC),
            @Param(name = "type", value = "The type of configured feature to create"),
            @Param(name = "featureConfig", value = "The config json object for the feature"),
            @Param(name = "placement", value = PLACEMENT_DESC)
    })
    public void genericFeature(String name, String type, JsonObject featureConfig, Placement placement) {
        finishFeature(name, type, featureConfig, placement);
    }

    @Info(value = "Creates a configured feature of the given type with the given config and no placed feature", params = {
            @Param(name = "name", value = NAME_DESC),
            @Param(name = "type", value = "The type of configured feature to create"),
            @Param(name = "featureConfig", value = "The config json object for the feature")
    })
    public void genericFeature(String name, String type, JsonObject featureConfig) {
        finishFeature(name, type, featureConfig);
    }

    @Info(value = "Creates a placed feature with the given name which places a configured feature with the same name", params = {
            @Param(name = "name", value = "The name of the placed feature and the ")
    })
    public void placedFeature(String name, Placement placement) {
        placedFeature(name, name, placement);
    }

    public void placedFeature(String name, String configuredFeatureToPlace, Placement placement) {
        add(DataUtils.placedFeatureName(name), Util.make(new PlacedFeatureBuilder(configuredFeatureToPlace), placement).toJson());
    }

    @Info(value = "Creates a configured feature of type `minecraft:no_op`", params = {
            @Param(name = "name", value = NAME_DESC)
    })
    public void noOp(String name) {
        final JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:no_op");
        json.add("config", new JsonObject());
        add(DataUtils.configuredFeatureName(name), json);
    }

    @Info(value = "Creates a configured feature of type `minecraft:chorus_plant` and the matching placed feature", params = {
            @Param(name = "name", value = NAME_DESC),
            @Param(name = "placement", value = PLACEMENT_DESC)
    })
    public void chorusPlant(String name, Placement placement) {
        finishFeature(name, "chorus_plant", new JsonObject(), placement);
    }

    @Info(value = "Creates a configured feature of type `minecraft:chorus_plant`", params = {
            @Param(name = "name", value = NAME_DESC)
    })
    public void chorusPlant(String name) {
        finishFeature(name, "chorus_plant", new JsonObject());
    }

    @Info(value = "Creates a configured feature of type `minecraft:void_start_platform` and the matching placed feature", params = {
            @Param(name = "name", value = NAME_DESC),
            @Param(name = "placement", value = PLACEMENT_DESC)
    })
    public void voidStartPlatform(String name, Placement placement) {
        finishFeature(name, "void_start_platform", new JsonObject(), placement);
    }

    @Info(value = "Creates a configured feature of type `minecraft:void_start_platform`", params = {
            @Param(name = "name", value = NAME_DESC)
    })
    public void voidStartPlatform(String name) {
        finishFeature(name, "void_start_platform", new JsonObject());
    }

    @Info(value = "Creates a configured feature of type `minecraft:desert_well` and the matching placed feature", params = {
            @Param(name = "name", value = NAME_DESC),
            @Param(name = "placement", value = PLACEMENT_DESC)
    })
    public void desertWell(String name, Placement placement) {
        finishFeature(name, "desert_well", new JsonObject(), placement);
    }

    @Info(value = "Creates a configured feature of type `minecraft:desert_well`", params = {
        @Param(name = "name", value = NAME_DESC)
    })
    public void desertWell(String name) {
        finishFeature(name, "desert_well", new JsonObject());
    }

    @Info(value = "Creates a configured feature of type `minecraft:ice_spike` and the matching placed feature", params = {
            @Param(name = "name", value = NAME_DESC),
            @Param(name = "placement", value = PLACEMENT_DESC)
    })
    public void iceSpike(String name, Placement placement) {
        finishFeature(name, "ice_spike", new JsonObject(), placement);
    }

    @Info(value = "Creates a configured feature of type `minecraft:ice_spike`", params = {
            @Param(name = "name", value = NAME_DESC)
    })
    public void iceSpike(String name) {
        finishFeature(name, "ice_spike", new JsonObject());
    }

    @Info(value = "Creates a configured feature of type `minecraft:glowstone_blob` and the matching placed feature", params = {
            @Param(name = "name", value = NAME_DESC),
            @Param(name = "placement", value = PLACEMENT_DESC)
    })
    public void glowstoneBlob(String name, Placement placement) {
        finishFeature(name, "glowstone_blob", new JsonObject(), placement);
    }

    @Info(value = "Creates a configured feature of type `minecraft:glowstone_blob`", params = {
            @Param(name = "name", value = NAME_DESC)
    })
    public void glowstoneBlob(String name) {
        finishFeature(name, "glowstone_blob", new JsonObject());
    }

    @Info(value = "Creates a configured feature of type `minecraft:freeze_top_layer` and the matching placed feature", params = {
            @Param(name = "name", value = NAME_DESC),
            @Param(name = "placement", value = PLACEMENT_DESC)
    })
    public void freezeTopLayer(String name, Placement placement) {
        finishFeature(name, "freeze_top_layer", new JsonObject(), placement);
    }

    @Info(value = "Creates a configured feature of type `minecraft:freeze_top_layer`", params = {
            @Param(name = "name", value = NAME_DESC)
    })
    public void freezeTopLayer(String name) {
        finishFeature(name, "free_top_layer", new JsonObject());
    }

    @Info(value = "Creates a configured feature of type `minecraft:vines` and the matching placed feature", params = {
            @Param(name = "name", value = NAME_DESC),
            @Param(name = "placement", value = PLACEMENT_DESC)
    })
    public void vines(String name, Placement placement) {
        finishFeature(name, "vines", new JsonObject(), placement);
    }

    @Info(value = "Creates a configured feature of type `minecraft:vines`", params = {
            @Param(name = "name", value = NAME_DESC)
    })
    public void vines(String name) {
        finishFeature(name, "vines", new JsonObject());
    }

    @Info(value = "Creates a configured feature of type `minecraft:monster_room` and the matching placed feature", params = {
            @Param(name = "name", value = NAME_DESC),
            @Param(name = "placement", value = PLACEMENT_DESC)
    })
    public void monsterRoom(String name, Placement placement) {
        finishFeature(name, "monster_room", new JsonObject(), placement);
    }

    @Info(value = "Creates a configured feature of type `minecraft:monster_room`", params = {
            @Param(name = "name", value = NAME_DESC)
    })
    public void monsterRoom(String name) {
        finishFeature(name, "monster_room", new JsonObject());
    }

    @Info(value = "Creates a configured feature of type `minecraft:blue_ice` and the matching placed feature", params = {
            @Param(name = "name", value = NAME_DESC),
            @Param(name = "placement", value = PLACEMENT_DESC)
    })
    public void blueIce(String name, Placement placement) {
        finishFeature(name, "blue_ice", new JsonObject(), placement);
    }

    @Info(value = "Creates a configured feature of type `minecraft:blue_ice`", params = {
            @Param(name = "name", value = NAME_DESC)
    })
    public void blueIce(String name) {
        finishFeature(name, "blue_ice", new JsonObject());
    }

    @Info(value = "Creates a configured feature of type `minecraft:end_island` nad the matching placed feature", params = {
            @Param(name = "name", value = NAME_DESC),
            @Param(name = "placement", value = PLACEMENT_DESC)
    })
    public void endIsland(String name, Placement placement) {
        finishFeature(name, "end_island", new JsonObject(), placement);
    }

    @Info(value = "Creates a new configured feature of type `minecraft:end_island`", params = {
            @Param(name = "name", value = NAME_DESC)
    })
    public void endIsland(String name) {
        finishFeature(name, "end_island", new JsonObject());
    }

    @Info(value = "Creates a new configured feature of type `minecraft:kelp` and the matching placed feature", params = {
            @Param(name = "name", value = NAME_DESC),
            @Param(name = "placement", value = PLACEMENT_DESC)
    })
    public void kelp(String name, Placement placement) {
        finishFeature(name, "kelp", new JsonObject(), placement);
    }

    @Info(value = "Creates a new configured feature of type `minecraft:kelp`", params = {
            @Param(name = "name", value = NAME_DESC)
    })
    public void kelp(String name) {
        finishFeature(name, "kelp", new JsonObject());
    }

    @Info(value = "Creates a new configured feature of type `minecraft:coral_tree` and the matching placed feature", params = {
            @Param(name = "name", value = NAME_DESC),
            @Param(name = "placement", value = PLACEMENT_DESC)
    })
    public void coralTree(String name, Placement placement) {
        finishFeature(name, "coral_tree", new JsonObject(), placement);
    }

    @Info(value = "Creates a new configured feature of type `minecraft:coral_tree`", params = {
            @Param(name = "name", value = NAME_DESC)
    })
    public void coralTree(String name) {
        finishFeature(name, "coral_tree", new JsonObject());
    }

    @Info(value = "Creates a new configured feature of type `minecraft:coral_mushroom` and the matching placed feature", params = {
            @Param(name = "name", value = NAME_DESC),
            @Param(name = "placement", value = PLACEMENT_DESC)
    })
    public void coralMushroom(String name, Placement placement) {
        finishFeature(name, "coral_mushroom", new JsonObject(), placement);
    }

    @Info(value = "Creates a new configured feature of type `minecraft:coral_mushroom`", params = {
            @Param(name = "name", value = NAME_DESC)
    })
    public void coralMushroom(String name) {
        finishFeature(name, "coral_mushroom", new JsonObject());
    }

    @Info(value = "Creates a new configured feature of type `minecraft:coral_claw` and the matching placed feature", params = {
            @Param(name = "name", value = NAME_DESC),
            @Param(name = "placement", value = PLACEMENT_DESC)
    })
    public void coralClaw(String name, Placement placement) {
        finishFeature(name, "coral_claw", new JsonObject(), placement);
    }

    @Info(value = "Creates a new configured feature of type `minecraft:coral_claw`", params = {
            @Param(name = "name", value = NAME_DESC)
    })
    public void coralClaw(String name) {
        finishFeature(name, "coral_claw", new JsonObject());
    }

    @Info(value = "Creates a new configured feature of type `minecraft:weeping_vines` and the matching placed feature", params = {
            @Param(name = "name", value = NAME_DESC),
            @Param(name = "placement", value = PLACEMENT_DESC)
    })
    public void weepingVines(String name, Placement placement) {
        finishFeature(name, "weeping_vines", new JsonObject(), placement);
    }

    @Info(value = "Creates a new configured feature of type `minecraft:weeping_vines`", params = {
            @Param(name = "name", value = NAME_DESC)
    })
    public void weepingVines(String name) {
        finishFeature(name, "weeping_vines", new JsonObject());
    }

    @Info(value = "Creates a new configured feature of type `minecraft:bonus_chest` and the matching placed feature", params = {
            @Param(name = "name", value = NAME_DESC),
            @Param(name = "placement", value = PLACEMENT_DESC)
    })
    public void bonusChest(String name, Placement placement) {
        finishFeature(name, "bonus_chest", new JsonObject(), placement);
    }

    @Info(value = "Creates a new configured feature of type `minecraft:bonus_chest`", params = {
            @Param(name = "name", value = NAME_DESC)
    })
    public void bonusChest(String name) {
        finishFeature(name, "bonus_chest", new JsonObject());
    }

    // TODO: Everything beyond this point is undocumented
    @Info(value = "Creates a new configured feature of type `minecraft:basalt_pillar` and the matching placed feature")
    public void basaltPillar(String name, Placement placement) {
        finishFeature(name, "basalt_pillar", new JsonObject(), placement);
    }

    @Info(value = "Creates a new configured feature of type `minecraft:basalt_pillar`", params = {
            @Param(name = "name", value = NAME_DESC)
    })
    public void basaltPillar(String name) {
        finishFeature(name, "basalt_pillar", new JsonObject());
    }

    public void sculkPatch(String name, int chargeCount, int amountPerCharge, int spreadAttempts, int growthRounds, int spreadRounds, IntProvider extraRareGrowths, float catalystChance, Placement placement) {
        sculkPatch(name, chargeCount, amountPerCharge, spreadAttempts, growthRounds, spreadRounds, extraRareGrowths, catalystChance);
        placedFeature(name, placement);
    }

    public void sculkPatch(String name, int chargeCount, int amountPerCharge, int spreadAttempts, int growthRounds, int spreadRounds, IntProvider extraRareGrowths, float catalystChance) {
        final JsonObject config = new JsonObject();
        config.addProperty("charge_count", chargeCount);
        config.addProperty("amount_per_charge", amountPerCharge);
        config.addProperty("spread_attempts", spreadAttempts);
        config.addProperty("growth_rounds", growthRounds);
        config.addProperty("spread_rounds", spreadAttempts);
        config.add("extra_rare_growths", DataUtils.encodeIntProvider(extraRareGrowths));
        config.addProperty("spread_rounds", spreadAttempts);
        finishFeature(name, "sculk_patch", config);
    }

    // Mention WorldJSBindings#targetBlockState()
    public void replaceSingleBlock(String name, OreConfiguration.TargetBlockState[] blockTargets, Placement placement) {
        replaceSingleBlock(name, blockTargets);
        placedFeature(name, placement);
    }

    public void replaceSingleBlock(String name, OreConfiguration.TargetBlockState[] blockTargets) {
        final JsonObject config = new JsonObject();
        config.add("targets", DataUtils.encodeTargetBlockStateArray(blockTargets));
        finishFeature(name, "replace_single_block", config);
    }

    public void ore(String name, OreConfiguration.TargetBlockState[] blockTargets, int size, float discardOnAirChance, boolean scattered, Placement placement) {
        ore(name, blockTargets, size, discardOnAirChance, scattered);
        placedFeature(name, placement);
    }

    public void ore(String name, OreConfiguration.TargetBlockState[] blockTargets, int size, float discardOnAirChance, boolean scattered) {
        final JsonObject config = new JsonObject();
        config.add("targets", DataUtils.encodeTargetBlockStateArray(blockTargets));
        config.addProperty("size", size);
        config.addProperty("discard_chance_on_air_exposure", discardOnAirChance);
        finishFeature(name, scattered ? "scattered_ore" : "ore", config);
    }
    
    public void fillLayer(String name, int height, String blockState, Placement placement) {
        fillLayer(name, height, blockState);
        placedFeature(name, placement);
    }
    
    public void fillLayer(String name, int height, String blockState) {
        final JsonObject config = new JsonObject();
        config.addProperty("height", height);
        config.add("state", DataUtils.encodeBlockState(blockState));
        finishFeature(name, "fill_layer", config);
    }
    
    public void netherrackReplaceBlobs(String name, String targetState, String replaceState, IntProvider radius, Placement placement) {
        netherrackReplaceBlobs(name, targetState, replaceState, radius);
        placedFeature(name, placement);
    }
    
    public void netherrackReplaceBlobs(String name, String targetState, String replaceState, IntProvider radius) {
        final JsonObject config = new JsonObject();
        config.add("target", DataUtils.encodeBlockState(targetState));
        config.add("state", DataUtils.encodeBlockState(replaceState));
        config.add("radius", DataUtils.encodeIntProvider(radius));
        finishFeature(name, "netherrack_replace_blobs", config);
    }
    
    public void deltaFeature(String name, String contentsState, String rimState, IntProvider size, IntProvider rimSize, Placement placement) {
        deltaFeature(name, contentsState, rimState, size, rimSize);
        placedFeature(name, placement);
    }
    
    public void deltaFeature(String name, String contentsState, String rimState, IntProvider size, IntProvider rimSize) {
        final JsonObject config = new JsonObject();
        config.add("contents", DataUtils.encodeBlockState(contentsState));
        config.add("rim", DataUtils.encodeBlockState(rimState));
        config.add("size", DataUtils.encodeIntProvider(size));
        config.add("rimSize", DataUtils.encodeIntProvider(rimSize));
        finishFeature(name, "delta_feature", config);
    }
    
    public void basaltColumns(String name, IntProvider reach, IntProvider height, Placement placement) {
        basaltColumns(name, reach, height);
        placedFeature(name, placement);
    }
    
    public void basaltColumns(String name, IntProvider reach, IntProvider height) {
        final JsonObject config = new JsonObject();
        config.add("reach", DataUtils.encodeIntProvider(reach));
        config.add("height", DataUtils.encodeIntProvider(height));
        finishFeature(name, "basalt_columns", config);
    }
    
    public void twistingVines(String name, int spreadWidth, int spreadHeight, int maxHeight, Placement placement) {
        twistingVines(name, spreadWidth, spreadHeight, maxHeight);
        placedFeature(name, placement);
    }
    
    public void twistingVines(String name, int spreadWidth, int spreadHeight, int maxHeight) {
        final JsonObject config = new JsonObject();
        config.addProperty("spread_width", spreadWidth);
        config.addProperty("spread_height", spreadHeight);
        config.addProperty("max_height", maxHeight);
        finishFeature(name, "twisting_vines", config);
    }
    
    public void forestRock(String name, String state, Placement placement) {
        forestRock(name, state);
        placedFeature(name, placement);
    }
    
    public void forestRock(String name, String state) {
        final JsonObject config = new JsonObject();
        config.add("state", DataUtils.encodeBlockState(state));
        finishFeature(name, "forest_rock", config);
    }
    
    public void iceberg(String name, String state, Placement placement) {
        iceberg(name, state);
        placedFeature(name, placement);
    }
    
    public void iceberg(String name, String state) {
        final JsonObject config = new JsonObject();
        config.add("state", DataUtils.encodeBlockState(state));
        finishFeature(name, "iceberg", config);
    }
    
    public void endGateway(String name, @Nullable BlockPos exit, boolean exact, Placement placement) {
        endGateway(name, exit, exact);
        placedFeature(name, placement);
    }
    
    public void endGateway(String name, @Nullable BlockPos exit, boolean exact) {
        final JsonObject config = new JsonObject();
        if (exit != null) {
            config.add("exit", DataUtils.encodeBlockPos(exit));
        }
        config.addProperty("exact", exact);
        finishFeature(name, "end_gateway", config);
    }
    
    public void seaGrass(String name, float probability, Placement placement) {
        seaGrass(name, probability);
        placedFeature(name, placement);
    }
    
    public void seaGrass(String name, float probability) {
        final JsonObject config = new JsonObject();
        config.addProperty("probability", probability);
        finishFeature(name, "seagrass", config);
    }
    
    public void bamboo(String name, float probability, Placement placement) {
        bamboo(name, probability);
        placedFeature(name, placement);
    }
    
    public void bamboo(String name, float probability) {
        final JsonObject config = new JsonObject();
        config.addProperty("probability", probability);
        finishFeature(name, "bamboo", config);
    }

    @Override
    protected void afterPosted(EventResult result) {
        super.afterPosted(result);
        DataUtils.setJde(null);
    }
}
