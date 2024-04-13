package net.liopyu.worldjs.utils;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.kubejs.block.state.BlockStatePredicate;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.JsonIO;
import dev.latvian.mods.kubejs.util.ListJS;
import dev.latvian.mods.kubejs.util.UtilsJS;
import dev.latvian.mods.rhino.Context;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.heightproviders.*;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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

    public static BlockState blockState(Context cx, @Nullable Object o) {
        if (o == null) {
            return Blocks.AIR.defaultBlockState();
        } else if (o instanceof BlockState state) {
            return state;
        } else if (o instanceof Block block) {
            return block.defaultBlockState();
        } else if (o instanceof CharSequence) {
            return UtilsJS.parseBlockState(o.toString());
        }

        final AtomicReference<BlockState> returned = new AtomicReference<>(Blocks.AIR.defaultBlockState());
        BlockState.CODEC.decode(JsonOps.INSTANCE, JsonIO.of(o)).get().map(l -> {
            returned.set(l.getFirst());
            return l;
        }, r -> {
            ScriptType.getCurrent(cx).console.warn("Unable to parse block state " + o + " into a BlockState");
            return r;
        });
        return returned.get();
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
                case "bottom_bias", "biased_to_bottom", "biasedToBottom" -> {
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
}
