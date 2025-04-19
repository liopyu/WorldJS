package net.liopyu.worldjs.builders;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

// TODO: Document
public class FeatureBuilder extends BuilderBase<FeatureBuilder.KubeFeature> {

    public transient final CodecBuilder codecBuilder;
    public transient Predicate<PlaceCtx> placement;

    public FeatureBuilder(ResourceLocation i) {
        super(i);
        codecBuilder = new CodecBuilder();
        placement = ctx -> true;
    }

    public FeatureBuilder placement(Predicate<PlaceCtx> placement) {
        this.placement = placement;
        return this;
    }

    public FeatureBuilder configuration(Consumer<CodecBuilder> builder) {
        builder.accept(codecBuilder);
        return this;
    }

    @Override
    public RegistryInfo<Feature> getRegistryType() {
        return RegistryInfo.FEATURE;
    }

    @Override
    public KubeFeature createObject() {
        return new KubeFeature(codecBuilder.build(KubeFeatureConfig::new, KubeFeatureConfig::values), placement);
    }

    public static class KubeFeature extends Feature<KubeFeatureConfig> {

        private final Predicate<PlaceCtx> placement;

        public KubeFeature(Codec<KubeFeatureConfig> pCodec, Predicate<PlaceCtx> placement) {
            super(pCodec);
            this.placement = placement;
        }

        @Override
        public boolean place(FeaturePlaceContext<KubeFeatureConfig> ctx) {
            return placement.test(new PlaceCtx(ctx));
        }
    }

    public record KubeFeatureConfig(Map<String, Object> values) implements FeatureConfiguration {}

    // TODO: Document
    public record PlaceCtx(FeaturePlaceContext<KubeFeatureConfig> ctx) {

        public Optional<ConfiguredFeature<?, ?>> topFeature() {
            return ctx.topFeature();
        }

        public WorldGenLevel level() {
            return ctx.level();
        }

        public ChunkGenerator chunkGenerator() {
            return ctx.chunkGenerator();
        }

        public RandomSource random() {
            return ctx.random();
        }

        public BlockPos origin() {
            return ctx.origin();
        }

        public Object get(String name) {
            return ctx.config().values().get(name);
        }
    }
}
