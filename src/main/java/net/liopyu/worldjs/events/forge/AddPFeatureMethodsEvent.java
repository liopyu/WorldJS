package net.liopyu.worldjs.events.forge;

import com.google.common.collect.ImmutableMap;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import net.liopyu.worldjs.api.IPFeatureMethodHolder;
import net.liopyu.worldjs.utils.PlacedFeatureBuilder;
import net.minecraftforge.eventbus.api.Event;

/**
 * An event which allows for methods to be added to {@link net.liopyu.worldjs.utils.PlacedFeatureBuilder PlacedFeatureBuilder} without mixins
 * <br><br>
 * Allows for placement modifiers from other mods to be added to the JS event which can be accessed through {@link PlacedFeatureBuilder#getMods()}
 * <br><br>
 * This event will only fire once, on the first time a script invokes <b>PlacedFeatureBuilder.getMods()</b>, and the results will be cached
 * <br><br>
 * This event fires on the {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS Forge EventBus} and is recommended that the event listener be registered in your plugin's {@link KubeJSPlugin#init()} method
 */
public class AddPFeatureMethodsEvent extends Event {

    private final ImmutableMap.Builder<String, IPFeatureMethodHolder> methods;

    public AddPFeatureMethodsEvent(ImmutableMap.Builder<String, IPFeatureMethodHolder> mapBuilder) {
        this.methods = mapBuilder;
    }

    public void add(String mod, IPFeatureMethodHolder methods) {
        this.methods.put(mod, methods);
    }
}
