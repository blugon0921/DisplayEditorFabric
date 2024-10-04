package kr.blugon.displayeditorfabric.client.api

import com.google.gson.Gson
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.serialization.JsonOps
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.ItemEnchantmentsComponent
import net.minecraft.entity.Entity
import net.minecraft.entity.decoration.DisplayEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.*
import org.joml.Vector3f

var Entity.nbt: NbtCompound
    get() = NbtCompound().also { this.writeNbt(it) }
    set(value) {
        this.readNbt(value)
    }
fun Entity.editNbt(nbt: (NbtCompound) -> Unit) {
    NbtCompound().also {
        this.writeNbt(it)
        nbt(it)
        this.nbt = it
    }
}


val ItemStack.nbt: NbtCompound?
    get() {
        val itemStack = this.copy()
        itemStack.set(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT)
        val enchantments = if(this.hasEnchantments()) this.enchantments
        else null

        // Stack -> Json
        val json = ItemStack.CODEC.encode(itemStack.copy(), JsonOps.INSTANCE, JsonOps.INSTANCE.empty())

        // Json -> String -> NbtCompound
        return if (json.result().isPresent) NbtHelper.fromNbtProviderString(Gson().toJson(json.result().get())).also { nbt ->
            if(!nbt.contains("components")) nbt.put("components", NbtCompound())
            nbt.put("components", nbt.getCompound("components").also { components->
                components.put("minecraft:enchantments", NbtCompound().also { enchantment ->
                    enchantment.put("levels", NbtCompound().also {  levels->
                        enchantments?.enchantments?.forEach {
                            levels.putInt(it.idAsString, enchantments.getLevel(it))
                        }
                    })
                })
            })
        }
        else null
    }

//val Block.id: String?
//    get() {
//        val split = this.translationKey.replace("block.", "").split(".")
//        val namespacedKey = split.getOrNull(0)
//        val id = split.getOrNull(1)
//        return if(namespacedKey == null || id == null) null
//        else "$namespacedKey:$id"
//    }

var DisplayEntity.billboard: Billboard
    get() {
        return Billboard[this.nbt.getString("billboard")?: "fixed"]!!
    }
    set(value) {
        this.editNbt { nbt->
            nbt.putString("billboard", value.name.lowercase())
        }
    }

enum class Billboard {
    Fixed,
    Vertical,
    Horizontal,
    Center;

    companion object {
        operator fun get(name: String): Billboard? {
            entries.forEach {
                if(it.name.lowercase() == name) return it
            }
            return null
        }
    }
}


val DisplayEntity.transformation: DisplayTransformation
    get() = DisplayTransformation(this)

class DisplayTransformation(val entity: DisplayEntity) {
    private val nbt: NbtCompound get() = entity.nbt.getCompound("transformation")

    private fun Entity.editTransformation(nbt: (NbtCompound) -> Unit) {
        this.editNbt {
            it.getCompound("transformation").also { transformation ->
                nbt(transformation)
            }
        }
    }

    var leftRotation: Vector3f
        get() {
            val list = nbt.getList("left_rotation", NbtElement.FLOAT_TYPE.toInt())
            return Vector3f(list.getFloat(0), list.getFloat(1), list.getFloat(2))
        }
        set(value) {
            entity.editTransformation {
                it.put("left_rotation", NbtList().apply {
                    this.add(NbtFloat.of(value.x))
                    this.add(NbtFloat.of(value.y))
                    this.add(NbtFloat.of(value.z))
                })
            }
        }

    var rightRotation: Vector3f
        get() {
            val list = nbt.getList("right_rotation", NbtElement.FLOAT_TYPE.toInt())
            return Vector3f(list.getFloat(0), list.getFloat(1), list.getFloat(2))
        }
        set(value) {
            entity.editTransformation {
                it.put("right_rotation", NbtList().apply {
                    this.add(NbtFloat.of(value.x))
                    this.add(NbtFloat.of(value.y))
                    this.add(NbtFloat.of(value.z))
                })
            }
        }

    var scale: Vector3f
        get() {
            val list = nbt.getList("scale", NbtElement.FLOAT_TYPE.toInt())
            return Vector3f(list.getFloat(0), list.getFloat(1), list.getFloat(2))
        }
        set(value) {
            entity.editTransformation {
                it.put("scale", NbtList().apply {
                    this.add(NbtFloat.of(value.x))
                    this.add(NbtFloat.of(value.y))
                    this.add(NbtFloat.of(value.z))
                })
            }
        }

    var translation: Vector3f
        get() {
            val list = nbt.getList("translation", NbtElement.FLOAT_TYPE.toInt())
            return Vector3f(list.getFloat(0), list.getFloat(1), list.getFloat(2))
        }
        set(value) {
            entity.editTransformation {
                it.put("translation", NbtList().apply {
                    this.add(NbtFloat.of(value.x))
                    this.add(NbtFloat.of(value.y))
                    this.add(NbtFloat.of(value.z))
                })
            }
        }
}