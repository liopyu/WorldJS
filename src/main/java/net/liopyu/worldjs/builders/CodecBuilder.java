package net.liopyu.worldjs.builders;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import dev.latvian.mods.kubejs.util.UtilsJS;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RuleBasedBlockStateProvider;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

// TODO: Document. Consider: rename #add & #addAsOptional to #fieldOf & optionalFieldOf?
public class CodecBuilder {

    private final Map<String, MapCodec<?>> serializers = new HashMap<>();

    public CodecBuilder add(String name, Codec<?> codec) {
        return addRaw(name, codec.fieldOf(name));
    }

    public CodecBuilder addAsOptional(String name, Codec<?> codec) {
        return addRaw(name, codec.optionalFieldOf(name));
    }

    private CodecBuilder addRaw(String name, MapCodec<?> mapCodec) {
        serializers.put(name, mapCodec);
        return this;
    }

    public Codec<Boolean> bool() {
        return Codec.BOOL;
    }

    public Codec<Integer> intValue() {
        return Codec.INT;
    }

    public Codec<Long> longValue() {
        return Codec.LONG;
    }

    public Codec<Float> floatValue() {
        return Codec.FLOAT;
    }

    public Codec<Double> doubleValue() {
        return Codec.DOUBLE;
    }

    public Codec<Integer> intRange(int min, int max) {
        return ExtraCodecs.intRange(min, max);
    }

    public Codec<Long> longRange(long min, long max) {
        final var checker = Codec.checkRange(min, max);
        return Codec.LONG.flatXmap(checker, checker);
    }

    public Codec<Float> floatRange(float min, float max) {
        return Codec.floatRange(min, max);
    }

    public Codec<Double> doubleRange(double min, double max) {
        final var checker = Codec.checkRange(min, max);
        return Codec.DOUBLE.flatXmap(checker, checker);
    }

    public Codec<String> string() {
        return Codec.STRING;
    }

    public Codec<String> nonEmptyString() {
        return ExtraCodecs.NON_EMPTY_STRING;
    }

    public Codec<Vector3f> vec3f() {
        return ExtraCodecs.VECTOR3F;
    }

    public Codec<Vec3i> vec3i() {
        return Vec3i.CODEC;
    }

    public Codec<Vec3i> clampedVec31(int maxRange) {
        return Vec3i.offsetCodec(maxRange);
    }

    public Codec<BlockPos> blockPos() {
        return BlockPos.CODEC;
    }

    public Codec<Direction> direction() {
        return Direction.CODEC;
    }

    public Codec<Direction> verticalDirection() {
        return Direction.VERTICAL_CODEC;
    }

    public Codec<BlockPredicate> blockPredicate() {
        return BlockPredicate.CODEC;
    }

    public Codec<IntProvider> intProvider() {
        return IntProvider.CODEC;
    }

    public Codec<IntProvider> rangedIntProvider(int min, int max) {
        return IntProvider.codec(min, max);
    }

    public Codec<Heightmap.Types> heightmap() {
        return Heightmap.Types.CODEC;
    }

    public Codec<BlockState> blockState() {
        return BlockState.CODEC;
    }

    public Codec<Block> block() {
        return BuiltInRegistries.BLOCK.byNameCodec();
    }

    public Codec<FluidState> fluidState() {
        return FluidState.CODEC;
    }

    public Codec<Fluid> fluid() {
        return BuiltInRegistries.FLUID.byNameCodec();
    }

    public Codec<BlockStateProvider> blockStateProvider() {
        return BlockStateProvider.CODEC;
    }

    public Codec<FloatProvider> floatProvider() {
        return FloatProvider.CODEC;
    }

    public Codec<FloatProvider> rangedFloatProvider(float min, float max) {
        return FloatProvider.codec(min, max);
    }

    public Codec<Holder<PlacedFeature>> placedFeatureHolder() {
        return PlacedFeature.CODEC;
    }

    public Codec<RuleTest> ruleTest() {
        return RuleTest.CODEC;
    }

    public Codec<ResourceLocation> resourceLocation() {
        return ResourceLocation.CODEC;
    }

    public Codec<RuleBasedBlockStateProvider> ruleBasedBlockStateProvider() {
        return RuleBasedBlockStateProvider.CODEC;
    }

    public <F, S> Codec<Either<F, S>> xor(Codec<F> first, Codec<S> second) {
        return ExtraCodecs.xor(first, second);
    }

    /**
     * Note: The {@link MapCodec#xmap(Function, Function) #xmap}ing is done before converting to {@link com.mojang.serialization.MapCodec.MapCodecCodec MapCodecCodec}
     * because {@link com.mojang.serialization.codecs.KeyDispatchCodec#decode(DynamicOps, MapLike) KeyDispatchCodec#decode}
     * (which this is dispatched from) <i>requires</i> the codec to be a {@code MapCodecCodec} if it wants to read inline with
     * the {@code type} property of the json object (like {@link com.mojang.serialization.codecs.RecordCodecBuilder RecordCodecBuilder RecordCodecBuilder}
     * codecs), otherwise the codec properties must be put inside a {@code "value": {}} object
     */
    @HideFromJS
    public <A> Codec<A> build(Function<Map<String, Object>, A> to, Function<A, Map<String, Object>> from) {
        // Do not stare into the darkness, it will leave you broken and afraid.
        // Do not think about the monster under the bed, it will take all you ever loved.
        // Do not worry, it works. So long as that is so, you can quietly ignore this in bliss.
        MapCodec<Map<String, Object>> codec = MapCodec.unit(HashMap::new);
        for (Map.Entry<String, MapCodec<?>> entry : serializers.entrySet()) {
            codec = codec.dependent(
                    entry.getValue(),
                    map -> Pair.of(
                            UtilsJS.cast(map.get(entry.getKey())),
                            UtilsJS.cast(entry.getValue())
                    ),
                    (map, val) -> {
                        map.put(entry.getKey(), val);
                        return map;
                    });
        }
        // Setting the lifecycle to stable prevents the experimental features screen from showing up
        // Arguably, this is highly experimental, but players needn't know that
        return codec.stable().xmap(to, from).codec();
    }
}
