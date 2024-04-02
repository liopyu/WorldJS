package net.liopyu.worldjs.api;

import com.google.gson.JsonObject;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.liopyu.worldjs.utils.DataUtils;
import net.liopyu.worldjs.utils.PlacedFeatureBuilder;

public interface IAdditionalPlacedFeatureMethodHolder {

    @HideFromJS
    default void add(JsonObject json) {
        final PlacedFeatureBuilder builder = DataUtils.getCurrentPlacedFeatureBuilder();
        if (builder != null) {
            builder.addJsonPlacement(json);
        }
    }
}
