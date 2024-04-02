package net.liopyu.worldjs.events.forge;

import com.google.common.collect.ImmutableMap;
import net.liopyu.worldjs.api.IAdditionalPlacedFeatureMethodHolder;
import net.minecraftforge.eventbus.api.Event;

/**
 * An event which allows for methods to be added to {@link net.liopyu.worldjs.utils.PlacedFeatureBuilder PlacedFeatureBuilder} without mixins
 */
public class AddPlacedFeatureMethods extends Event {

    private final ImmutableMap.Builder<String, IAdditionalPlacedFeatureMethodHolder> methods;

    public AddPlacedFeatureMethods(ImmutableMap.Builder<String, IAdditionalPlacedFeatureMethodHolder> mapBuilder) {
        this.methods = mapBuilder;
    }

    public void add(String mod, IAdditionalPlacedFeatureMethodHolder methods) {
        this.methods.put(mod, methods);
    }
}
