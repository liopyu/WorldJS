package net.liopyu.worldjs.utils;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.kubejs.util.UtilsJS;
import net.liopyu.worldjs.WorldJS;
import net.liopyu.worldjs.api.ICFeatureMethodHolder;
import net.liopyu.worldjs.api.IPFeatureMethodHolder;
import net.liopyu.worldjs.events.JsonDataEventJS;
import net.liopyu.worldjs.events.forge.AddCFeatureMethodsEvent;
import net.liopyu.worldjs.events.forge.AddPFeatureMethodsEvent;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
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

    public static JsonElement encodeBlockState(String state) {
        return encode(BlockState.CODEC, UtilsJS.parseBlockState(state));
    }

    public static JsonElement encodeIntProvider(IntProvider provider) {
        return encode(IntProvider.CODEC, provider);
    }

    public static JsonElement encodeBlockPos(BlockPos pos) {
        return encode(BlockPos.CODEC, pos);
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
