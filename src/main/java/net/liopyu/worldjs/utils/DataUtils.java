package net.liopyu.worldjs.utils;

import com.google.common.collect.ImmutableMap;
import net.liopyu.worldjs.WorldJS;
import net.liopyu.worldjs.api.IAdditionalConfiguredFeatureMethodHolder;
import net.liopyu.worldjs.api.IAdditionalPlacedFeatureMethodHolder;
import net.liopyu.worldjs.events.JsonDataEventJS;
import net.liopyu.worldjs.events.forge.AddConfiguredFeatureMethods;
import net.liopyu.worldjs.events.forge.AddPlacedFeatureMethods;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
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

    private static final ThreadLocal<@Nullable PlacedFeatureBuilder> pfb = new ThreadLocal<>();

    @ApiStatus.Internal
    public static void setPfb(@Nullable PlacedFeatureBuilder pfb) {
        DataUtils.pfb.set(pfb);
    }

    @Nullable
    public static PlacedFeatureBuilder getCurrentPlacedFeatureBuilder() {
        return pfb.get();
    }

    public static final Supplier<ImmutableMap<String, IAdditionalPlacedFeatureMethodHolder>> modPlacements = Lazy.of(() -> Util.make(new ImmutableMap.Builder<String, IAdditionalPlacedFeatureMethodHolder>(), b -> MinecraftForge.EVENT_BUS.post(new AddPlacedFeatureMethods(b))).build());

    private static final ThreadLocal<@Nullable JsonDataEventJS> jde = new ThreadLocal<>();

    @ApiStatus.Internal
    public static void setJde(@Nullable JsonDataEventJS jde) {
        DataUtils.jde.set(jde);
    }

    @Nullable
    public static JsonDataEventJS getCurrentJsonDataEventJS() {
        return jde.get();
    }

    public static final Supplier<ImmutableMap<String, IAdditionalConfiguredFeatureMethodHolder>> modConfiguredFeatures = Lazy.of(() -> Util.make(new ImmutableMap.Builder<String, IAdditionalConfiguredFeatureMethodHolder>(), b -> MinecraftForge.EVENT_BUS.post(new AddConfiguredFeatureMethods(b))).build());
}
