package kr.blugon.displayeditorfabric.client.api.display

import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import kr.blugon.displayeditorfabric.client.api.editNbt
import kr.blugon.displayeditorfabric.client.api.nbt
import net.minecraft.entity.decoration.DisplayEntity.ItemDisplayEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound

var ItemDisplayEntity.itemStack: ItemStack?
    get() {
        val jsonDecoded = JsonParser.parseString(this.nbt.getCompound("item").asString())
        return ItemStack.CODEC.decode(JsonOps.INSTANCE, jsonDecoded).result().get().first
    }
    set(value) {
        editNbt {
            if(value == null) {
                it.put("item", NbtCompound())
                return@editNbt
            }
            it.put("item", value.nbt?: NbtCompound())
        }
    }