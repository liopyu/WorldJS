package net.liopyu.worldjs.api;

import com.google.gson.JsonObject;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.liopyu.worldjs.WorldJS;
import net.liopyu.worldjs.events.JsonDataEventJS;
import net.liopyu.worldjs.utils.DataUtils;
import net.liopyu.worldjs.utils.PlacedFeatureBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public interface ICFeatureMethodHolder {

    /**
     * Adds the given json object to the virtual datapack, it is recommended that one of the <b>#finishFeature</b>
     * ({@link #finishFeature(String, String, JsonObject) 1}, {@link #finishFeature(String, String, JsonObject, Consumer) 2}, {@link #finishFeature(String, JsonObject, Consumer) 3})
     * methods be used over this as they automatically place files into the correct placed/configured feature folders
     * @param id The fully qualified path of the added data
     * @param data the data to be added to the pack
     */
    @HideFromJS
    default void add(ResourceLocation id, JsonObject data) {
        final JsonDataEventJS event = DataUtils.getCurrentJsonDataEventJS();
        if (event != null) {
            event.add(id, data);
        } else {
            // Put a breakpoint here
            WorldJS.LOGGER.warn("Attempted to add a feature json while there was no active {}: {} - {} | Skipping", JsonDataEventJS.class.getName(), id, data);
        }
    }

    /**
     * See {@link JsonDataEventJS#genericFeature(String, String, JsonObject, Consumer)}
     *
     * Finishes a matching pair of configured an placed features
     * @param name The unqualified name of the features (e.g. 'kubejs:tree/spectral', 'spectral_mass'), usually passed from a matching param in a JS visible method
     * @param type The registered configured feature type (e.g. 'minecraft:no_op')
     * @param config The config object of the feature being built
     * @param placement the definition of the <i>configured</i> feature's matching <i>placed</i> feature, if you do not wish to have a matching placed feature generated use {@link #finishFeature(String, String, JsonObject)}
     */
    @HideFromJS
    default void finishFeature(String name, String type, JsonObject config, Consumer<PlacedFeatureBuilder> placement) {
        final JsonDataEventJS event = DataUtils.getCurrentJsonDataEventJS();
        if (event != null) {
            event.finishFeature(name, type, config, placement);
        } else {
            // Put a breakpoint here
            WorldJS.LOGGER.warn("Attempted to add a feature json while there was no active {}: {} - Type: {} Config: {} | Skipping!", JsonDataEventJS.class.getName(), name, type, config);
        }
    }

    /**
     * Finishes a matching pair of configured and placed features
     * @param name The unqualified name of the features (e.g. 'kubejs:test', 'tree_2'), usually passed from a matching param in a JS visible method
     * @param configured The json data of the configured feature (e.g. 'minecraft:fossil')
     * @param placement The definition of the configured feature's <i>placed</i> feature, if you do not wish to have a matching placed feature generated use {@link #finishFeature(String, String, JsonObject)}
     */
    @HideFromJS
    default void finishFeature(String name, JsonObject configured, Consumer<PlacedFeatureBuilder> placement) {
        final JsonDataEventJS event = DataUtils.getCurrentJsonDataEventJS();
        if (event != null) {
            event.finishFeature(name, configured, placement);
        } else {
            // Put a breakpoint here
            WorldJS.LOGGER.warn("Attempted to add a feature json while there was no active {}: {} - {} | Skipping!", JsonDataEventJS.class.getName(), name, configured);
        }
    }

    /**
     * See {@link JsonDataEventJS#genericFeature(String, String, JsonObject)}
     * <br><br>
     * Finishes only a <strong>configured</strong> feature
     * @param name The unqualified name of the configured feature (e.g. 'kubejs:configured', 'not_placed'), usually passed from a matching param in a JS visible method
     * @param type The registered configured feature type (e.g. 'minecraft:geode')
     * @param config The config object of the feature being built
     */
    @HideFromJS
    default void finishFeature(String name, String type, JsonObject config) {
        final JsonDataEventJS event = DataUtils.getCurrentJsonDataEventJS();
        if (event != null) {
            event.finishFeature(name, type, config);
        } else {
            // Put a breakpoint here
            WorldJS.LOGGER.warn("Attempted to add a configured feature json while there was no active {}: {} - Type: {} Config: {} | Skipping!", JsonDataEventJS.class.getName(), name, type, config);
        }
    }
}
