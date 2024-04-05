package net.liopyu.worldjs.internal.tests;

import com.google.gson.JsonObject;
import net.liopyu.worldjs.api.ICFeatureMethodHolder;

/**
 * An internal class for testing {@link net.liopyu.worldjs.events.forge.AddCFeatureMethodsEvent AddCFeatureMethodsEvent}
 */
public enum TestCFeatureMethodHolder implements ICFeatureMethodHolder {
    INSTANCE;

    public void test(String name) {
        finishFeature(name, "minecraft:no_op", new JsonObject(), p -> {});
    }
}
