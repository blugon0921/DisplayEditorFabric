package kr.blugon.displayeditorfabric.client.commands

import com.mojang.brigadier.context.CommandContext
import kr.blugon.displayeditorfabric.client.api.*
import kr.blugon.displayeditorfabric.client.commands.position.thenPositionEdit
import kr.blugon.displayeditorfabric.client.commands.position.thenRotationEdit
import kr.blugon.displayeditorfabric.client.commands.transformation.thenTransformationEdit
import kr.blugon.kotlinbrigadierfabric.BrigadierNode
import kr.blugon.kotlinbrigadierfabric.get
import kr.blugon.kotlinbrigadierfabric.registerCommandHandlers
import net.minecraft.client.MinecraftClient
import net.minecraft.command.EntitySelector
import net.minecraft.command.argument.EntityArgumentType.entities
import net.minecraft.command.argument.EntityArgumentType.entity
import net.minecraft.entity.Entity
import net.minecraft.entity.decoration.DisplayEntity.BlockDisplayEntity
import net.minecraft.entity.decoration.DisplayEntity.ItemDisplayEntity
import net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity
import net.minecraft.nbt.*
import net.minecraft.server.command.ServerCommandSource


fun registerCommand() {
    registerCommandHandlers {
        register("displayeditor", "dp") {
            require { hasPermissionLevel(2) } //hasOp

            then("edit") {
                thenWithEntities("block") { thenBlockDisplayEdit() }
                thenWithEntities("item") { thenItemDisplayEdit() }
                thenWithEntities("text") { thenTextDisplayEdit() }

                thenWithEntities("transformation") { thenTransformationEdit() }
                thenWithEntities("position") { thenPositionEdit() }
                thenWithEntities("rotation") { thenRotationEdit() }
            }

            then("spawn") {
                then("block") { thenBlockDisplaySpawn() }
                then("item") { thenItemDisplaySpawn() }
                then("text") { thenTextDisplaySpawn() }
            }

            then("copydata") {
                require { player != null }

                executesWithEntity { it, entity ->
                    if(entity == null) return@executesWithEntity this.sendFeedback("개체를 찾을 수 없습니다".literal.color(NamedTextColor.RED))
                    val nbt = NbtCompound()
                    when(entity) {
                        is BlockDisplayEntity -> {
                            nbt.putFromAnotherNbt("block_state", NbtCompound().also { it.putString("Name", "minecraft:air") }, entity.nbt)
                        }
                        is ItemDisplayEntity -> {
                                nbt.putFromAnotherNbt<NbtCompound>("item", null, entity.nbt)
                                nbt.putFromAnotherNbt("item_display", "none", entity.nbt)
                            }
                        is TextDisplayEntity -> {
                            nbt.putFromAnotherNbt("alignment", "center", entity.nbt)
                            nbt.putFromAnotherNbt("background", 0, entity.nbt)
                            nbt.putFromAnotherNbt("default_background", false, entity.nbt)
                            nbt.putFromAnotherNbt("line_width", 200, entity.nbt)
                            nbt.putFromAnotherNbt("see_through", false, entity.nbt)
                            nbt.putFromAnotherNbt("shadow", false, entity.nbt)
                            nbt.putFromAnotherNbt("text", "", entity.nbt)
                            nbt.putFromAnotherNbt<Byte>("text_opacity", -1, entity.nbt)
                        }
                        else -> {
                            this.sendFeedback("개체가 표지가 아닙니다".literal.color(NamedTextColor.RED))
                            return@executesWithEntity
                        }
                    }
                    val dataList = listOf(
                        "billboard".put("fixed"),
                        "brightness".put(NbtCompound().also { it.putInt("block", 0);it.putInt("sky", 0) }),
                        "glow_color_override".put(-1),
                        "height".put(0),
                        "width".put(0),
                        "interpolation_duration".put(0),
                        "start_interpolation".put<Int>(null),
                        "shadow_radius".put(0f),
                        "shadow_strength".put(1f),
                        "view_range".put(1f),
                        "teleport_duration".put(0),
                        "transformation".put(NbtCompound().also {
                            fun nbtFloatList(vararg values: Float): NbtList {
                                return NbtList().also {
                                    for (value in values) it.add(NbtFloat.of(value))
                                }
                            }
                            it.put("left_rotation", nbtFloatList(0f, 0f, 0f, 1f))
                            it.put("translation", nbtFloatList(0f, 0f, 0f))
                            it.put("right_rotation", nbtFloatList(0f, 0f, 0f, 1f))
                            it.put("scale", nbtFloatList(1f, 1f, 1f))
                        }),
                    )
                    dataList.forEach {
                        nbt.putFromAnotherNbt(it, entity.nbt)
                    }
                    MinecraftClient.getInstance().keyboard.clipboard = nbt.asString()
                    this.sendFeedback("[".literal.append(entity.displayName()).append("]의 nbt데이터를 클립보드에 복사했습니다"))
                }
            }
        }
    }
}

fun <T> String.put(defaultValue: T?): Put<T> {
    return Put(this, defaultValue)
}
data class Put<T>(val key: String, val defaultValue: T? = null)

fun <T> NbtCompound.putFromAnotherNbt(put: Put<T>, another: NbtCompound) { this.putFromAnotherNbt(put.key, put.defaultValue, another) }
fun <T> NbtCompound.putFromAnotherNbt(key: String, defaultValue: T?, another: NbtCompound) {
    if(!another.contains(key)) return
    when(defaultValue) {
        is NbtCompound? -> if(another.getCompound(key) != defaultValue) this.put(key, another.getCompound(key))
        is String? -> if(another.getString(key) != defaultValue) this.putString(key, another.getString(key))
        is Int? -> if(another.getInt(key) != defaultValue) this.putInt(key, another.getInt(key))
        is Float? -> if(another.getFloat(key) != defaultValue) this.putFloat(key, another.getFloat(key))
        is Boolean? -> if(another.getBoolean(key) != defaultValue) this.putBoolean(key, another.getBoolean(key))
        is Byte? -> if(another.getByte(key) != defaultValue) this.putByte(key, another.getByte(key))
        is Long? -> if(another.getLong(key) != defaultValue) this.putLong(key, another.getLong(key))
    }
}


fun BrigadierNode.executesWithEntity(literal: String? = null, execute: ServerCommandSource.(CommandContext<ServerCommandSource>, Entity?) -> Unit) {
    this.thenWithEntity(literal) {
        executes { execute(this, it, it.get<EntitySelector>("entity").getEntity(this)) }
    }
}
fun BrigadierNode.thenWithEntity(literal: String? = null, then: BrigadierNode.() -> Unit) {
    if(literal == null) {
        then("entity" to entity()) {
            then(this)
        }
        return
    }
    then(literal) {
        then("entity" to entity()) {
            then(this)
        }
    }
}

fun BrigadierNode.thenWithEntities(literal: String, then: BrigadierNode.() -> Unit) {
    then(literal) {
        then("entities" to entities()) {
            then(this)
        }
    }
}