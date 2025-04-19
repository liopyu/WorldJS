package net.liopyu.worldjs;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

import java.util.function.Consumer;

@Mod(WorldJS.MODID)
public class WorldJS {
    public static final String MODID = "worldjs";

    public static final Logger LOGGER = LogUtils.getLogger();

    // TODO: No, this is terrible. I don't care what has to be done, do better
    private static Consumer<ImmutableMap.Builder<String, Object>> modBindings = b -> {};

    public WorldJS() {
    }

    /**
     * Allows addons to add custom objects to our bindings
     * <br><br>
     * Done here instead of as a forge event because (startup) scripts are read before Forge's main event bus is started
     * @param mod The mod name to add this binding under
     * @param binding The object to be added as a binding, suggested to be the instance of a single value enum class
     */
    public static void addModBinding(String mod, Object binding) {
        modBindings = modBindings.andThen(b -> b.put(mod, binding));
    }

    @ApiStatus.Internal
    public static ImmutableMap<String, Object> getModBindings() {
        final ImmutableMap.Builder<String, Object> builder = new ImmutableMap.Builder<>();
        modBindings.accept(builder);
        return builder.build();
    }
}
