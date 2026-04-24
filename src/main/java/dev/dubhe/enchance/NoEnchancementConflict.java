package dev.dubhe.enchance;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(NoEnchancementConflict.MODID)
public class NoEnchancementConflict {
    public static final String MODID = "no_enchancement_conflict";
    public static final Logger LOGGER = LogUtils.getLogger();

    public NoEnchancementConflict(IEventBus modEventBus, ModContainer modContainer) {
    }
}
