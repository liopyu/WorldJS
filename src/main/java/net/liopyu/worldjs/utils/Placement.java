package net.liopyu.worldjs.utils;

import java.util.function.Consumer;

@FunctionalInterface
public interface Placement extends Consumer<PlacedFeatureBuilder> {

    void accept(PlacedFeatureBuilder placedFeatureBuilder);
}
