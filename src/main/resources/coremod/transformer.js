var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var IntInsnNode = Java.type('org.objectweb.asm.tree.IntInsnNode');

var redirectList = []

function redirect(nodeName, targetClass, targetMethod, targetMethodDesc, targetOwner, targetName, targetDesc, owner, name, desc) {
    redirectList.push(
        {
            nodeName: nodeName,
            targetClass: targetClass,
            targetMethod: targetMethod,
            targetMethodDesc: targetMethodDesc,
            targetOwner: targetOwner,
            targetName: targetName,
            targetDesc: targetDesc,
            owner: owner,
            name: name,
            desc: desc
        }
    )
}

function redirectTransformer(record, classNode) {
    classNode.methods.forEach(function (methodNode) {
        ASMAPI.log("INFO", "Methods: {} {};{}", classNode.name, methodNode.name, methodNode.desc)
        if (methodNode.name !== record.targetMethod) return;
        if (methodNode.desc !== record.targetMethodDesc) return;
        methodNode.instructions.forEach(function (node) {
            if (node.getType() !== 5) return;
            ASMAPI.log("INFO", "Instructions: {};{};{}", node.owner, node.name, node.desc)
            if (node.owner !== record.targetOwner) return;
            if (node.name !== record.targetName) return;
            if (node.desc !== record.targetDesc) return;
            ASMAPI.log(
                "INFO",
                "Redirect: {};{};{};{} to {};{};{};{}",
                node.getOpcode(), node.owner, node.name, node.desc,
                Opcodes.INVOKESTATIC, record.owner, record.name, record.desc
            )
            node.setOpcode(Opcodes.INVOKESTATIC)
            node.owner = record.owner;
            node.name = record.name;
            node.desc = record.desc;
        })
    });
}

function initializeCoreMod() {
    redirect(
        "anvil_menu_can_enchant_redirect",
        "net.minecraft.world.inventory.AnvilMenu",
        "createResult",
        "()V",
        "net/minecraft/world/item/enchantment/Enchantment",
        "canEnchant",
        "(Lnet/minecraft/world/item/ItemStack;)Z",
        "dev/dubhe/enchant/util/CanEnchantHelper",
        "canEnchant",
        "(Lnet/minecraft/world/item/enchantment/Enchantment;Lnet/minecraft/world/item/ItemStack;)Z"
    );
    redirect(
        "anvil_menu_are_compatible_redirect",
        "net.minecraft.world.inventory.AnvilMenu",
        "createResult",
        "()V",
        "net/minecraft/world/item/enchantment/Enchantment",
        "areCompatible",
        "(Lnet/minecraft/core/Holder;Lnet/minecraft/core/Holder;)Z",
        "dev/dubhe/enchant/util/CanEnchantHelper",
        "areCompatible",
        "(Lnet/minecraft/core/Holder;Lnet/minecraft/core/Holder;)Z"
    );

    redirect(
        "anvil_menu_create_result_redirect",
        "net.minecraft.world.inventory.AnvilMenu",
        "createResult",
        "()V",
        "net/minecraft/world/inventory/DataSlot",
        "set",
        "(I)V",
        "dev/dubhe/enchant/util/RepairCostHelper",
        "setRepairCost",
        "(Lnet/minecraft/world/inventory/DataSlot;I)V"
    );
    var transformers = {}
    redirectList.forEach(function (record) {
        transformers[record.nodeName] = {
            "target": {
                "type": "CLASS",
                "name": record.targetClass
            },
            "transformer": function (node) {
                redirectTransformer(record, node)
            }
        };
    })
    transformers["enchantment_definition_max_level_return"] = {
        "target": {
            "type": "CLASS",
            "name": "net.minecraft.world.item.enchantment.Enchantment$EnchantmentDefinition"
        },
        "transformer": function (classNode) {
            classNode.methods.forEach(function (methodNode) {
                ASMAPI.log("INFO", "Methods: {} {};{}", classNode.name, methodNode.name, methodNode.desc)
                if (methodNode.name !== "maxLevel") return;
                if (methodNode.desc !== "()I") return;
                var insnNode;
                methodNode.instructions.forEach(function (node) {
                    if (node.getOpcode() !== Opcodes.GETFIELD) return;
                    insnNode = node;
                });
                methodNode.instructions.insert(insnNode, new IntInsnNode(Opcodes.SIPUSH, 255))
                methodNode.instructions.remove(insnNode)
            });
        }
    }
    return transformers
}