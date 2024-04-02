package net.liopyu.worldjs.events;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.script.data.DataPackEventJS;
import net.liopyu.worldjs.WorldJS;

public class EventHandlers {

    public static final EventGroup WorldJSEvents = EventGroup.of("WorldJSEvents");

    public static final EventHandler jsonData = WorldJSEvents.server("worldgenData", () -> JsonDataEventJS.class);

    public static Object handleJsonDataEvent(EventJS event) {
        if (event instanceof DataPackEventJS data) {
            jsonData.post(new JsonDataEventJS(data));
        } else {
            WorldJS.LOGGER.error("WorldJS JSON worldgen event failed to fire due to provided parent event not being the correct class!");
        }
        return null;
    }
}
