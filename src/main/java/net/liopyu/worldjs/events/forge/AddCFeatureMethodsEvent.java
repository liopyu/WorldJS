package net.liopyu.worldjs.events.forge;

import com.google.common.collect.ImmutableMap;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import net.liopyu.worldjs.api.ICFeatureMethodHolder;
import net.liopyu.worldjs.events.JsonDataEventJS;
import net.minecraftforge.eventbus.api.Event;

/**
 * An event which enables methods to be added to {@link JsonDataEventJS} without mixins
 * <br><br>
 * Allows for configured feature types from other mods to be added to the JS event which can be accessed through {@link JsonDataEventJS#getMods()}
 * <br><br>
 * This event will fire only once, on the first time a script invokes <b>JsonDataEventJS.getMods()</b>, and the results will be cached
 * <br><br>
 * This event fires on the {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS Forge EventBus} and is recommended that the event listener be registered in your plugin's {@link KubeJSPlugin#init()} method
 */
public class AddCFeatureMethodsEvent extends Event {

    private final ImmutableMap.Builder<String, ICFeatureMethodHolder> methods;

    public AddCFeatureMethodsEvent(ImmutableMap.Builder<String, ICFeatureMethodHolder> mapBuilder) {
        this.methods = mapBuilder;
    }

    public void add(String mod, ICFeatureMethodHolder methodHolder) {
        methods.put(mod, methodHolder);
    }
}
