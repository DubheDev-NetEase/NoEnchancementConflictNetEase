package dev.dubhe.enchant.util;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RepairCostHelper {
    public static @NotNull Object getRepairCost(ItemStack instance, DataComponentType<?> dataComponentType, Object object) {
        if (dataComponentType == DataComponents.REPAIR_COST) return 0;
        return instance.getOrDefault(dataComponentType, object);
    }

    public static void setRepairCost(@NotNull DataSlot slot, int i) {
        slot.set(Math.min(i, 1));
    }
}
