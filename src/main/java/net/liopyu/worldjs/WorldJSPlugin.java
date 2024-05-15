package net.liopyu.worldjs;

import dev.architectury.platform.Platform;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.bindings.event.ServerEvents;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.ClassFilter;
import dev.latvian.mods.rhino.util.wrap.TypeWrappers;
import net.liopyu.worldjs.events.EventHandlers;
import net.liopyu.worldjs.events.forge.AddCFeatureMethodsEvent;
import net.liopyu.worldjs.events.forge.AddPFeatureMethodsEvent;
import net.liopyu.worldjs.internal.tests.TestCFeatureMethodHolder;
import net.liopyu.worldjs.internal.tests.TestPFeatureMethodHolder;
import net.liopyu.worldjs.utils.WorldJSTypeWrappers;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RuleBasedBlockStateProvider;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;

public class WorldJSPlugin extends KubeJSPlugin {

    @Override
    public void init() {
        if (Platform.isDevelopmentEnvironment()) {
            MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, AddCFeatureMethodsEvent.class, event -> {
                event.add("test", TestCFeatureMethodHolder.INSTANCE);
            });
            MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, AddPFeatureMethodsEvent.class, event -> {
                event.add("test", TestPFeatureMethodHolder.INSTANCE);
            });
        }
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("WorldJS", WorldJSBindings.class);
        event.add("BlockStateProvider", BlockStateProvider.class);
        event.add("IntProvider", IntProvider.class);
        event.add("UniformInt", UniformInt.class);
        event.add("RuleBasedBlockStateProvider", RuleBasedBlockStateProvider.class);
        if (event.getType().isServer()) {
            // This is here because dumb stuff relating to the server script manager not existing until the server starts
            // Oh, the pain it took to get to here in KubeJS TFC, if only this method had been I dunno, documented!
            ServerEvents.HIGH_DATA.listenJava(ScriptType.SERVER, null, EventHandlers::handleJsonDataEvent);
        }
    }

    @Override
    public void registerClasses(ScriptType type, ClassFilter filter) {
        filter.allow("net.liopyu.worldjs");
        filter.deny(EventHandlers.class);
        filter.deny(WorldJSPlugin.class);
        filter.deny(WorldJS.class);
        filter.deny("net.liopyu.worldjs.internal");
        filter.deny("net.liopyu.worldjs.mixin"); // In case we ever do so
    }

    @Override
    public void registerEvents() {
        EventHandlers.WorldJSEvents.register();
    }

    @Override
    public void registerTypeWrappers(ScriptType type, TypeWrappers typeWrappers) {
        typeWrappers.register(FloatProvider.class, WorldJSTypeWrappers::floatProvider);
        typeWrappers.register(BlockState.class, WorldJSTypeWrappers::blockState);
        typeWrappers.register(BlockPredicate.class, WorldJSTypeWrappers::blockPredicate);
        typeWrappers.register(HeightProvider.class, WorldJSTypeWrappers::heightProvider);
        typeWrappers.register(VerticalAnchor.class, WorldJSTypeWrappers::verticalAnchor);
        typeWrappers.register(BlockStateProvider.class, WorldJSTypeWrappers::blockStateProvider);
        typeWrappers.register(NormalNoise.NoiseParameters.class, WorldJSTypeWrappers::noiseParameters);
        typeWrappers.register(FluidState.class, WorldJSTypeWrappers::fluidState);
    }
}
