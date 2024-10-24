package kr.blugon.displayeditorfabric.client.commands

import com.mojang.brigadier.context.CommandContext
import kr.blugon.displayeditorfabric.client.*
import kr.blugon.displayeditorfabric.client.api.*
import kr.blugon.displayeditorfabric.client.api.display.itemStack
import kr.blugon.kotlinbrigadierfabric.BrigadierNode
import kr.blugon.kotlinbrigadierfabric.get
import kr.blugon.kotlinbrigadierfabric.getValue
import net.minecraft.command.EntitySelector
import net.minecraft.command.argument.BlockPosArgumentType.blockPos
import net.minecraft.command.argument.EntityArgumentType.entity
import net.minecraft.command.argument.ItemSlotArgumentType.itemSlot
import net.minecraft.command.argument.ItemStackArgument
import net.minecraft.command.argument.ItemStackArgumentType.itemStack
import net.minecraft.command.argument.PosArgument
import net.minecraft.command.argument.RotationArgumentType.rotation
import net.minecraft.command.argument.Vec3ArgumentType.vec3
import net.minecraft.entity.*
import net.minecraft.entity.decoration.DisplayEntity.ItemDisplayEntity
import net.minecraft.entity.decoration.ItemFrameEntity
import net.minecraft.entity.vehicle.VehicleInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.server.command.ServerCommandSource


fun Entity.getItem(slot: Int): ItemStack? {
    val equipmentSlot = when(slot) {
        98 -> EquipmentSlot.MAINHAND
        99 -> EquipmentSlot.OFFHAND
        100 -> EquipmentSlot.FEET
        101 -> EquipmentSlot.LEGS
        102 -> EquipmentSlot.CHEST
        103 -> EquipmentSlot.HEAD
        105 -> EquipmentSlot.BODY
        else -> null
    }

    return when(this) {
        is ItemFrameEntity -> return if(slot == 0) this.heldItemStack else null
        is ItemDisplayEntity -> return if(slot == 0) this.itemStack else null
        is ItemEntity -> return if(slot == 0) this.stack else null
        is VehicleInventory -> return this.inventory.getOrNull(slot)
        is InventoryOwner -> return this.inventory.getStack(slot-300)
        is LivingEntity -> {
            if(equipmentSlot != null) this.getEquippedStack(equipmentSlot)
            else null
        }
        else -> null
    }
}


fun ServerCommandSource.editItem(item: ItemStack, entities: List<Entity>) {
    entities.forEach { display ->
        (display as ItemDisplayEntity).itemStack = item
    }
    this.sendFeedback("아이템 표시 ${entities.size}개의 아이템을 ".literal.append(item.displayName()).append("(으)로 바꿨습니다".literal))
}

fun BrigadierNode.thenItemDisplayEdit() {
    then("from") {
        then("entity") {
            then("target" to entity()) {
                then("slot" to itemSlot()) {
                    executes {
                        val entities = it.getEntities("entities")
                        if(!entities.isItemDisplayList(this)) return@executes
                        val slot = it.get<Integer>("slot").toInt()
                        val target: EntitySelector by it
                        val entity = target.getEntity(this)?: return@executes this.sendFeedback("개체를 찾을 수 없습니다".literal.color(NamedTextColor.RED))

                        val item = entity.getItem(slot)?: return@executes this.sendFeedback("원본에게 $slot 슬롯이 없습니다".literal.color(NamedTextColor.RED))
                        editItem(item, entities)
                    }
                }
            }
        }
        then("block") {
            then("position" to blockPos()) {
                then("slot" to itemSlot()) {
                    executes {
                        val entities = it.getEntities("entities")
                        if(!entities.isItemDisplayList(this)) return@executes
                        val position = it.get<PosArgument>("position").toAbsoluteBlockPos(this)
                        val slot = it.get<Integer>("slot").toInt()
                        val block = world.getBlockEntity(position)
                        if(block !is Inventory) return@executes this.sendFeedback("원본 위치 ${position.x}, ${position.y}, ${position.z}은(는) 용기가 아닙니다".literal.color(NamedTextColor.RED))
                        if(block.size() <= slot) return@executes this.sendFeedback("원본에게 $slot 슬롯이 없습니다".literal.color(NamedTextColor.RED))
                        val item = block.getStack(slot)?: return@executes this.sendFeedback("원본에게 $slot 슬롯이 없습니다".literal.color(NamedTextColor.RED))
                        editItem(item, entities)
                    }
                }
            }
        }
    }
    then("with") {
        then("item" to itemStack(registryAccess)) {
            executes {
                val entities = it.getEntities("entities")
                if(!entities.isItemDisplayList(this)) return@executes
                val item = ItemStack(it.get<ItemStackArgument>("item").item)
                editItem(item, entities)
            }
        }
    }
}



private fun ServerCommandSource.spawn(item: ItemStack, location: Location) {
    location.spawnEntity<ItemDisplayEntity>(EntityType.ITEM_DISPLAY) {
        it.itemStack = item
    }
    this.sendMessage("새로운 아이템 표시를 소환했습니다".literal)
}

private fun BrigadierNode.runWithPosition(run: ServerCommandSource.(CommandContext<ServerCommandSource>, Location?) -> Unit) {
    this.executes {
        run(this, it, null)
    }
    this.then("location" to vec3()) {
        this.then("rotation" to rotation()) {
            executes {
                val position = it.get<PosArgument>("location").getPos(this)
                val rotation = it.get<PosArgument>("rotation").getRotation(this)
                run(this, it, Location(world, position, rotation))
            }
        }
        executes {
            val position = it.get<PosArgument>("location").getPos(this)
            run(this, it, Location(world, position))
        }
    }
}

fun BrigadierNode.thenItemDisplaySpawn() {
    then("from") {
        then("entity") {
            then("target" to entity()) {
                then("slot" to itemSlot()) {
                    runWithPosition { it, location ->
                        val slot = it.get<Integer>("slot").toInt()
                        val target: EntitySelector by it
                        val entity = target.getEntity(this)?: return@runWithPosition this.sendFeedback("개체를 찾을 수 없습니다".literal.color(NamedTextColor.RED))

                        val item = entity.getItem(slot)?: return@runWithPosition this.sendFeedback("원본에게 $slot 슬롯이 없습니다".literal.color(NamedTextColor.RED))
                        spawn(item, location?: this.location)
                    }
                }
            }
        }
        then("block") {
            then("position" to blockPos()) {
                then("slot" to itemSlot()) {
                    runWithPosition { it, location ->
                        val position = it.get<PosArgument>("position").toAbsoluteBlockPos(this)
                        val slot = it.get<Integer>("slot").toInt()
                        val block = world.getBlockEntity(position)
                        if(block !is Inventory) return@runWithPosition this.sendFeedback("원본 위치 ${position.x}, ${position.y}, ${position.z}은(는) 용기가 아닙니다".literal.color(NamedTextColor.RED))
                        if(block.size() <= slot) return@runWithPosition this.sendFeedback("원본에게 $slot 슬롯이 없습니다".literal.color(NamedTextColor.RED))
                        val item = block.getStack(slot)?: return@runWithPosition this.sendFeedback("원본에게 $slot 슬롯이 없습니다".literal.color(NamedTextColor.RED))
                        spawn(item, location?: this.location)
                    }
                }
            }
        }
    }
    then("with") {
        then("item" to itemStack(registryAccess)) {
            runWithPosition { it, location ->
                val item = ItemStack(it.get<ItemStackArgument>("item").item)
                spawn(item, location?: this.location)
            }
        }
    }
}