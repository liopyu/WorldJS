package net.liopyu.worldjs.api;

import com.google.gson.JsonObject;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.liopyu.worldjs.events.JsonDataEventJS;
import net.liopyu.worldjs.utils.DataUtils;
import net.minecraft.resources.ResourceLocation;

public interface IAdditionalConfiguredFeatureMethodHolder {

    @HideFromJS
    default void add(ResourceLocation id, JsonObject data) {
        final JsonDataEventJS event = DataUtils.getCurrentJsonDataEventJS();
        if (event != null) {
            event.add(id, data);
        }
    }
}
