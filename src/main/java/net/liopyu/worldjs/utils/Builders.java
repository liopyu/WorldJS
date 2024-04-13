package net.liopyu.worldjs.utils;

import com.google.gson.JsonElement;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Builders {

    public static class EndSpike {
        private final List<SpikeFeature.EndSpike> spikes = new ArrayList<>();

        public void addSpike(@Nullable Integer xCenter, @Nullable Integer zCenter, @Nullable Integer radius, @Nullable Integer height, boolean guarded) {
            spikes.add(new SpikeFeature.EndSpike(xCenter == null ? 0 : xCenter, zCenter == null ? 0 : zCenter, radius == null ? 0 : radius, height == null ? 0 : height, guarded));
        }

        @HideFromJS
        public JsonElement write() {
            return DataUtils.encode(SpikeFeature.EndSpike.CODEC.listOf(), spikes);
        }

        @FunctionalInterface
        public interface Builder extends Consumer<EndSpike> {
            void accept(EndSpike spike);
        }
    }
}
