package net.liopyu.worldjs.builders;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

//TODO: Document
public class PlacementModifierTypeBuilder extends BuilderBase<PlacementModifierType<PlacementModifierTypeBuilder.KubeModifier>> {

    public transient final CodecBuilder codecBuilder;
    public transient PosSelector selector;

    public PlacementModifierTypeBuilder(ResourceLocation i) {
        super(i);
        codecBuilder = new CodecBuilder();
        selector = (ctx, r, o, c, a) -> a.accept(o);
    }

    public PlacementModifierTypeBuilder positionSelector(PosSelector selector) {
        this.selector = selector;
        return this;
    }

    public PlacementModifierTypeBuilder configuration(Consumer<CodecBuilder> builder) {
        builder.accept(codecBuilder);
        return this;
    }

    @Override
    public RegistryInfo<PlacementModifierType> getRegistryType() {
        return RegistryInfo.PLACEMENT_MODIFIER_TYPE;
    }

    @Override
    public PlacementModifierType<KubeModifier> createObject() {
        return new KubeType(codecBuilder, selector);
    }
    
    public static class KubeModifier extends PlacementModifier {

        public final Map<String, Object> values;
        private final KubeType type;
        private final PosSelector selector;

        public KubeModifier(Map<String, Object> values, KubeType type, PosSelector selector) {
            this.values = values;
            this.type = type;
            this.selector = selector;
        }

        @Override
        public Stream<BlockPos> getPositions(PlacementContext pContext, RandomSource pRandom, BlockPos pPos) {
            final PosAcceptor acceptor = new PosAcceptor();
            selector.select(pContext, pRandom, pPos, values, acceptor);
            return acceptor.stream();
        }

        @Override
        public PlacementModifierType<?> type() {
            assert type != null;
            return type;
        }
    }

    public static class KubeType implements PlacementModifierType<KubeModifier> {

        private final Codec<KubeModifier> codec;

        public KubeType(CodecBuilder codecBuilder, PosSelector selector) {
            this.codec = codecBuilder.build(map -> new KubeModifier(map, this, selector), m -> m.values);
        }

        @Override
        public Codec<KubeModifier> codec() {
            return codec;
        }
    }

    @FunctionalInterface
    public interface PosSelector {

        void select(
                PlacementContext ctx,
                RandomSource random,
                BlockPos origin,
                Map<String, Object> config,
                PosAcceptor positionAcceptor
        );
    }

    // This exists to avoid having to deal with Rhino's generics jank
    public static class PosAcceptor {

        private final List<BlockPos> positions = new ArrayList<>();

        public PosAcceptor accept(BlockPos pos) {
            positions.add(pos);
            return this;
        }

        public PosAcceptor acceptAll(BlockPos... positions) {
            return acceptAll(List.of(positions));
        }

        public PosAcceptor acceptAll(List<BlockPos> positions) {
            this.positions.addAll(positions);
            return this;
        }

        @HideFromJS
        public Stream<BlockPos> stream() {
            return positions.stream();
        }
    }
}
