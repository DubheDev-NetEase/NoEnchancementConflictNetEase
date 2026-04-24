package dev.dubhe.enchance.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AnvilMenu.class)
abstract class AnvilMenuMixin {
    @Redirect(
        method = "createResultInternal",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;supportsEnchantment(Lnet/minecraft/core/Holder;)Z"
        )
    )
    private boolean createResultInternal1(ItemStack instance, Holder<Enchantment> holder) {
        return true;
    }

    @Redirect(
        method = "createResultInternal",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/enchantment/Enchantment;areCompatible(Lnet/minecraft/core/Holder;Lnet/minecraft/core/Holder;)Z"
        )
    )
    private boolean createResultInternal2(Holder<Enchantment> first, Holder<Enchantment> second) {
        return true;
    }

    @Redirect(
        method = "createResultInternal",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/inventory/DataSlot;set(I)V"
        )
    )
    private void createResultInternal3(DataSlot instance, int i) {
        instance.set(Math.min(i, 1));
    }
}
