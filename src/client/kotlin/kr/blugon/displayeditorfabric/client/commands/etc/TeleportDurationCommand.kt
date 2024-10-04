package kr.blugon.displayeditorfabric.client.commands.etc

import com.mojang.brigadier.arguments.IntegerArgumentType.integer
import kr.blugon.displayeditorfabric.client.api.*
import kr.blugon.displayeditorfabric.client.getEntities
import kr.blugon.displayeditorfabric.client.isDisplayList
import kr.blugon.kotlinbrigadierfabric.BrigadierNode
import kr.blugon.kotlinbrigadierfabric.arguments.BillboardArgumentType.Companion.billboard
import kr.blugon.kotlinbrigadierfabric.getValue
import net.minecraft.entity.decoration.DisplayEntity
import net.minecraft.text.Text

//teleport_duration
fun BrigadierNode.thenTeleportDurationEdit() {

    then("teleport_duration" to integer(0, 59)) {
        executes { it ->
            val entities = it.getEntities("entities")
            if(!entities.isDisplayList(this)) return@executes
            val teleport_duration: Int by it
            entities.forEach { entity->
                entity.editNbt {
                    it.putInt("teleport_duration", teleport_duration)
                }
            }
            this.sendFeedback(Text.literal("표지 ${entities.size}개의 teleport_duration를 ${teleport_duration}(으)로 바꿨습니다"))
        }
    }
}