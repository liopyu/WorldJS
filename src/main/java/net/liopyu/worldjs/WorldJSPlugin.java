package net.liopyu.worldjs;

import dev.architectury.platform.Platform;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.bindings.event.ServerEvents;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.ClassFilter;
import net.liopyu.worldjs.events.EventHandlers;
import net.liopyu.worldjs.events.forge.AddCFeatureMethodsEvent;
import net.liopyu.worldjs.events.forge.AddPFeatureMethodsEvent;
import net.liopyu.worldjs.internal.tests.TestCFeatureMethodHolder;
import net.liopyu.worldjs.internal.tests.TestPFeatureMethodHolder;
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
        if (event.getType() == ScriptType.SERVER) {
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
}
