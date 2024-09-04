package kr.blugon.displayeditorfabric.client.commands.position

import kr.blugon.displayeditorfabric.client.api.sendFeedback
import kr.blugon.displayeditorfabric.client.getEntities
import kr.blugon.displayeditorfabric.client.isDisplayList
import kr.blugon.kotlinbrigadierfabric.BrigadierNode
import kr.blugon.kotlinbrigadierfabric.get
import net.minecraft.command.argument.PosArgument
import net.minecraft.command.argument.RotationArgumentType.rotation
import net.minecraft.command.argument.Vec3ArgumentType.vec3
import net.minecraft.network.packet.s2c.play.PositionFlag
import net.minecraft.text.Text
import kotlin.math.pow
import kotlin.math.roundToInt

fun BrigadierNode.thenPositionEdit() {
    then("position" to vec3()) {
        executes {
            val entities = it.getEntities("entities")
            if(!entities.isDisplayList(this)) return@executes
            val position = it.get<PosArgument>("position").toAbsolutePos(this)
            entities.forEach { entity->
                entity.teleport(world, position.x, position.y, position.z, PositionFlag.ROT, entity.yaw, entity.pitch)
            }
            this.sendFeedback(Text.literal("표지 ${entities.size}개의 위치를 [${position.x.floor(6)}, ${position.y.floor(6)}, ${position.z.floor(6)}](으)로 이동시켰습니다"))
        }
    }
}

fun BrigadierNode.thenRotationEdit() {
    then("rotation" to rotation()) {
        executes {
            val entities = it.getEntities("entities")
            if(!entities.isDisplayList(this)) return@executes
            val rotation = it.get<PosArgument>("rotation").toAbsoluteRotation(this)
            entities.forEach { entity->
                entity.teleport(world, entity.x, entity.y, entity.z, PositionFlag.ROT, rotation.y, rotation.x)
            }
            this.sendFeedback(Text.literal("표지 ${entities.size}개의 회전을 [${rotation.y}, ${rotation.x}](으)로 바꿨습니다"))
        }
    }
}

fun Double.floor(dot: Int): Double = String.format("%.${dot}f", this).toDouble()