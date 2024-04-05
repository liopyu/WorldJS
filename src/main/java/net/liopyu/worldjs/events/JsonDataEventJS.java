package net.liopyu.worldjs.events;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.event.EventResult;
import dev.latvian.mods.kubejs.script.data.DataPackEventJS;
import dev.latvian.mods.kubejs.typings.Generics;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.liopyu.worldjs.api.ICFeatureMethodHolder;
import net.liopyu.worldjs.utils.DataUtils;
import net.liopyu.worldjs.utils.PlacedFeatureBuilder;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class JsonDataEventJS extends EventJS {

    @HideFromJS
    public static final String NAME_DESC = "The name of the feature, the namespace will default to 'worldjs' if none is provided";

    private final DataPackEventJS parent;

    public JsonDataEventJS(DataPackEventJS parent) {
        this.parent = parent;
        DataUtils.setJde(this);
    }

    /**
     * Allows for
     * <pre>{@code
     * WorldJSEvents.worldgenData(event => {
     *     let { tfc, ae2 } = event.mods;
     *     tfc.soilDisc(<blah blah blah>)
     *     ae2.meteor(<blah blah blah>)
     * }
     * }</pre>
     */
    @Generics(String.class)
    public ImmutableMap<String, ICFeatureMethodHolder> getMods() {
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
    public void finishFeature(String name, String type, JsonObject config) {
        final JsonObject json = new JsonObject();
        json.addProperty("type", type);
        json.add("config", config);
        add(DataUtils.configuredFeatureName(name), json);
    }

    @HideFromJS
    public void finishFeature(String name, JsonObject configured, Consumer<PlacedFeatureBuilder> placement) {
        add(DataUtils.configuredFeatureName(name), configured);
        add(DataUtils.placedFeatureName(name), Util.make(new PlacedFeatureBuilder(name), placement).toJson());
    }

    @Info(value = "Creates a configured feature of the given type with the given config and the matching placed feature", params = {
            @Param(name = "name", value = NAME_DESC),
            @Param(name = "type", value = "The type of configured feature to create"),
            @Param(name = "featureConfig", value = "The config json object for the feature"),
            @Param(name = "placement", value = "The placement properties")
    })
    @Generics(PlacedFeatureBuilder.class)
    public void genericFeature(String name, String type, JsonObject featureConfig, Consumer<PlacedFeatureBuilder> placement) {
        finishFeature(name, type, featureConfig, placement);
    }

    @Info(value = "Creates a configured feature of the given type with the given config and no placed feature", params = {
            @Param(name = "name", value = NAME_DESC),
            @Param(name = "type", value = "The type of configured feature to create"),
            @Param(name = "featureConfig", value = "The config json object for the feature")
    })
    public void genericFeature(String name, String type, JsonObject featureConfig) {
        finishFeature(name, type, featureConfig);
    }

    @Info(value = "Creates a placed feature with the given name which places a configured feature with the same name", params = {
            @Param(name = "name", value = "The name of the placed feature and the ")
    })
    @Generics(PlacedFeatureBuilder.class)
    public void placedFeature(String name, Consumer<PlacedFeatureBuilder> placement) {
        placedFeature(name, name, placement);
    }

    @Generics(PlacedFeatureBuilder.class)
    public void placedFeature(String name, String configuredFeatureToPlace, Consumer<PlacedFeatureBuilder> placement) {
        add(DataUtils.placedFeatureName(name), Util.make(new PlacedFeatureBuilder(configuredFeatureToPlace), placement).toJson());
    }

    @Info(value = "Creates a configured feature of type `minecraft:no_op`", params = {
            @Param(name = "name", value = NAME_DESC)
    })
    public void noOp(String name) {
        final JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:no_op");
        json.add("config", new JsonObject());
        add(DataUtils.configuredFeatureName(name), json);
    }

    @Override
    protected void afterPosted(EventResult result) {
        super.afterPosted(result);
        DataUtils.setJde(null);
    }
}
