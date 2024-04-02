package net.liopyu.worldjs.events;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.event.EventResult;
import dev.latvian.mods.kubejs.script.data.DataPackEventJS;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.liopyu.worldjs.api.IAdditionalConfiguredFeatureMethodHolder;
import net.liopyu.worldjs.utils.DataUtils;
import net.liopyu.worldjs.utils.PlacedFeatureBuilder;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class JsonDataEventJS extends EventJS {

    private final DataPackEventJS parent;

    public JsonDataEventJS(DataPackEventJS parent) {
        this.parent = parent;
        DataUtils.setJde(this);
    }

    /**
     * Should allow for
     * <pre>{@code
     * WorldJSEvents.worldgenData(event => {
     *     let { tfc, ae2 } = event.mods;
     *     tfc.soilDisc(<blah blah blah>)
     *     ae2.meteor(<blah blah blah>)
     * }
     * }</pre>
     */
    public ImmutableMap<String, IAdditionalConfiguredFeatureMethodHolder> getMods() {
        return DataUtils.modConfiguredFeatures.get();
    }

    @HideFromJS
    public void add(ResourceLocation id, JsonElement data) {
        parent.addJson(id, data);
    }

    @HideFromJS
    public void finishFeature(String name, String type, JsonObject config, Consumer<PlacedFeatureBuilder> placement) {
        final JsonObject json = new JsonObject();
        json.addProperty("type", type);
        json.add("config", config);
        finishFeature(name, json, placement);
    }

    @HideFromJS
    public void finishFeature(String name, JsonObject configured, Consumer<PlacedFeatureBuilder> placement) {
        add(DataUtils.configuredFeatureName(name), configured);
        add(DataUtils.placedFeatureName(name), Util.make(new PlacedFeatureBuilder(name), placement).toJson());
    }

    public void genericFeature(String name, String type, JsonObject featureConfig, Consumer<PlacedFeatureBuilder> placement) {
        finishFeature(name, type, featureConfig, placement);
    }

    @Override
    protected void afterPosted(EventResult result) {
        super.afterPosted(result);
        DataUtils.setJde(null);
    }
}
