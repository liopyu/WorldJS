package net.liopyu.worldjs.utils;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.liopyu.worldjs.WorldJS;
import net.liopyu.worldjs.api.ICFeatureMethodHolder;
import net.liopyu.worldjs.api.IPFeatureMethodHolder;
import net.liopyu.worldjs.events.JsonDataEventJS;
import net.liopyu.worldjs.events.forge.AddCFeatureMethodsEvent;
import net.liopyu.worldjs.events.forge.AddPFeatureMethodsEvent;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.configurations.BlockColumnConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RuleBasedBlockStateProvider;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class DataUtils {

    public static ResourceLocation configuredFeatureName(String name) {
        final String[] loc = normalizeName(name);
        return new ResourceLocation(loc[0], "worldgen/configured_feature/" + loc[1]);
    }

    public static ResourceLocation placedFeatureName(String name) {
        final String[] loc = normalizeName(name);
        return new ResourceLocation(loc[0], "worldgen/placed_feature/" + loc[1]);
    }

    public static String[] normalizeName(String name) {
        final int colon = name.indexOf(':');
        if (colon == -1) {
            return new String[]{WorldJS.MODID, name};
        } else {
            return name.split(":", 2);
        }
    }

    public static <T> JsonElement encode(Codec<T> codec, T value) {
        return codec.encodeStart(JsonOps.INSTANCE, value).get().left().get(); // If this is empty you're doing something wrong
    }

    public static JsonArray encodeTargetBlockStateArray(OreConfiguration.TargetBlockState[] blockTargets) {
        final JsonArray array = new JsonArray(blockTargets.length);
        for (OreConfiguration.TargetBlockState target : blockTargets) {
            array.add(encode(OreConfiguration.TargetBlockState.CODEC, target));
        }
        return array;
    }

    public static JsonElement encodeBlockState(BlockState state) {
        return encode(BlockState.CODEC, state);
    }

    public static JsonElement encodeIntProvider(IntProvider provider) {
        return encode(IntProvider.CODEC, provider);
    }

    public static JsonElement encodeBlockPos(BlockPos pos) {
        return encode(BlockPos.CODEC, pos);
    }

    public static JsonElement encodeBlockPredicate(BlockPredicate predicate) {
        return encode(BlockPredicate.CODEC, predicate);
    }

    public static JsonElement encodeFloatProvider(FloatProvider provider) {
        return encode(FloatProvider.CODEC, provider);
    }

    public static JsonElement encodeBlockStateProvider(BlockStateProvider provider) {
        return encode(BlockStateProvider.CODEC, provider);
    }

    public static JsonElement encodeRuleBasedBlockStateProvider(RuleBasedBlockStateProvider provider) {
        return encode(RuleBasedBlockStateProvider.CODEC, provider);
    }

    public static JsonArray encodeBlockColumnLayerArray(BlockColumnConfiguration.Layer[] layers) {
        final JsonArray array = new JsonArray(layers.length);
        for (BlockColumnConfiguration.Layer layer : layers) {
            array.add(encode(BlockColumnConfiguration.Layer.CODEC, layer));
        }
        return array;
    }

    public static JsonArray encodeStringArray(String[] strings) {
        final JsonArray array = new JsonArray(strings.length);
        for (String str : strings) {
            array.add(str);
        }
        return array;
    }

    public static JsonObject randomPatchConfig(@Nullable Integer tries, @Nullable Integer xzSpread, @Nullable Integer ySpread, String placedFeature) {
        final JsonObject json = new JsonObject();
        addProperty(json, "tires", tries);
        addProperty(json, "xz_spread", xzSpread);
        addProperty(json, "y_spread", ySpread);
        json.addProperty("feature", placedFeature);
        return json;
    }

    public static JsonObject countConfig(IntProvider count) {
        final JsonObject json = new JsonObject();
        json.add("count", encodeIntProvider(count));
        return json;
    }

    public static void addProperty(JsonObject json, String property, @Nullable Number value) {
        if (value != null) json.addProperty(property, value);
    }
    public static void addProperty(JsonObject json, String property, @Nullable Boolean value) {
        if (value != null) json.addProperty(property, value);
    }
    public static void addProperty(JsonObject json, String property, @Nullable String value) {
        if (value != null) json.addProperty(property, value);
    }

    public static void addProperty(JsonObject json, String property, @Nullable JsonElement value) {
        if (value != null) json.add(property, value);
    }

    // There should never be multiple of these at any one time, so ThreadLocals shouldn't be necessary
    @Nullable
    private static PlacedFeatureBuilder pfb = null;
    @Nullable
    private static JsonDataEventJS jde = null;

    @ApiStatus.Internal
    public static void setPfb(@Nullable PlacedFeatureBuilder pfb) { DataUtils.pfb = pfb; }
    @ApiStatus.Internal
    public static void setJde(@Nullable JsonDataEventJS jde) { DataUtils.jde = jde; }

    @Nullable
    public static PlacedFeatureBuilder getCurrentPlacedFeatureBuilder() { return pfb; }
    @Nullable
    public static JsonDataEventJS getCurrentJsonDataEventJS() { return jde; }

    public static final Supplier<ImmutableMap<String, IPFeatureMethodHolder>> modPlacementModifiers = Lazy.of(() -> Util.make(new ImmutableMap.Builder<String, IPFeatureMethodHolder>(), b -> MinecraftForge.EVENT_BUS.post(new AddPFeatureMethodsEvent(b))).build());
    public static final Supplier<ImmutableMap<String, ICFeatureMethodHolder>> modConfiguredFeatures = Lazy.of(() -> Util.make(new ImmutableMap.Builder<String, ICFeatureMethodHolder>(), b -> MinecraftForge.EVENT_BUS.post(new AddCFeatureMethodsEvent(b))).build());
}
