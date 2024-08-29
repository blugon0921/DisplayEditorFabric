package kr.blugon.displayeditorfabric.client.api.display

import kr.blugon.displayeditorfabric.client.api.editNbt
import kr.blugon.displayeditorfabric.client.api.nbt
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity
import net.minecraft.text.Text

val TextDisplayEntity.text: Text? get() = this.data?.text

fun TextDisplayEntity.setText(value: Text?, registryAccess: CommandRegistryAccess) {
    editNbt {
        if(value == null) {
            it.putString("text", "")
            return@editNbt
        }
        it.putString("text", Text.Serialization.toJsonString(value, registryAccess))
    }
}

var TextDisplayEntity.background: Int
    get() = this.nbt.getInt("background")
    set(value) {
        this.editNbt {
            it.putInt("background", value)
        }
    }