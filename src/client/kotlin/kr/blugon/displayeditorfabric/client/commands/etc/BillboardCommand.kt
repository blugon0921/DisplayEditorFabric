package kr.blugon.displayeditorfabric.client.commands.etc

import kr.blugon.displayeditorfabric.client.api.Billboard
import kr.blugon.displayeditorfabric.client.api.billboard
import kr.blugon.displayeditorfabric.client.api.sendFeedback
import kr.blugon.displayeditorfabric.client.getEntities
import kr.blugon.displayeditorfabric.client.isDisplayList
import kr.blugon.kotlinbrigadierfabric.BrigadierNode
import kr.blugon.kotlinbrigadierfabric.arguments.BillboardArgumentType.Companion.billboard
import kr.blugon.kotlinbrigadierfabric.getValue
import net.minecraft.entity.decoration.DisplayEntity
import net.minecraft.text.Text

fun BrigadierNode.thenBillboardEdit() {
    then("billboard" to billboard()) {
        executes {
            val entities = it.getEntities("entities")
            if(!entities.isDisplayList(this)) return@executes
            val billboard: Billboard by it
            entities.forEach { entity->
                (entity as DisplayEntity).billboard = billboard
            }
            this.sendFeedback(Text.literal("표지 ${entities.size}개의 billboard를 ${billboard.name.lowercase()}(으)로 바꿨습니다"))
        }
    }
}