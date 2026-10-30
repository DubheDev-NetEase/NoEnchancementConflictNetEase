package dev.dubhe.enchance.gametest;

import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(EnchantmentTests.MOD_ID)
public final class EnchantmentTests {
    static final String MOD_ID = "no_enchancement_conflict_tests";

    public EnchantmentTests(IEventBus bus) {
        var functions = DeferredRegister.<Consumer<GameTestHelper>>create(Registries.TEST_FUNCTION, MOD_ID);
        functions.register("unrestricted_item", () -> EnchantmentTests::unrestrictedItem);
        functions.register("conflicting_enchantments", () -> EnchantmentTests::conflictingEnchantments);
        functions.register("high_level_merge", () -> EnchantmentTests::highLevelMerge);
        functions.register("repair_cost", () -> EnchantmentTests::repairCost);
        functions.register(bus);
        bus.addListener(this::registerTests);
    }

    private static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(MOD_ID, name);
    }

    private void registerTests(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(id("normal"));
        for (String name : new String[]{"unrestricted_item", "conflicting_enchantments", "high_level_merge", "repair_cost"}) {
            event.registerTest(id(name), new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION, id(name)),
                new TestData<>(environment, id("empty"), 100, 0, true)));
        }
    }

    private static Holder<Enchantment> enchantment(GameTestHelper helper, ResourceKey<Enchantment> key) {
        return helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
    }

    private static AnvilMenu combine(Player player, ItemStack first, ItemStack second) {
        var menu = new AnvilMenu(1, player.getInventory());
        menu.getSlot(0).set(first);
        menu.getSlot(1).set(second);
        menu.createResult();
        return menu;
    }

    private static ItemStack book(Holder<Enchantment> enchantment, int level) {
        var stack = new ItemStack(Items.ENCHANTED_BOOK);
        stack.enchant(enchantment, level);
        return stack;
    }

    private static void unrestrictedItem(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var sharpness = enchantment(helper, Enchantments.SHARPNESS);
        var menu = combine(player, new ItemStack(Items.STICK), book(sharpness, 2));
        var result = menu.getSlot(2).getItem();
        helper.assertTrue(result.is(Items.STICK) && EnchantmentHelper.getItemEnchantmentLevel(sharpness, result) == 2,
            Component.literal("铁砧未能为木棍应用锋利附魔"));
        helper.succeed();
    }

    private static void conflictingEnchantments(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var sharpness = enchantment(helper, Enchantments.SHARPNESS);
        var smite = enchantment(helper, Enchantments.SMITE);
        var sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.enchant(sharpness, 3);
        var menu = combine(player, sword, book(smite, 2));
        var result = menu.getSlot(2).getItem();
        helper.assertTrue(!result.isEmpty()
                && EnchantmentHelper.getItemEnchantmentLevel(sharpness, result) == 3
                && EnchantmentHelper.getItemEnchantmentLevel(smite, result) == 2,
            Component.literal("冲突附魔未能同时保留"));
        helper.succeed();
    }

    private static void highLevelMerge(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var sharpness = enchantment(helper, Enchantments.SHARPNESS);
        var sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.enchant(sharpness, 254);
        var menu = combine(player, sword, book(sharpness, 254));
        var result = menu.getSlot(2).getItem().copy();
        helper.assertTrue(!result.isEmpty() && EnchantmentHelper.getItemEnchantmentLevel(sharpness, result) == 255,
            Component.literal("相同高等级附魔未能合并至 255 级"));
        menu = combine(player, result, book(sharpness, 255));
        helper.assertTrue(EnchantmentHelper.getItemEnchantmentLevel(sharpness, menu.getSlot(2).getItem()) == 255,
            Component.literal("附魔等级超过上限或被降低"));
        helper.succeed();
    }

    private static void repairCost(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.experienceLevel = 10;
        var sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.setDamageValue(900);
        sword.set(DataComponents.REPAIR_COST, 100);
        var menu = combine(player, sword, new ItemStack(Items.DIAMOND, 3));
        var result = menu.getSlot(2).getItem().copy();
        helper.assertTrue(!result.isEmpty() && result.getDamageValue() < sword.getDamageValue(),
            Component.literal("高历史费用的物品无法修复"));
        helper.assertTrue(menu.getSlot(2).mayPickup(player), Component.literal("铁砧结果无法取出"));
        menu.getSlot(2).onTake(player, result);
        helper.assertTrue(player.experienceLevel == 9, Component.literal("铁砧操作未按 1 级经验扣费"));
        helper.succeed();
    }
}
