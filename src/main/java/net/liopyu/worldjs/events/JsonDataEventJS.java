package net.liopyu.worldjs.events;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.event.EventResult;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.data.DataPackEventJS;
import dev.latvian.mods.kubejs.typings.Generics;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.liopyu.worldjs.api.ICFeatureMethodHolder;
import net.liopyu.worldjs.utils.Builders;
import net.liopyu.worldjs.utils.DataUtils;
import net.liopyu.worldjs.utils.PlacedFeatureBuilder;
import net.liopyu.worldjs.utils.Placement;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.configurations.BlockColumnConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RuleBasedBlockStateProvider;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Methods for creating configured features found in {@link net.minecraft.world.level.levelgen.feature.Feature}
 * 
 * TODO:
 * {@link net.minecraft.world.level.levelgen.feature.Feature#TREE}
 * {@link net.minecraft.world.level.levelgen.feature.Feature#FOSSIL}
 * {@link net.minecraft.world.level.levelgen.feature.Feature#MULTIFACE_GROWTH}
 * {@link net.minecraft.world.level.levelgen.feature.Feature#RANDOM_SELECTOR}
 *
 * Some things to keep in mind:
 * - It should be fine to use registry objects here
 * - RuleTest has a type wrapper
 * - BlockPos has a type wrapper
 * - IntProvider has a type wrapper
 * - BlockState, BlockPredicate, FloatProvider, BlockStateProvider, FluidState have wrappers provided by us
 * - OreConfiguration$TargetBlockState, RuleBasedBlockStateProvider, BlockColumnConfiguration$Layer can be created via the WorldJS binding
 *
 * Need:
 * - StructureProcessor wrapper for fossils
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

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration#CODEC}
     */
    @Info(value = "Creates a configured feature of type `minecraft:no_op`", params = {
            @Param(name = "name", value = NAME_DESC)
    })
    public void noOp(String name) {
        final JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:no_op");
        json.add("config", new JsonObject());
        add(DataUtils.configuredFeatureName(name), json);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration#CODEC}
     */
    @Info(value = "Creates a configured feature of type `minecraft:chorus_plant` and the matching placed feature", params = {
            @Param(name = "name", value = NAME_DESC),
            @Param(name = "placement", value = PLACEMENT_DESC)
    })
    public void chorusPlant(String name, Placement placement) {
        finishFeature(name, "chorus_plant", new JsonObject(), placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration#CODEC}
     */
    @Info(value = "Creates a configured feature of type `minecraft:chorus_plant`", params = {
            @Param(name = "name", value = NAME_DESC)
    })
    public void chorusPlant(String name) {
        finishFeature(name, "chorus_plant", new JsonObject());
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration#CODEC}
     */
    @Info(value = "Creates a configured feature of type `minecraft:void_start_platform` and the matching placed feature", params = {
            @Param(name = "name", value = NAME_DESC),
            @Param(name = "placement", value = PLACEMENT_DESC)
    })
    public void voidStartPlatform(String name, Placement placement) {
        finishFeature(name, "void_start_platform", new JsonObject(), placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration#CODEC}
     */
    @Info(value = "Creates a configured feature of type `minecraft:void_start_platform`", params = {
            @Param(name = "name", value = NAME_DESC)
    })
    public void voidStartPlatform(String name) {
        finishFeature(name, "void_start_platform", new JsonObject());
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration#CODEC}
     */
    @Info(value = "Creates a configured feature of type `minecraft:desert_well` and the matching placed feature", params = {
            @Param(name = "name", value = NAME_DESC),
            @Param(name = "placement", value = PLACEMENT_DESC)
    })
    public void desertWell(String name, Placement placement) {
        finishFeature(name, "desert_well", new JsonObject(), placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration#CODEC}
     */
    @Info(value = "Creates a configured feature of type `minecraft:desert_well`", params = {
        @Param(name = "name", value = NAME_DESC)
    })
    public void desertWell(String name) {
        finishFeature(name, "desert_well", new JsonObject());
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration#CODEC}
     */
    @Info(value = "Creates a configured feature of type `minecraft:ice_spike` and the matching placed feature", params = {
            @Param(name = "name", value = NAME_DESC),
            @Param(name = "placement", value = PLACEMENT_DESC)
    })
    public void iceSpike(String name, Placement placement) {
        finishFeature(name, "ice_spike", new JsonObject(), placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration#CODEC}
     */
    @Info(value = "Creates a configured feature of type `minecraft:ice_spike`", params = {
            @Param(name = "name", value = NAME_DESC)
    })
    public void iceSpike(String name) {
        finishFeature(name, "ice_spike", new JsonObject());
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration#CODEC}
     */
    @Info(value = "Creates a configured feature of type `minecraft:glowstone_blob` and the matching placed feature", params = {
            @Param(name = "name", value = NAME_DESC),
            @Param(name = "placement", value = PLACEMENT_DESC)
    })
    public void glowstoneBlob(String name, Placement placement) {
        finishFeature(name, "glowstone_blob", new JsonObject(), placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration#CODEC}
     */
    @Info(value = "Creates a configured feature of type `minecraft:glowstone_blob`", params = {
            @Param(name = "name", value = NAME_DESC)
    })
    public void glowstoneBlob(String name) {
        finishFeature(name, "glowstone_blob", new JsonObject());
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration#CODEC}
     */
    @Info(value = "Creates a configured feature of type `minecraft:freeze_top_layer` and the matching placed feature", params = {
            @Param(name = "name", value = NAME_DESC),
            @Param(name = "placement", value = PLACEMENT_DESC)
    })
    public void freezeTopLayer(String name, Placement placement) {
        finishFeature(name, "freeze_top_layer", new JsonObject(), placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration#CODEC}
     */
    @Info(value = "Creates a configured feature of type `minecraft:freeze_top_layer`", params = {
            @Param(name = "name", value = NAME_DESC)
    })
    public void freezeTopLayer(String name) {
        finishFeature(name, "free_top_layer", new JsonObject());
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration#CODEC}
     */
    @Info(value = "Creates a configured feature of type `minecraft:vines` and the matching placed feature", params = {
            @Param(name = "name", value = NAME_DESC),
            @Param(name = "placement", value = PLACEMENT_DESC)
    })
    public void vines(String name, Placement placement) {
        finishFeature(name, "vines", new JsonObject(), placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration#CODEC}
     */
    @Info(value = "Creates a configured feature of type `minecraft:vines`", params = {
            @Param(name = "name", value = NAME_DESC)
    })
    public void vines(String name) {
        finishFeature(name, "vines", new JsonObject());
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration#CODEC}
     */
    @Info(value = "Creates a configured feature of type `minecraft:monster_room` and the matching placed feature", params = {
            @Param(name = "name", value = NAME_DESC),
            @Param(name = "placement", value = PLACEMENT_DESC)
    })
    public void monsterRoom(String name, Placement placement) {
        finishFeature(name, "monster_room", new JsonObject(), placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration#CODEC}
     */
    @Info(value = "Creates a configured feature of type `minecraft:monster_room`", params = {
            @Param(name = "name", value = NAME_DESC)
    })
    public void monsterRoom(String name) {
        finishFeature(name, "monster_room", new JsonObject());
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration#CODEC}
     */
    @Info(value = "Creates a configured feature of type `minecraft:blue_ice` and the matching placed feature", params = {
            @Param(name = "name", value = NAME_DESC),
            @Param(name = "placement", value = PLACEMENT_DESC)
    })
    public void blueIce(String name, Placement placement) {
        finishFeature(name, "blue_ice", new JsonObject(), placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration#CODEC}
     */
    @Info(value = "Creates a configured feature of type `minecraft:blue_ice`", params = {
            @Param(name = "name", value = NAME_DESC)
    })
    public void blueIce(String name) {
        finishFeature(name, "blue_ice", new JsonObject());
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration#CODEC}
     */
    @Info(value = "Creates a configured feature of type `minecraft:end_island` nad the matching placed feature", params = {
            @Param(name = "name", value = NAME_DESC),
            @Param(name = "placement", value = PLACEMENT_DESC)
    })
    public void endIsland(String name, Placement placement) {
        finishFeature(name, "end_island", new JsonObject(), placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration#CODEC}
     */
    @Info(value = "Creates a new configured feature of type `minecraft:end_island`", params = {
            @Param(name = "name", value = NAME_DESC)
    })
    public void endIsland(String name) {
        finishFeature(name, "end_island", new JsonObject());
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration#CODEC}
     */
    @Info(value = "Creates a new configured feature of type `minecraft:kelp` and the matching placed feature", params = {
            @Param(name = "name", value = NAME_DESC),
            @Param(name = "placement", value = PLACEMENT_DESC)
    })
    public void kelp(String name, Placement placement) {
        finishFeature(name, "kelp", new JsonObject(), placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration#CODEC}
     */
    @Info(value = "Creates a new configured feature of type `minecraft:kelp`", params = {
            @Param(name = "name", value = NAME_DESC)
    })
    public void kelp(String name) {
        finishFeature(name, "kelp", new JsonObject());
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration#CODEC}
     */
    @Info(value = "Creates a new configured feature of type `minecraft:coral_tree` and the matching placed feature", params = {
            @Param(name = "name", value = NAME_DESC),
            @Param(name = "placement", value = PLACEMENT_DESC)
    })
    public void coralTree(String name, Placement placement) {
        finishFeature(name, "coral_tree", new JsonObject(), placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration#CODEC}
     */
    @Info(value = "Creates a new configured feature of type `minecraft:coral_tree`", params = {
            @Param(name = "name", value = NAME_DESC)
    })
    public void coralTree(String name) {
        finishFeature(name, "coral_tree", new JsonObject());
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration#CODEC}
     */
    @Info(value = "Creates a new configured feature of type `minecraft:coral_mushroom` and the matching placed feature", params = {
            @Param(name = "name", value = NAME_DESC),
            @Param(name = "placement", value = PLACEMENT_DESC)
    })
    public void coralMushroom(String name, Placement placement) {
        finishFeature(name, "coral_mushroom", new JsonObject(), placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration#CODEC}
     */
    @Info(value = "Creates a new configured feature of type `minecraft:coral_mushroom`", params = {
            @Param(name = "name", value = NAME_DESC)
    })
    public void coralMushroom(String name) {
        finishFeature(name, "coral_mushroom", new JsonObject());
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration#CODEC}
     */
    @Info(value = "Creates a new configured feature of type `minecraft:coral_claw` and the matching placed feature", params = {
            @Param(name = "name", value = NAME_DESC),
            @Param(name = "placement", value = PLACEMENT_DESC)
    })
    public void coralClaw(String name, Placement placement) {
        finishFeature(name, "coral_claw", new JsonObject(), placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration#CODEC}
     */
    @Info(value = "Creates a new configured feature of type `minecraft:coral_claw`", params = {
            @Param(name = "name", value = NAME_DESC)
    })
    public void coralClaw(String name) {
        finishFeature(name, "coral_claw", new JsonObject());
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration#CODEC}
     */
    @Info(value = "Creates a new configured feature of type `minecraft:weeping_vines` and the matching placed feature", params = {
            @Param(name = "name", value = NAME_DESC),
            @Param(name = "placement", value = PLACEMENT_DESC)
    })
    public void weepingVines(String name, Placement placement) {
        finishFeature(name, "weeping_vines", new JsonObject(), placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration#CODEC}
     */
    @Info(value = "Creates a new configured feature of type `minecraft:weeping_vines`", params = {
            @Param(name = "name", value = NAME_DESC)
    })
    public void weepingVines(String name) {
        finishFeature(name, "weeping_vines", new JsonObject());
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration#CODEC}
     */
    @Info(value = "Creates a new configured feature of type `minecraft:bonus_chest` and the matching placed feature", params = {
            @Param(name = "name", value = NAME_DESC),
            @Param(name = "placement", value = PLACEMENT_DESC)
    })
    public void bonusChest(String name, Placement placement) {
        finishFeature(name, "bonus_chest", new JsonObject(), placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration#CODEC}
     */
    @Info(value = "Creates a new configured feature of type `minecraft:bonus_chest`", params = {
            @Param(name = "name", value = NAME_DESC)
    })
    public void bonusChest(String name) {
        finishFeature(name, "bonus_chest", new JsonObject());
    }

    // TODO: Everything beyond this point is undocumented
    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration#CODEC}
     */
    @Info(value = "Creates a new configured feature of type `minecraft:basalt_pillar` and the matching placed feature")
    public void basaltPillar(String name, Placement placement) {
        finishFeature(name, "basalt_pillar", new JsonObject(), placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration#CODEC}
     */
    @Info(value = "Creates a new configured feature of type `minecraft:basalt_pillar`", params = {
            @Param(name = "name", value = NAME_DESC)
    })
    public void basaltPillar(String name) {
        finishFeature(name, "basalt_pillar", new JsonObject());
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.SculkPatchConfiguration#CODEC}
     */
    public void sculkPatch(String name, int chargeCount, int amountPerCharge, int spreadAttempts, int growthRounds, int spreadRounds, IntProvider extraRareGrowths, float catalystChance, Placement placement) {
        sculkPatch(name, chargeCount, amountPerCharge, spreadAttempts, growthRounds, spreadRounds, extraRareGrowths, catalystChance);
        placedFeature(name, placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.SculkPatchConfiguration#CODEC}
     */
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

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.ReplaceBlockConfiguration#CODEC}
     */
    public void replaceSingleBlock(String name, OreConfiguration.TargetBlockState[] blockTargets, Placement placement) {
        replaceSingleBlock(name, blockTargets);
        placedFeature(name, placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.ReplaceBlockConfiguration#CODEC}
     */
    public void replaceSingleBlock(String name, OreConfiguration.TargetBlockState[] blockTargets) {
        final JsonObject config = new JsonObject();
        config.add("targets", DataUtils.encodeTargetBlockStateArray(blockTargets));
        finishFeature(name, "replace_single_block", config);
    }

    /**
     * {@link OreConfiguration#CODEC}
     */
    public void ore(String name, OreConfiguration.TargetBlockState[] blockTargets, int size, float discardOnAirChance, boolean scattered, Placement placement) {
        ore(name, blockTargets, size, discardOnAirChance, scattered);
        placedFeature(name, placement);
    }

    /**
     * {@link OreConfiguration#CODEC}
     */
    public void ore(String name, OreConfiguration.TargetBlockState[] blockTargets, int size, float discardOnAirChance, boolean scattered) {
        final JsonObject config = new JsonObject();
        config.add("targets", DataUtils.encodeTargetBlockStateArray(blockTargets));
        config.addProperty("size", size);
        config.addProperty("discard_chance_on_air_exposure", discardOnAirChance);
        finishFeature(name, scattered ? "scattered_ore" : "ore", config);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.LayerConfiguration#CODEC}
     */
    public void fillLayer(String name, int height, BlockState blockState, Placement placement) {
        fillLayer(name, height, blockState);
        placedFeature(name, placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.LayerConfiguration#CODEC}
     */
    public void fillLayer(String name, int height, BlockState blockState) {
        final JsonObject config = new JsonObject();
        config.addProperty("height", height);
        config.add("state", DataUtils.encodeBlockState(blockState));
        finishFeature(name, "fill_layer", config);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.ReplaceSphereConfiguration#CODEC}
     */
    public void netherrackReplaceBlobs(String name, BlockState targetState, BlockState replaceState, IntProvider radius, Placement placement) {
        netherrackReplaceBlobs(name, targetState, replaceState, radius);
        placedFeature(name, placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.ReplaceSphereConfiguration#CODEC}
     */
    public void netherrackReplaceBlobs(String name, BlockState targetState, BlockState replaceState, IntProvider radius) {
        final JsonObject config = new JsonObject();
        config.add("target", DataUtils.encodeBlockState(targetState));
        config.add("state", DataUtils.encodeBlockState(replaceState));
        config.add("radius", DataUtils.encodeIntProvider(radius));
        finishFeature(name, "netherrack_replace_blobs", config);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.DeltaFeatureConfiguration#CODEC}
     */
    public void deltaFeature(String name, BlockState contentsState, BlockState rimState, IntProvider size, IntProvider rimSize, Placement placement) {
        deltaFeature(name, contentsState, rimState, size, rimSize);
        placedFeature(name, placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.DeltaFeatureConfiguration#CODEC}
     */
    public void deltaFeature(String name, BlockState contentsState, BlockState rimState, IntProvider size, IntProvider rimSize) {
        final JsonObject config = new JsonObject();
        config.add("contents", DataUtils.encodeBlockState(contentsState));
        config.add("rim", DataUtils.encodeBlockState(rimState));
        config.add("size", DataUtils.encodeIntProvider(size));
        config.add("rimSize", DataUtils.encodeIntProvider(rimSize));
        finishFeature(name, "delta_feature", config);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.ColumnFeatureConfiguration#CODEC}
     */
    public void basaltColumns(String name, IntProvider reach, IntProvider height, Placement placement) {
        basaltColumns(name, reach, height);
        placedFeature(name, placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.ColumnFeatureConfiguration#CODEC}
     */
    public void basaltColumns(String name, IntProvider reach, IntProvider height) {
        final JsonObject config = new JsonObject();
        config.add("reach", DataUtils.encodeIntProvider(reach));
        config.add("height", DataUtils.encodeIntProvider(height));
        finishFeature(name, "basalt_columns", config);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.TwistingVinesConfig#CODEC}
     */
    public void twistingVines(String name, int spreadWidth, int spreadHeight, int maxHeight, Placement placement) {
        twistingVines(name, spreadWidth, spreadHeight, maxHeight);
        placedFeature(name, placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.TwistingVinesConfig#CODEC}
     */
    public void twistingVines(String name, int spreadWidth, int spreadHeight, int maxHeight) {
        final JsonObject config = new JsonObject();
        config.addProperty("spread_width", spreadWidth);
        config.addProperty("spread_height", spreadHeight);
        config.addProperty("max_height", maxHeight);
        finishFeature(name, "twisting_vines", config);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration#CODEC}
     */
    public void forestRock(String name, BlockState state, Placement placement) {
        forestRock(name, state);
        placedFeature(name, placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration#CODEC}
     */
    public void forestRock(String name, BlockState state) {
        final JsonObject config = new JsonObject();
        config.add("state", DataUtils.encodeBlockState(state));
        finishFeature(name, "forest_rock", config);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration#CODEC}
     */
    public void iceberg(String name, BlockState state, Placement placement) {
        iceberg(name, state);
        placedFeature(name, placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration#CODEC}
     */
    public void iceberg(String name, BlockState state) {
        final JsonObject config = new JsonObject();
        config.add("state", DataUtils.encodeBlockState(state));
        finishFeature(name, "iceberg", config);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.EndGatewayConfiguration#CODEC}
     */
    public void endGateway(String name, @Nullable BlockPos exit, boolean exact, Placement placement) {
        endGateway(name, exit, exact);
        placedFeature(name, placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.EndGatewayConfiguration#CODEC}
     */
    public void endGateway(String name, @Nullable BlockPos exit, boolean exact) {
        final JsonObject config = new JsonObject();
        if (exit != null) {
            config.add("exit", DataUtils.encodeBlockPos(exit));
        }
        config.addProperty("exact", exact);
        finishFeature(name, "end_gateway", config);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration#CODEC}
     */
    public void seaGrass(String name, float probability, Placement placement) {
        seaGrass(name, probability);
        placedFeature(name, placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration#CODEC}
     */
    public void seaGrass(String name, float probability) {
        final JsonObject config = new JsonObject();
        config.addProperty("probability", probability);
        finishFeature(name, "seagrass", config);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration#CODEC}
     */
    public void bamboo(String name, float probability, Placement placement) {
        bamboo(name, probability);
        placedFeature(name, placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration#CODEC}
     */
    public void bamboo(String name, float probability) {
        final JsonObject config = new JsonObject();
        config.addProperty("probability", probability);
        finishFeature(name, "bamboo", config);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration#CODEC}
     */
    public void flower(String name, @Nullable Integer tries, @Nullable Integer xzSpread, @Nullable Integer ySpread, String placedFeature, Placement placement) {
        flower(name, tries, xzSpread, ySpread, placedFeature);
        placedFeature(name, placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration#CODEC}
     */
    public void flower(String name, @Nullable Integer tries, @Nullable Integer xzSpread, @Nullable Integer ySpread, String placedFeature) {
        finishFeature(name, "flower", DataUtils.randomPatchConfig(tries, xzSpread, ySpread, placedFeature));
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration#CODEC}
     */
    public void noBonemealFlower(String name, @Nullable Integer tries, @Nullable Integer xzSpread, @Nullable Integer ySpread, String placedFeature, Placement placement) {
        noBonemealFlower(name, tries, xzSpread, ySpread, placedFeature);
        placedFeature(name, placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration#CODEC}
     */
    public void noBonemealFlower(String name, @Nullable Integer tries, @Nullable Integer xzSpread, @Nullable Integer ySpread, String placedFeature) {
        finishFeature(name, "no_bonemeal_flower", DataUtils.randomPatchConfig(tries, xzSpread, ySpread, placedFeature));
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration#CODEC}
     */
    public void randomPatch(String name, @Nullable Integer tries, @Nullable Integer xzSpread, @Nullable Integer ySpread, String placedFeature, Placement placement) {
        randomPatch(name, tries, xzSpread, ySpread, placedFeature);
        placedFeature(name, placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration#CODEC}
     */
    public void randomPatch(String name, @Nullable Integer tries, @Nullable Integer xzSpread, @Nullable Integer ySpread, String placedFeature) {
        finishFeature(name, "random_patch", DataUtils.randomPatchConfig(tries, xzSpread, ySpread, placedFeature));
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.UnderwaterMagmaConfiguration#CODEC}
     */
    public void underwaterMagma(String name, int searchRange, int placementRadius, float placementProbability, Placement placement) {
        underwaterMagma(name, searchRange, placementRadius, placementProbability);
        placedFeature(name, placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.UnderwaterMagmaConfiguration#CODEC}
     */
    public void underwaterMagma(String name, int searchRange, int placementRadius, float placementProbability) {
        final JsonObject config = new JsonObject();
        config.addProperty("floor_search_range", searchRange);
        config.addProperty("placement_radius_around_floor", placementRadius);
        config.addProperty("placement_probability_per_valid_position", placementProbability);
        finishFeature(name, "underwater_magma", config);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.SpikeConfiguration#CODEC}
     */
    public void endSpike(String name, @Nullable Boolean invulnerableCrystal, Builders.EndSpike.Builder endSpikes, @Nullable BlockPos beamTarget, Placement placement) {
        endSpike(name, invulnerableCrystal, endSpikes, beamTarget);
        placedFeature(name, placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.SpikeConfiguration#CODEC}
     */
    public void endSpike(String name, @Nullable Boolean invulnerableCrystal, Builders.EndSpike.Builder endSpikes, @Nullable BlockPos beamTarget) {
        final JsonObject config = new JsonObject();
        DataUtils.addProperty(config, "crystal_invulnerable", invulnerableCrystal);
        config.add("spikes", Util.make(new Builders.EndSpike(), endSpikes).write());
        if (beamTarget != null) {
            config.add("crystal_beam_target", DataUtils.encodeBlockPos(beamTarget));
        }
        finishFeature(name, "end_spike", config);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration#CODEC}
     */
    public void seaPickle(String name, IntProvider count, Placement placement) {
        seaPickle(name, count);
        placedFeature(name, placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration#CODEC}
     */
    public void seaPickle(String name, IntProvider count) {
        finishFeature(name, "sea_pickle", DataUtils.countConfig(count));
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.HugeFungusConfiguration#CODEC}
     */
    public void hugeFungus(String name, BlockState baseBlock, BlockState stem, BlockState hat, BlockState decor, BlockPredicate replaceableBlocks, boolean planted, Placement placement) {
        hugeFungus(name, baseBlock, stem, hat, decor, replaceableBlocks, planted);
        placedFeature(name, placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.HugeFungusConfiguration#CODEC}
     */
    public void hugeFungus(String name, BlockState baseBlock, BlockState stem, BlockState hat, BlockState decor, BlockPredicate replaceableBlocks, boolean planted) {
        final JsonObject config = new JsonObject();
        config.add("valid_base_block", DataUtils.encodeBlockState(baseBlock));
        config.add("stem_state", DataUtils.encodeBlockState(stem));
        config.add("hat_state", DataUtils.encodeBlockState(hat));
        config.add("decor_state", DataUtils.encodeBlockState(decor));
        config.add("replaceable_blocks", DataUtils.encodeBlockPredicate(replaceableBlocks));
        config.addProperty("planted", planted);
        finishFeature(name, "huge_fungus", config);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.DripstoneClusterConfiguration#CODEC}
     */
    public void dripstoneCluster(
            String name,
            int searchRange,
            IntProvider height,
            IntProvider radius,
            int maxHeightDifference,
            int heightDeviation,
            IntProvider dripstoneBlockLayerThickness,
            FloatProvider density,
            FloatProvider wetness,
            float chanceOfColumnAtMaxDistFromCenter,
            int maxDistFromEdgeAffectingChanceOfDripstoneColumn,
            int maxDistFromCenterAffectingHeightBias,
            Placement placement
    ) {
        dripStoneCluster(name, searchRange, height, radius, maxHeightDifference, heightDeviation, dripstoneBlockLayerThickness, density, wetness, chanceOfColumnAtMaxDistFromCenter, maxDistFromEdgeAffectingChanceOfDripstoneColumn, maxDistFromCenterAffectingHeightBias);
        placedFeature(name, placement);
    }

    // Awful
    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.DripstoneClusterConfiguration#CODEC}
     */
    public void dripStoneCluster(
            String name,
            int searchRange,
            IntProvider height,
            IntProvider radius,
            int maxHeightDifference,
            int heightDeviation,
            IntProvider dripstoneBlockLayerThickness,
            FloatProvider density,
            FloatProvider wetness,
            float chanceOfColumnAtMaxDistFromCenter,
            int maxDistFromEdgeAffectingChanceOfDripstoneColumn,
            int maxDistFromCenterAffectingHeightBias
    ) {
        final JsonObject config = new JsonObject();
        config.addProperty("floor_to_ceiling_search_range", searchRange);
        config.add("height", DataUtils.encodeIntProvider(height));
        config.add("radius", DataUtils.encodeIntProvider(radius));
        config.addProperty("max_stalagmite_stalactite_height_diff", maxHeightDifference);
        config.addProperty("height_deviation", heightDeviation);
        config.add("dripstone_block_layer_thickness", DataUtils.encodeIntProvider(dripstoneBlockLayerThickness));
        config.add("density", DataUtils.encodeFloatProvider(density));
        config.add("wetness", DataUtils.encodeFloatProvider(wetness));
        config.addProperty("chance_of_dripstone_column_at_max_distance_from_center", chanceOfColumnAtMaxDistFromCenter);
        config.addProperty("max_distance_from_edge_affecting_chance_of_dripstone_column", maxDistFromEdgeAffectingChanceOfDripstoneColumn);
        config.addProperty("max_distance_from_center_affecting_height_bias", maxDistFromCenterAffectingHeightBias);
        finishFeature(name, "dripstone_cluster", config);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.LargeDripstoneConfiguration#CODEC}
     */
    public void largeDripstone(
            String name,
            int searchRange,
            IntProvider columnRadius,
            FloatProvider heightScale,
            float maxColumnRadiusToCaveHeightRatio,
            FloatProvider stalactiteBluntness,
            FloatProvider stalagmiteBluntness,
            FloatProvider windSpeed,
            int minWindRadius,
            float minWindBluntness,
            Placement placement
    ) {
        largeDripstone(name, searchRange, columnRadius, heightScale, maxColumnRadiusToCaveHeightRatio, stalactiteBluntness, stalagmiteBluntness, windSpeed, minWindRadius, minWindBluntness);
        placedFeature(name, placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.LargeDripstoneConfiguration#CODEC}
     */
    public void largeDripstone(
            String name,
            int searchRange,
            IntProvider columnRadius,
            FloatProvider heightScale,
            float maxColumnRadiusToCaveHeightRatio,
            FloatProvider stalactiteBluntness,
            FloatProvider stalagmiteBluntness,
            FloatProvider windSpeed,
            int minWindRadius,
            float minWindBluntness
    ) {
        final JsonObject config = new JsonObject();
        config.addProperty("floor_to_ceiling_search_range", searchRange);
        config.add("column_radius", DataUtils.encodeIntProvider(columnRadius));
        config.add("height_scale", DataUtils.encodeFloatProvider(heightScale));
        config.addProperty("max_column_radius_to_cave_height_ratio", maxColumnRadiusToCaveHeightRatio);
        config.add("stalactite_bluntness", DataUtils.encodeFloatProvider(stalactiteBluntness));
        config.add("stalagmite_bluntness", DataUtils.encodeFloatProvider(stalagmiteBluntness));
        config.add("wind_speed", DataUtils.encodeFloatProvider(windSpeed));
        config.addProperty("min_radius_for_wind", minWindRadius);
        config.addProperty("min_bluntness_for_wind", minWindBluntness);
        finishFeature(name, "large_dripstone", config);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.PointedDripstoneConfiguration#CODEC}
     */
    public void pointedDripstone(String name, @Nullable Float tallerDripstoneChance, @Nullable Float directionalSpreadChance, @Nullable Float spreadRadius2Chance, @Nullable Float spreadRadius3Chance, Placement placement) {
        pointedDripstone(name, tallerDripstoneChance, directionalSpreadChance, spreadRadius2Chance, spreadRadius3Chance);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.PointedDripstoneConfiguration#CODEC}
     */
    public void pointedDripstone(String name, @Nullable Float tallerDripstoneChance, @Nullable Float directionalSpreadChance, @Nullable Float spreadRadius2Chance, @Nullable Float spreadRadius3Chance) {
        final JsonObject config = new JsonObject();
        DataUtils.addProperty(config, "chance_of_taller_dripstone", tallerDripstoneChance);
        DataUtils.addProperty(config, "chance_of_directional_spread", directionalSpreadChance);
        DataUtils.addProperty(config, "chance_of_spread_radius2", spreadRadius2Chance);
        DataUtils.addProperty(config, "chance_of_spread_radius3", spreadRadius3Chance);
        finishFeature(name, "pointed_dripstone", config);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.LakeFeature.Configuration#CODEC}
     */
    public void lake(String name, BlockStateProvider fluid, BlockStateProvider barrier, Placement placement) {
        lake(name, fluid, barrier);
        placedFeature(name, placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.LakeFeature.Configuration#CODEC}
     */
    public void lake(String name, BlockStateProvider fluid, BlockStateProvider barrier) {
        final JsonObject config = new JsonObject();
        config.add("fluid", DataUtils.encodeBlockStateProvider(fluid));
        config.add("barrier", DataUtils.encodeBlockStateProvider(barrier));
        finishFeature(name, "lake", config);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration#CODEC}
     */
    public void disk(String name, RuleBasedBlockStateProvider stateProvider, BlockPredicate target, IntProvider radius, int halfHeight, Placement placement) {
        disk(name, stateProvider, target, radius, halfHeight);
        placedFeature(name, placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration#CODEC}
     */
    public void disk(String name, RuleBasedBlockStateProvider stateProvider, BlockPredicate target, IntProvider radius, int halfHeight) {
        final JsonObject config = new JsonObject();
        config.add("state_provider", DataUtils.encodeRuleBasedBlockStateProvider(stateProvider));
        config.add("target", DataUtils.encodeBlockPredicate(target));
        config.add("radius", DataUtils.encodeIntProvider(radius));
        config.addProperty("half_height", halfHeight);
        finishFeature(name, "disk", config);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration#CODEC}
     */
    public void simpleBlock(String name, BlockStateProvider toPlace, Placement placement) {
        simpleBlock(name, toPlace);
        placedFeature(name, placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration#CODEC}
     */
    public void simpleBlock(String name, BlockStateProvider toPlace) {
        final JsonObject config = new JsonObject();
        config.add("to_place", DataUtils.encodeBlockStateProvider(toPlace));
        finishFeature(name, "simple_block", config);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.SpringConfiguration#CODEC}
     */
    public void spring(
            String name,
            FluidState fluid,
            boolean requiresBlockBelow,
            @Nullable Integer rockCount,
            @Nullable Integer holeCount,
            Block[] validBlocks,
            Placement placement
    ) {
        spring(name, fluid, requiresBlockBelow, rockCount, holeCount, validBlocks);
        placedFeature(name, placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.SpringConfiguration#CODEC}
     */
    public void spring(
            String name,
            FluidState fluid,
            boolean requiresBlockBelow,
            @Nullable Integer rockCount,
            @Nullable Integer holeCount,
            Block[] validBlocks
    ) {
        final JsonObject config = new JsonObject();
        config.add("state", DataUtils.encode(FluidState.CODEC, fluid));
        config.addProperty("requires_block_below", requiresBlockBelow);
        DataUtils.addProperty(config, "rock_count", rockCount);
        DataUtils.addProperty(config, "hole_count", holeCount);
        final JsonArray blocks = new JsonArray(validBlocks.length);
        for (Block block : validBlocks) {
            blocks.add(RegistryInfo.BLOCK.getId(block).toString());
        }
        config.add("valid_blocks", blocks);
        finishFeature(name, "spring_feature", config);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.BlockPileConfiguration#CODEC}
     */
    public void blockPile(String name, BlockStateProvider stateProvider, Placement placement) {
        blockPile(name, stateProvider);
        placedFeature(name, placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.BlockPileConfiguration#CODEC}
     */
    public void blockPile(String name, BlockStateProvider stateProvider) {
        final JsonObject config = new JsonObject();
        config.add("state_provider", DataUtils.encodeBlockStateProvider(stateProvider));
        finishFeature(name, "block_pile", config);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.RootSystemConfiguration#CODEC}
     */
    public void rootSystem(
            String name,
            String treeFeature,
            int verticalSpaceRequiredForTree,
            int rootRadius,
            String rootReplaceableBlockTag,
            BlockStateProvider rootState,
            int rootPlacementAttempts,
            int rootMaxHeight,
            int hangingRootRadius,
            int hangingRootsVerticalSpan,
            BlockStateProvider hangingRootState,
            int hangingRootPlacementAttempts,
            int verticalWaterAllowedForTree,
            BlockPredicate allowedTreePosition,
            Placement placement
    ) {
        rootSystem(name, treeFeature, verticalSpaceRequiredForTree, rootRadius, rootReplaceableBlockTag, rootState, rootPlacementAttempts, rootMaxHeight, hangingRootRadius, hangingRootsVerticalSpan, hangingRootState, hangingRootPlacementAttempts, verticalWaterAllowedForTree, allowedTreePosition);
        placedFeature(name, placement);
    }

    // Never change Mojang
    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.RootSystemConfiguration#CODEC}
     * Uh, good luck
     */
    public void rootSystem(
            String name,
            String treeFeature,
            int verticalSpaceRequiredForTree,
            int rootRadius,
            String rootReplaceableBlockTag,
            BlockStateProvider rootState,
            int rootPlacementAttempts,
            int rootMaxHeight,
            int hangingRootRadius,
            int hangingRootsVerticalSpan,
            BlockStateProvider hangingRootState,
            int hangingRootPlacementAttempts,
            int verticalWaterAllowedForTree,
            BlockPredicate allowedTreePosition
    ) {
        final JsonObject config = new JsonObject();
        config.addProperty("feature", treeFeature);
        config.addProperty("required_vertical_space_for_tree", verticalSpaceRequiredForTree);
        config.addProperty("root_radius", rootRadius);
        config.addProperty("root_replaceable", rootReplaceableBlockTag);
        config.add("root_state_provider", DataUtils.encodeBlockStateProvider(rootState));
        config.addProperty("root_placement_attempts", rootPlacementAttempts);
        config.addProperty("root_column_max_height", rootMaxHeight);
        config.addProperty("hanging_root_radius", hangingRootRadius);
        config.addProperty("hanging_roots_vertical_span", hangingRootsVerticalSpan);
        config.add("hanging_roots_state_provider", DataUtils.encodeBlockStateProvider(hangingRootState));
        config.addProperty("hanging_root_placement_attempts", hangingRootPlacementAttempts);
        config.addProperty("allowed_vertical_water_for_tree", verticalWaterAllowedForTree);
        config.add("allowed_tree_position", DataUtils.encodeBlockPredicate(allowedTreePosition));
        finishFeature(name, "root_system", config);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration#CODEC}
     */
    public void hugeRedMushroom(String name, BlockStateProvider cap, BlockStateProvider stem, @Nullable Integer foliageRadius, Placement placement) {
        hugeRedMushroom(name, cap, stem, foliageRadius);
        placedFeature(name, placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration#CODEC}
     */
    public void hugeRedMushroom(String name, BlockStateProvider cap, BlockStateProvider stem, @Nullable Integer foliageRadius) {
        hugeShroom(name, cap, stem, foliageRadius, true);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration#CODEC}
     */
    public void hugeBrownMushroom(String name, BlockStateProvider cap, BlockStateProvider stem, @Nullable Integer foliageRadius, Placement placement) {
        hugeBrownMushroom(name, cap, stem, foliageRadius);
        placedFeature(name, placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration#CODEC}
     */
    public void hugeBrownMushroom(String name, BlockStateProvider cap, BlockStateProvider stem, @Nullable Integer foliageRadius) {
        hugeShroom(name, cap, stem, foliageRadius, false);
    }

    private void hugeShroom(String name, BlockStateProvider cap, BlockStateProvider stem, @Nullable Integer foliageRadius, boolean red) {
        final JsonObject config = new JsonObject();
        config.add("cap_provider", DataUtils.encodeBlockStateProvider(cap));
        config.add("stem_provider", DataUtils.encodeBlockStateProvider(stem));
        DataUtils.addProperty(config, "foliage_radius", foliageRadius);
        finishFeature(name, red ? "huge_red_mushroom" : "huge_brown_mushroom", config);
    }

    /**
     * {@link BlockColumnConfiguration#CODEC}
     */
    public void blockColumn(String name, BlockColumnConfiguration.Layer[] layers, Direction direction, BlockPredicate allowedPlacement, boolean prioritizeTip, Placement placement) {
        blockColumn(name, layers, direction, allowedPlacement, prioritizeTip);
        placedFeature(name, placement);
    }

    /**
     * {@link BlockColumnConfiguration#CODEC}
     */
    public void blockColumn(String name, BlockColumnConfiguration.Layer[] layers, Direction direction, BlockPredicate allowedPlacement, boolean prioritizeTip) {
        final JsonObject config = new JsonObject();
        config.add("layers", DataUtils.encodeBlockColumnLayerArray(layers));
        config.addProperty("direction", direction.getSerializedName());
        config.add("allowed_placement", DataUtils.encodeBlockPredicate(allowedPlacement));
        config.addProperty("prioritize_tip", prioritizeTip);
        finishFeature(name, "block_column", config);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.NetherForestVegetationConfig#CODEC}
     */
    public void netherForestVegetation(String name, BlockStateProvider stateProvider, int spreadWidth, int spreadHeight, Placement placement) {
        netherForestVegetation(name, stateProvider, spreadWidth, spreadHeight);
        placedFeature(name, placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.NetherForestVegetationConfig#CODEC}
     */
    public void netherForestVegetation(String name, BlockStateProvider stateProvider, int spreadWidth, int spreadHeight) {
        final JsonObject config = new JsonObject();
        config.add("state_provider", DataUtils.encodeBlockStateProvider(stateProvider));
        config.addProperty("spread_width", spreadWidth);
        config.addProperty("spread_height", spreadHeight);
        finishFeature(name, "nether_forest_vegetation", config);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.SimpleRandomFeatureConfiguration#CODEC}
     */
    public void simpleRandomSelector(String name, String[] features, Placement placement) {
        simpleRandomSelector(name, features);
        placedFeature(name, placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.SimpleRandomFeatureConfiguration#CODEC}
     */
    public void simpleRandomSelector(String name, String[] features) {
        final JsonObject config = new JsonObject();
        config.add("features", DataUtils.encodeStringArray(features));
        finishFeature(name, "simple_random_selector", config);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.RandomBooleanFeatureConfiguration#CODEC}
     */
    public void randomBooleanSelector(String name, String ifTrue, String ifFalse, Placement placement) {
        randomBooleanSelector(name, ifTrue, ifFalse);
        placedFeature(name, placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.RandomBooleanFeatureConfiguration#CODEC}
     */
    public void randomBooleanSelector(String name, String ifTrue, String ifFalse) {
        final JsonObject config = new JsonObject();
        config.addProperty("feature_true", ifTrue);
        config.addProperty("feature_false", ifFalse);
        finishFeature(name, "random_boolean_selector", config);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration#CODEC}
     */
    public void vegetationPatch(
            String name,
            boolean waterlogged,
            String replaceableBlockTag,
            BlockStateProvider groundState,
            String vegetationFeature,
            CaveSurface surface,
            IntProvider depth,
            float extraBottomBlockChance,
            int verticalRange,
            float vegetationChance,
            IntProvider xzRadius,
            float extraEdgeColumnChance,
            Placement placement
    ) {
        vegetationPatch(name, waterlogged, replaceableBlockTag, groundState, vegetationFeature, surface, depth, extraBottomBlockChance, verticalRange, vegetationChance, xzRadius, extraEdgeColumnChance);
        placedFeature(name, placement);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration#CODEC}
     */
    public void vegetationPatch(
            String name,
            boolean waterlogged,
            String replaceableBlockTag,
            BlockStateProvider groundState,
            String vegetationFeature,
            CaveSurface surface,
            IntProvider depth,
            float extraBottomBlockChance,
            int verticalRange,
            float vegetationChance,
            IntProvider xzRadius,
            float extraEdgeColumnChance
    ) {
        final JsonObject config = new JsonObject();
        config.addProperty("replaceable", replaceableBlockTag);
        config.add("ground_state", DataUtils.encodeBlockStateProvider(groundState));
        config.addProperty("vegetation_feature", vegetationFeature);
        config.addProperty("surface", surface.getSerializedName());
        config.add("depth", DataUtils.encodeIntProvider(depth));
        config.addProperty("extra_bottom_block_chance", extraBottomBlockChance);
        config.addProperty("vertical_range", verticalRange);
        config.addProperty("vegetation_chance", vegetationChance);
        config.add("xz_radius", DataUtils.encodeIntProvider(xzRadius));
        config.addProperty("extra_edge_column_chance", extraEdgeColumnChance);
        finishFeature(name, waterlogged ? "waterlogged_vegetation_patch" : "vegetation_patch", config);
    }

    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration#CODEC}
     */
    public void geode(
            String name,
            BlockStateProvider fillingProvider,
            BlockStateProvider innerLayerProvider,
            BlockStateProvider altInnerLayerProvider,
            BlockStateProvider middleLayerProvider,
            BlockStateProvider outerLayerProvider,
            BlockState[] innerPlacements,
            ResourceLocation cannotReplace,
            ResourceLocation invalidBlocks,
            @Nullable Double fillingRange,
            @Nullable Double innerLayerRange,
            @Nullable Double middleLayerRange,
            @Nullable Double outerLayerRange,
            @Nullable Double generateCrackChance,
            @Nullable Double baseCrackSize,
            @Nullable Integer crackPointOffset,
            @Nullable Double usePotentialPlacementsChance,
            @Nullable Double useAlternateLayer0Chance,
            boolean placementsRequireLayer0Alternate,
            @Nullable IntProvider outerWallDistance,
            @Nullable IntProvider distributionPoints,
            @Nullable IntProvider pointOffset,
            @Nullable Integer minGenOffset,
            @Nullable Integer maxGenOffset,
            @Nullable Double noiseMultiplier,
            int invalidBlocksThreshold,
            Placement placement
    ) {
        geode(
                name, fillingProvider, innerLayerProvider, altInnerLayerProvider, middleLayerProvider,
                outerLayerProvider, innerPlacements, cannotReplace, invalidBlocks, fillingRange,
                innerLayerRange, middleLayerRange, outerLayerRange, generateCrackChance,
                baseCrackSize, crackPointOffset, usePotentialPlacementsChance, useAlternateLayer0Chance,
                placementsRequireLayer0Alternate, outerWallDistance, distributionPoints, pointOffset,
                minGenOffset, maxGenOffset, noiseMultiplier, invalidBlocksThreshold
        );
        placedFeature(name, placement);
    }

    // The price we pay for peace
    /**
     * {@link net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration#CODEC}
     */
    public void geode(
            String name,
            BlockStateProvider fillingProvider,
            BlockStateProvider innerLayerProvider,
            BlockStateProvider altInnerLayerProvider,
            BlockStateProvider middleLayerProvider,
            BlockStateProvider outerLayerProvider,
            BlockState[] innerPlacements,
            ResourceLocation cannotReplace,
            ResourceLocation invalidBlocks,
            @Nullable Double fillingRange,
            @Nullable Double innerLayerRange,
            @Nullable Double middleLayerRange,
            @Nullable Double outerLayerRange,
            @Nullable Double generateCrackChance,
            @Nullable Double baseCrackSize,
            @Nullable Integer crackPointOffset,
            @Nullable Double usePotentialPlacementsChance,
            @Nullable Double useAlternateLayer0Chance,
            boolean placementsRequireLayer0Alternate,
            @Nullable IntProvider outerWallDistance,
            @Nullable IntProvider distributionPoints,
            @Nullable IntProvider pointOffset,
            @Nullable Integer minGenOffset,
            @Nullable Integer maxGenOffset,
            @Nullable Double noiseMultiplier,
            int invalidBlocksThreshold
    ) {
        final GeodeBlockSettings blockSettings = new GeodeBlockSettings(
                fillingProvider,
                innerLayerProvider,
                altInnerLayerProvider,
                middleLayerProvider,
                outerLayerProvider,
                List.of(innerPlacements),
                TagKey.create(Registries.BLOCK, cannotReplace),
                TagKey.create(Registries.BLOCK, invalidBlocks)
        );
        final JsonElement blocks = DataUtils.encode(GeodeBlockSettings.CODEC, blockSettings);

        final JsonObject layers = new JsonObject();
        DataUtils.addProperty(layers, "filling", fillingRange);
        DataUtils.addProperty(layers, "inner_layer", innerLayerRange);
        DataUtils.addProperty(layers, "middle_layer", middleLayerRange);
        DataUtils.addProperty(layers, "outer_layer", outerLayerRange);

        final JsonObject crack = new JsonObject();
        DataUtils.addProperty(crack, "generate_crack_chance", generateCrackChance);
        DataUtils.addProperty(crack, "base_crack_size", baseCrackSize);
        DataUtils.addProperty(crack, "crack_point_offset", crackPointOffset);

        final JsonObject config = new JsonObject();
        config.add("blocks", blocks);
        config.add("layers", layers);
        config.add("crack", crack);
        DataUtils.addProperty(config, "use_potential_placements_chance", usePotentialPlacementsChance);
        DataUtils.addProperty(config, "use_alternate_layer0_chance", useAlternateLayer0Chance);
        DataUtils.addProperty(config, "placements_require_layer0_alternate", placementsRequireLayer0Alternate);
        config.add("outer_wall_distance", DataUtils.encodeIntProvider(outerWallDistance == null ? UniformInt.of(4, 5) : outerWallDistance));
        config.add("distribution_points", DataUtils.encodeIntProvider(distributionPoints == null ? UniformInt.of(3, 4) : distributionPoints));
        config.add("point_offset", DataUtils.encodeIntProvider(pointOffset == null ? UniformInt.of(1, 2) : pointOffset));
        DataUtils.addProperty(config, "min_gen_offset", minGenOffset);
        DataUtils.addProperty(config, "max_gen_offset", maxGenOffset);
        DataUtils.addProperty(config, "noise_multiplier", noiseMultiplier);
        config.addProperty("invalid_blocks_threshold", invalidBlocksThreshold);
        finishFeature(name, "geode", config);
    }

    @Override
    protected void afterPosted(EventResult result) {
        super.afterPosted(result);
        DataUtils.setJde(null);
    }
}
