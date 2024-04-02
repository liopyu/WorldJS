package net.liopyu.worldjs.events.forge;

import com.google.common.collect.ImmutableMap;
import net.liopyu.worldjs.api.IAdditionalConfiguredFeatureMethodHolder;
import net.minecraftforge.eventbus.api.Event;

/**
 * An event which enables methods to be added to {@link net.liopyu.worldjs.events.JsonDataEventJS JsonDataEventJS} without mixins
 */
public class AddConfiguredFeatureMethods extends Event {

    private final ImmutableMap.Builder<String, IAdditionalConfiguredFeatureMethodHolder> methods;

    public AddConfiguredFeatureMethods(ImmutableMap.Builder<String, IAdditionalConfiguredFeatureMethodHolder> mapBuilder) {
        this.methods = mapBuilder;
    }

    public void add(String mod, IAdditionalConfiguredFeatureMethodHolder methodHolder) {
        methods.put(mod, methodHolder);
    }
}
