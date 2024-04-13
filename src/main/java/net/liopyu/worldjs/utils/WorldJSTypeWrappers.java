package net.liopyu.worldjs.utils;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.kubejs.block.state.BlockStatePredicate;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.ListJS;
import dev.latvian.mods.kubejs.util.MapJS;
import dev.latvian.mods.kubejs.util.UtilsJS;
import dev.latvian.mods.rhino.Context;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.InclusiveRange;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.*;
import net.minecraft.world.level.levelgen.heightproviders.*;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class WorldJSTypeWrappers {

    public static FloatProvider floatProvider(Context cx, @Nullable Object o) {
        if (o instanceof Number num) {
            return ConstantFloat.of(num.floatValue());
        } else if (o instanceof FloatProvider prov) {
            return prov;
        } else if (o instanceof List<?> list && !list.isEmpty()) {
            final Number min = (Number) list.get(0); // Mirrors how kube does Int/NumberProviders
            final Number max = list.size() >= 2 ? (Number) list.get(1) : min;
            return UniformFloat.of(min.floatValue(), max.floatValue());
        } else if (o instanceof Map<?,?> map) {
            if (map.containsKey("max") && map.containsKey("min")) {
                if (map.containsKey("plateau")) {
                    return TrapezoidFloat.of(((Number) map.get("min")).floatValue(), ((Number) map.get("max")).floatValue(), ((Number) map.get("plateau")).floatValue());
                } else if (map.containsKey("mean") && map.containsKey("deviation")) {
                    return ClampedNormalFloat.of(((Number) map.get("mean")).floatValue(), ((Number) map.get("deviation")).floatValue(), ((Number) map.get("max")).floatValue(), ((Number) map.get("plateau")).floatValue());
                } else {
                    return UniformFloat.of(((Number) map.get("max")).floatValue(), ((Number) map.get("plateau")).floatValue());
                }
            } else if (map.containsKey("value")) {
                return ConstantFloat.of(((Number) map.get("value")).floatValue());
            }
        } else if (o instanceof JsonElement json) {
            final AtomicReference<FloatProvider> returned = new AtomicReference<>(ConstantFloat.of(0.0F));
            FloatProvider.CODEC.decode(JsonOps.INSTANCE, json).get().map(l -> {
                returned.set(l.getFirst());
                return l;
            }, r -> {
                ScriptType.getCurrent(cx).console.warn("Unable to parse float provider json " + json + " into a FloatProvider");
                return r;
            });
            return returned.get();
        }

        return ConstantFloat.of(0.0F);
    }

    public static BlockPredicate blockPredicate(Context cx, @Nullable Object o) {
        if (o instanceof BlockPredicate predicate) {
            return predicate;
        } else if (o instanceof BlockStatePredicate statePredicate) {
            return BlockPredicate.matchesBlocks(statePredicate.getBlocks().toArray(new Block[0]));
        } else if (o instanceof JsonElement json) {
            final AtomicReference<BlockPredicate> returned = new AtomicReference<>(BlockPredicate.not(BlockPredicate.alwaysTrue()));
            BlockPredicate.CODEC.decode(JsonOps.INSTANCE, json).get().map(l -> {
                returned.set(l.getFirst());
                return l;
            }, r -> {
                ScriptType.getCurrent(cx).console.warn("Unable to parse block predicate json " + json + " into a BlockPredicate");
                return r;
            });
            return returned.get();
        } else if (o instanceof Map<?, ?> map) {
            Vec3i offset = Vec3i.ZERO;
            if (map.containsKey("offset")) {
                offset = UtilsJS.blockPosOf(map.get("offset"));
            }
            final String type = String.valueOf(map.get("type"));
            return switch (type) {
                case "true", "always_true", "alwaysTrue" -> BlockPredicate.alwaysTrue();
                case "in_world", "inWorld" -> BlockPredicate.insideWorld(offset);
                case "sturdy_face", "sturdy", "sturdyFace" -> BlockPredicate.hasSturdyFace(offset, Direction.CODEC.byName(String.valueOf(map.get("direction")), Direction.DOWN));
                case "replaceable" -> BlockPredicate.replaceable(offset);
                case "would_survive", "wouldSurvive" -> BlockPredicate.wouldSurvive(blockState(cx, map.get("state")), offset);
                case "solid" -> BlockPredicate.solid(offset);
                case "no_fluid", "noFluid" -> BlockPredicate.noFluid(offset);
                case "not" -> BlockPredicate.not(blockPredicate(cx, map.get("value")));
                case "fluids" -> {
                    final List<Fluid> fluids = new ArrayList<>();
                    final List<?> objects = ListJS.orSelf(map.get("fluids"));
                    for (Object obj : objects) {
                        fluids.add(RegistryInfo.FLUID.getValue(new ResourceLocation(String.valueOf(obj))));
                    }
                    yield BlockPredicate.matchesFluids(offset, fluids);
                }
                case "tag" -> BlockPredicate.matchesTag(offset, TagKey.create(Registries.BLOCK, new ResourceLocation(String.valueOf(map.get("tag")))));
                case "blocks" -> {
                    final List<Block> blocks = new ArrayList<>();
                    final List<?> objects = ListJS.orSelf(map.get("blocks"));
                    for (Object obj : objects) {
                        blocks.add(RegistryInfo.BLOCK.getValue(new ResourceLocation(String.valueOf(obj))));
                    }
                    yield BlockPredicate.matchesBlocks(offset, blocks);
                }
                case "any_of", "anyOf" -> {
                    final List<BlockPredicate> predicates = new ArrayList<>();
                    final List<?> objects = ListJS.orSelf(map.get("predicates"));
                    for (Object obj : objects) {
                        predicates.add(blockPredicate(cx, obj));
                    }
                    yield BlockPredicate.anyOf(predicates);
                }
                case "all_of", "allOf" -> {
                    final List<BlockPredicate> predicates = new ArrayList<>();
                    final List<?> objects = ListJS.orSelf(map.get("predicates"));
                    for (Object obj : objects) {
                        predicates.add(blockPredicate(cx, obj));
                    }
                    yield BlockPredicate.allOf(predicates);
                }
                default -> BlockPredicate.not(BlockPredicate.alwaysTrue());
            };
        } else if (o instanceof CharSequence) {
            final String type = o.toString();
            return switch (type) {
                case "true", "always_true", "alwaysTrue" -> BlockPredicate.alwaysTrue();
                case "no_fluid", "noFluid" -> BlockPredicate.noFluid();
                case "replaceable" -> BlockPredicate.replaceable();
                case "in_world", "inWorld" -> BlockPredicate.insideWorld(Vec3i.ZERO);
                default -> BlockPredicate.not(BlockPredicate.alwaysTrue());
            };
        }

        return BlockPredicate.not(BlockPredicate.alwaysTrue());
    }

    public static <T extends Comparable<T>> BlockState blockState(Context cx, @Nullable Object o) {
        if (o == null) {
            return Blocks.AIR.defaultBlockState();
        } else if (o instanceof BlockState state) {
            return state;
        } else if (o instanceof Block block) {
            return block.defaultBlockState();
        } else if (o instanceof CharSequence) {
            return UtilsJS.parseBlockState(o.toString());
        } else if (o instanceof Map<?,?> map) {
            final Object possibleBlock = GeneralUtils.getFirstOfKeys(map, "name", "Name", "block", "base"); // Pls only pass in a string here k thx
            BlockState state = (possibleBlock == null ? Blocks.AIR : RegistryInfo.BLOCK.getValue(new ResourceLocation(String.valueOf(possibleBlock)))).defaultBlockState();
            final Object key = GeneralUtils.getFirstKeyMapHas(map, "properties", "Properties", "states", "values", "state");
            if (key != null) {
                final Map<?, ?> propertiesMap = MapJS.of(map.get(key));
                if (propertiesMap != null) {
                    final Collection<Property<?>> stateProperties = state.getProperties();
                    final AtomicReference<BlockState> stateReference = new AtomicReference<>(state);
                    for (Property<?> property : stateProperties) {
                        final Property<T> castedProperty = UtilsJS.cast(property);
                        if (propertiesMap.containsKey(castedProperty.getName())) {
                            castedProperty.getValue(String.valueOf(propertiesMap.get(castedProperty.getName()))).ifPresent(value -> stateReference.set(stateReference.get().setValue(castedProperty, value)));
                        }
                    }
                    state = stateReference.get();
                }
            }
            return state;
        } else if (o instanceof JsonElement json) {
            final AtomicReference<BlockState> returned = new AtomicReference<>(Blocks.AIR.defaultBlockState());
            BlockState.CODEC.decode(JsonOps.INSTANCE, json).get().map(l -> {
                returned.set(l.getFirst());
                return l;
            }, r -> {
                ScriptType.getCurrent(cx).console.warn("Unable to parse block state json" + json + " into a BlockState");
                return r;
            });
            return returned.get();
        }

        return Blocks.AIR.defaultBlockState();
    }

    public static HeightProvider heightProvider(Context cx, @Nullable Object o) {
        if (o instanceof HeightProvider provider) {
            return provider;
        } else if (o instanceof Number num) {
            return ConstantHeight.of(VerticalAnchor.absolute(num.intValue()));
        } else if (o instanceof JsonElement json) {
            final AtomicReference<HeightProvider> returned = new AtomicReference<>(ConstantHeight.ZERO);
            HeightProvider.CODEC.decode(JsonOps.INSTANCE, json).get().map(l -> {
                returned.set(l.getFirst());
                return l;
            }, r -> {
                ScriptType.getCurrent(cx).console.warn("Unable to parse height provider json " + json + " into a HeightProvider");
                return r;
            });
            return returned.get();
        } else if (o instanceof Map<?,?> map) {
            final String type = String.valueOf(map.get("type"));
            return switch (type) {
                case "constant" -> ConstantHeight.of(verticalAnchor(cx, map.get("anchor")));
                case "bottom_bias", "biased_to_bottom", "biasedToBottom", "bottomBias" -> {
                    final boolean very = map.containsKey("very") ? ((Boolean) map.get("very")) : false;
                    final int inner = map.containsKey("inner") ? ((Number) map.get("inner")).intValue() : 1;
                    yield very ?
                            VeryBiasedToBottomHeight.of(verticalAnchor(cx, map.get("min")), verticalAnchor(cx, map.get("max")), inner) :
                            BiasedToBottomHeight.of(verticalAnchor(cx, map.get("min")), verticalAnchor(cx, map.get("max")), inner);
                }
                case "uniform" -> UniformHeight.of(verticalAnchor(cx, map.get("min")), verticalAnchor(cx, map.get("max")));
                case "trapezoid" -> {
                    final int plateau = map.containsKey("plateau") ? ((Number) map.get("plateau")).intValue() : 0;
                    yield TrapezoidHeight.of(verticalAnchor(cx, map.get("min")), verticalAnchor(cx, map.get("max")), plateau);
                }
                case "list", "weighted_list", "weightedList", "simple_weighted_list", "simpleWeightedList" -> {
                    final SimpleWeightedRandomList.Builder<HeightProvider> providerListBuilder = SimpleWeightedRandomList.builder();
                    final List<?> providers = ListJS.orSelf(map.get("values"));
                    for (Object obj : providers) {
                        final int weight = obj instanceof Map<?,?> subMap && subMap.containsKey("weight") ? ((Number) subMap.get("weight")).intValue() : 1;
                        providerListBuilder.add(heightProvider(cx, obj), weight);
                    }
                    yield new WeightedListHeight(providerListBuilder.build());
                }
                default -> ConstantHeight.ZERO;
            };
        }

        return ConstantHeight.ZERO;
    }

    public static VerticalAnchor verticalAnchor(Context cx, @Nullable Object o) {
        if (o instanceof VerticalAnchor anchor) {
            return anchor;
        } else if (o instanceof Number number) {
            return VerticalAnchor.absolute(number.intValue());
        } else if (o instanceof CharSequence) {
            final String type = String.valueOf(o);
            return switch (type) {
                case "above", "above_bottom", "aboveBottom", "bottom" -> VerticalAnchor.BOTTOM;
                case "below", "below_top", "belowTop", "top" -> VerticalAnchor.TOP;
                default -> VerticalAnchor.absolute(Integer.getInteger(type, 0));
            };
        } else if (o instanceof Map<?, ?> map) {
            final String type = String.valueOf(map.get("type"));
            final int value = map.containsKey("value") ? ((Number) map.get("value")).intValue() : 0;
            return switch (type) {
                case "above", "above_bottom", "aboveBottom", "bottom" -> VerticalAnchor.aboveBottom(value);
                case "below", "below_top", "belowTop", "top" -> VerticalAnchor.belowTop(value);
                default -> VerticalAnchor.absolute(value);
            };
        } else if (o instanceof JsonElement json) {
            final AtomicReference<VerticalAnchor> returned = new AtomicReference<>(VerticalAnchor.absolute(0));
            VerticalAnchor.CODEC.decode(JsonOps.INSTANCE, json).get().map(l -> {
                returned.set(l.getFirst());
                return l;
            }, r -> {
                ScriptType.getCurrent(cx).console.warn("Unable to parse vertical anchor json " + json + " into a VerticalAnchor");
                return r;
            });
            return returned.get();
        }

        return VerticalAnchor.absolute(0);
    }
    
    public static BlockStateProvider blockStateProvider(Context cx, @Nullable Object o) {
        if (o instanceof BlockStateProvider provider) {
            return provider;
        } else if (o instanceof BlockState state) {
            return BlockStateProvider.simple(state);
        } else if (o instanceof Block block) {
            return BlockStateProvider.simple(block);
        } else if (o instanceof CharSequence) {
            return BlockStateProvider.simple(UtilsJS.parseBlockState(o.toString()));
        } else if (o instanceof JsonElement json) {
            final AtomicReference<BlockStateProvider> returned = new AtomicReference<>(BlockStateProvider.simple(Blocks.AIR));
            BlockStateProvider.CODEC.decode(JsonOps.INSTANCE, json).get().map(l -> {
                returned.set(l.getFirst());
                return l;
            }, r -> {
                ScriptType.getCurrent(cx).console.warn("Unable to parse block state provider json " + json + " into a BlockStateProvider");
                return r;
            });
            return returned.get();
        } else if (o instanceof Map<?,?> map) {
            final String type = String.valueOf(map.get("type"));
            return switch (type) {
                case "simple", "block", "state", "blockstate" -> BlockStateProvider.simple(blockState(cx, map.get("state")));
                case "dual_noise", "dualNoise" -> {
                    final BasicNoiseHolder holder = BasicNoiseHolder.fromMap(map, cx);
                    final List<BlockState> states = new ArrayList<>();
                    final List<?> objects = ListJS.orSelf(map.get("states"));
                    for (Object obj : objects) {
                        states.add(blockState(cx, obj));
                    }
                    final IntProvider variety = UtilsJS.intProviderOf(map.get("variety")); // Cheeky way of getting around dealing with that
                    yield new DualNoiseProvider(
                            new InclusiveRange<>(variety.getMinValue(), variety.getMaxValue()),
                            noiseParameters(cx, map.get("slow_noise_parameters")),
                            ((Number) map.get("slow_scale")).floatValue(),
                            holder.seed,
                            holder.parameters,
                            holder.scale,
                            states
                    );
                }
                case "noise" -> {
                    final BasicNoiseHolder holder = BasicNoiseHolder.fromMap(map, cx);
                    final List<BlockState> states = new ArrayList<>();
                    final List<?> objects = ListJS.orSelf(map.get("states"));
                    for (Object obj : objects) {
                        states.add(blockState(cx, obj));
                    }
                    yield new NoiseProvider(holder.seed, holder.parameters, holder.scale, states);
                }
                case "noise_threshold", "noiseThreshold" -> {
                    final BasicNoiseHolder holder = BasicNoiseHolder.fromMap(map, cx);
                    final List<BlockState> lowStates = new ArrayList<>();
                    final List<?> lowObjects = ListJS.orSelf(GeneralUtils.getFirstOfKeys(map, "low_states", "low", "lowStates"));
                    for (Object obj : lowObjects) {
                        lowStates.add(blockState(cx, obj));
                    }
                    final List<BlockState> highStates = new ArrayList<>();
                    final List<?> highObjects = ListJS.orSelf(GeneralUtils.getFirstOfKeys(map, "high_states", "high", "highStates"));
                    for (Object obj : highObjects) {
                        highStates.add(blockState(cx, obj));
                    }
                    yield new NoiseThresholdProvider(
                            holder.seed,
                            holder.parameters,
                            holder.scale,
                            ((Number) map.get("threshold")).floatValue(),
                            ((Number) GeneralUtils.getFirstOfKeys(map, "high_chance", "highChance")).floatValue(),
                            blockState(cx, map.get("default_state")),
                            lowStates,
                            highStates
                    );
                }
                case "random_int", "randomInt" -> new RandomizedIntStateProvider(
                        blockStateProvider(cx, map.get("source")),
                        String.valueOf(map.get("property")),
                        UtilsJS.intProviderOf(map.get("values"))
                );
                case "rotated_block", "rotatedBlock", "rotated" -> new RotatedBlockProvider(RegistryInfo.BLOCK.getValue(new ResourceLocation(String.valueOf(map.get("block")))));
                case "list", "weighted_list", "weightedList", "simple_weighted_list", "simpleWeightedList", "weighted" -> {
                    final SimpleWeightedRandomList.Builder<BlockState> builder = SimpleWeightedRandomList.builder();
                    final List<?> objects = ListJS.orSelf(map.get("states"));
                    for (Object obj : objects) {
                        builder.add(blockState(cx, obj), obj instanceof Map<?,?> subMap && subMap.containsKey("weight") ? ((Number) subMap.get("weight")).intValue() : 1);
                    }
                    yield new WeightedStateProvider(builder);
                }
                default -> BlockStateProvider.simple(Blocks.AIR);
            };
        }

        return BlockStateProvider.simple(Blocks.AIR);
    }

    private record BasicNoiseHolder(long seed, NormalNoise.NoiseParameters parameters, float scale) {

        static BasicNoiseHolder fromMap(Map<?, ?> map, Context cx) {
            return new BasicNoiseHolder(
                    ((Number) map.get("seed")).longValue(),
                    noiseParameters(cx, map.get("parameters")),
                    ((Number) map.get("scale")).floatValue()
            );
        }
    }

    public static NormalNoise.NoiseParameters noiseParameters(Context cx, @Nullable Object o) {
        if (o instanceof NormalNoise.NoiseParameters parameters) {
            return parameters;
        } else if (o instanceof JsonElement json) {
            final AtomicReference<NormalNoise.NoiseParameters> returned = new AtomicReference<>(new NormalNoise.NoiseParameters(0, 0D));
            NormalNoise.NoiseParameters.CODEC.decode(JsonOps.INSTANCE, json).get().map(l -> {
                returned.set(l.getFirst().get());
                return l;
            }, r -> {
                ScriptType.getCurrent(cx).console.warn("Unable to parse noise parameters json " + json + " into a NoiseParameters");
                return r;
            });
            return returned.get();
        } else if (o instanceof List<?> list) {
            final List<Number> numbers = new ArrayList<>();
            for (Object obj : list) {
                if (obj instanceof Number num) {
                    numbers.add(num);
                }
            }
            while (numbers.size() < 2) {
                numbers.add(0); // I hate this, but It Just Works™
            }
            final double[] otherAmplitudes = new double[numbers.size() - 2];
            for (int i = 0 ; i < otherAmplitudes.length ; i++) {
                otherAmplitudes[i] = numbers.get(i + 2).doubleValue();
            }
            return new NormalNoise.NoiseParameters(numbers.get(0).intValue(), numbers.get(1).doubleValue(), otherAmplitudes);
        } else if (o instanceof Map<?,?> map) {
            final int firstOctave = ((Number) GeneralUtils.getFirstOfKeys(map, "first_octave", "firstOctave")).intValue();
            final List<Double> doubles = ListJS.orSelf(map.get("amplitudes")).stream().filter(v -> v instanceof Number).map(v -> ((Number) v).doubleValue()).toList();
            if (doubles.size() == 1) {
                return new NormalNoise.NoiseParameters(firstOctave, doubles.get(0));
            } else {
                final double[] doublesArray = new double[doubles.size() - 1];
                for (int i = 0 ; i < doublesArray.length ; i++) {
                    doublesArray[i] = doubles.get(i + 1);
                }
                return new NormalNoise.NoiseParameters(firstOctave, doubles.get(0), doublesArray);
            }
        }

        return new NormalNoise.NoiseParameters(0, 0D);
    }
}
