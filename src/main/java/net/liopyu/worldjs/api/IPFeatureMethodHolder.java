package net.liopyu.worldjs.api;

import com.google.gson.JsonObject;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.liopyu.worldjs.WorldJS;
import net.liopyu.worldjs.utils.DataUtils;
import net.liopyu.worldjs.utils.PlacedFeatureBuilder;

public interface IPFeatureMethodHolder {

    /**
     * Adds the given json object to the {@link PlacedFeatureBuilder}'s list of placement modifiers
     * @param json The placement modifier, must include the {@code type} value
     */
    @HideFromJS
    default void addJson(JsonObject json) {
        final PlacedFeatureBuilder builder = DataUtils.getCurrentPlacedFeatureBuilder();
        if (builder != null) {
            builder.addJsonPlacement(json);
        } else {
            // Put a breakpoint here
            WorldJS.LOGGER.warn("Attempted to add a json placement while there was no active {}: {}", PlacedFeatureBuilder.class.getName(), json);
        }
    }

    /**
     * Adds a new placement modifier to the {@link PlacedFeatureBuilder}'s list of placement modifiers as a json object shaped like: <b>{"type": <i>{@code type}</i>}</b>
     * @param type The type of the placement modifier
     */
    @HideFromJS
    default void addSimple(String type) {
        final PlacedFeatureBuilder builder = DataUtils.getCurrentPlacedFeatureBuilder();
        if (builder != null) {
            builder.addSimplePlacement(type);
        } else {
            // Put a breakpoint here
            WorldJS.LOGGER.warn("Attempted to add a simple placement while there was no active {}: {}", PlacedFeatureBuilder.class.getName(), type);
        }
    }
}
