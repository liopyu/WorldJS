package net.liopyu.worldjs.internal.tests;

import net.liopyu.worldjs.api.IPFeatureMethodHolder;
import net.liopyu.worldjs.events.forge.AddPFeatureMethodsEvent;

/**
 * An internal class for testing {@link AddPFeatureMethodsEvent AddPFeatureMethodsEvent}
 */
public enum TestPFeatureMethodHolder implements IPFeatureMethodHolder {
    INSTANCE;

    public void test(String type) {
        addSimple(type);
    }
}
