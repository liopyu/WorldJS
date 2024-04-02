package net.liopyu.worldjs.utils;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.liopyu.worldjs.api.IAdditionalPlacedFeatureMethodHolder;

import java.util.ArrayList;
import java.util.List;

public class PlacedFeatureBuilder {

    public transient final String feature;
    public transient final List<JsonObject> placements;

    public PlacedFeatureBuilder(String name) {
        final String[] loc = DataUtils.normalizeName(name);
        feature = loc[0] + ":" + loc[1];
        placements = new ArrayList<>();
        DataUtils.setPfb(this);
    }

    public ImmutableMap<String, IAdditionalPlacedFeatureMethodHolder> getMods() {
        return DataUtils.modPlacements.get();
    }

    public void addJsonPlacement(JsonObject json) {
        placements.add(json);
    }

    public void addSimplePlacement(String type) {
        final JsonObject json = new JsonObject();
        json.addProperty("type", type);
        placements.add(json);
    }

    public JsonObject toJson() {
        DataUtils.setPfb(null);
        final JsonObject json = new JsonObject();
        json.addProperty("feature", feature);
        final JsonArray array = new JsonArray(placements.size());
        for (JsonObject obj : placements) {
            array.add(obj);
        }
        json.add("placement", array);
        return json;
    }
}
