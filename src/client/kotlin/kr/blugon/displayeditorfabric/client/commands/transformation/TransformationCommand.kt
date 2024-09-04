package kr.blugon.displayeditorfabric.client.commands.transformation

import com.mojang.brigadier.arguments.FloatArgumentType.floatArg
import com.mojang.brigadier.context.CommandContext
import kr.blugon.displayeditorfabric.client.api.editNbt
import kr.blugon.displayeditorfabric.client.api.sendFeedback
import kr.blugon.displayeditorfabric.client.getEntities
import kr.blugon.displayeditorfabric.client.isDisplayList
import kr.blugon.kotlinbrigadierfabric.BrigadierNode
import kr.blugon.kotlinbrigadierfabric.get
import net.minecraft.nbt.NbtFloat
import net.minecraft.nbt.NbtList
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text


fun BrigadierNode.thenTransformationEdit() {
    Transformation.entries.forEach { first->
        thenAction(first, listOf(first)) {
            Transformation.entries.filter { it != first }.forEach second@{ second->
                thenAction(second, listOf(first, second)) {
                    Transformation.entries.filter { !listOf(first, second).contains(it) }.forEach third@{ third->
                        thenAction(third, listOf(first, second, third)) {
                            Transformation.entries.filter { !listOf(first, second, third).contains(it) }.forEach fourth@{ fourth->
                                thenAction(fourth, listOf(first, second, third, fourth))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun BrigadierNode.thenAction(action: Transformation, keys: List<Transformation>? = null, next: BrigadierNode.() -> Unit = {}) {
    then(action.valueName) {
        fun BrigadierNode.runNext() {
            next(this)
            executes {
                val entities = it.getEntities("entities")
                if(!entities.isDisplayList(this)) return@executes
                val keysNotNull = ArrayList<Transformation>()
                if(keys == null)  keysNotNull.add(action)
                else keysNotNull.addAll(keys)
                var message = ""
                val editNbt = HashMap<String, NbtList>()
                keysNotNull.forEach { key->
                    val value = it.getFloatList(key.valueName, key.valueSize)
                    editNbt[key.valueName] = NbtList().apply {
                        value.forEach {
                            this.add(NbtFloat.of(it))
                        }
                    }
                    message += "${key.valueName}을(를) ${value.toStringWithF()}(으)로, "
                }
                entities.forEach { entity ->
                    entity.editNbt { nbt ->
                        nbt.getCompound("transformation").also { transformation ->
                            editNbt.forEach { (key, list) ->
                                println("${key}: ${list}")
                                transformation.put(key, list)
                            }
                        }
                    }
                }
                this.sendFeedback(Text.literal("표지 ${entities.size}개의 ${message.substring(0, message.length-2)} 바꿨습니다"))
            }
        }

        when(action) {
            Transformation.LEFT_ROTATION,
            Transformation.RIGHT_ROTATION -> { thenFloatList4(action.valueName) { runNext() } }
            Transformation.TRANSLATION,
            Transformation.SCALE -> { thenFloatList3(action.valueName) { runNext() } }
        }
    }
}

fun BrigadierNode.thenFloatList3(name: String, next: BrigadierNode.() -> Unit) {
    then("${name}_0" to floatArg()) {
        then("${name}_1" to floatArg()) {
            then("${name}_2" to floatArg()) {
                next(this)
            }
        }
    }
}
fun BrigadierNode.thenFloatList4(name: String, next: BrigadierNode.() -> Unit) {
    this.thenFloatList3(name) {
        then("${name}_3" to floatArg()) {
            next(this)
        }
    }
}

fun CommandContext<ServerCommandSource>.getFloatList(key: String, size: Int): List<Float> {
    return ArrayList<Float>().also { list ->
        repeat(size) {
            list.add(this.get<Float>("${key}_${it}"))
        }
    }
}


enum class Transformation(val valueName: String, val valueSize: Int) {
    LEFT_ROTATION("left_rotation", 4),
    RIGHT_ROTATION("right_rotation", 4),
    TRANSLATION("translation", 3),
    SCALE("scale", 3),
}

fun List<Float>.toStringWithF(): String {
    if (isEmpty()) return "[]"
    var value = "["
    this.forEach {
        value += "${it}f, "
    }
    return "${value.substring(0, value.length - 2)}]"
}