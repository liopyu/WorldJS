package net.liopyu.worldjs.utils;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class GeneralUtils {

    @Nullable
    public static Object getFirstOfKeys(Map<?, ?> map, Object... keys) {
        for (Object key : keys) {
            if (map.containsKey(key)) {
                return map.get(key);
            }
        }
        return null;
    }

    @Nullable
    public static Object getFirstKeyMapHas(Map<?, ?> map, Object... keys) {
        for (Object key : keys) {
            if (map.containsKey(key)) {
                return key;
            }
        }
        return null;
    }
}
