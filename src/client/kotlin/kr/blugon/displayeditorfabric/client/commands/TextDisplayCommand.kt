package kr.blugon.displayeditorfabric.client.commands

import com.mojang.brigadier.arguments.IntegerArgumentType.integer
import com.mojang.brigadier.context.CommandContext
import kr.blugon.displayeditorfabric.client.*
import kr.blugon.displayeditorfabric.client.api.display.background
import kr.blugon.displayeditorfabric.client.api.display.setText
import kr.blugon.kotlinbrigadierfabric.BrigadierNode
import kr.blugon.kotlinbrigadierfabric.get
import kr.blugon.kotlinbrigadierfabric.getValue
import kr.blugon.displayeditorfabric.client.api.sendFeedback
import net.minecraft.command.argument.PosArgument
import net.minecraft.command.argument.RotationArgumentType.rotation
import net.minecraft.command.argument.TextArgumentType.text
import net.minecraft.command.argument.Vec3ArgumentType.vec3
import net.minecraft.entity.EntityType
import net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text


fun BrigadierNode.thenTextDisplayEdit() {
    then("text" to text(registryAccess)) {
//        suggests {
//            val entities = it.getEntities("entities")
//            if(!entities.isTextDisplayList(this)) return@suggests listOf()
//            val suggestions = ArrayList<String>()
//            entities.forEach { entity->
//                val text = (entity as TextDisplayEntity).data?.text?: return@forEach
//                suggestions.add(Text.Serialization.toJsonString(text, registryAccess))
//            }
//            return@suggests suggestions
//        }
        then("background" to integer()) {
            executes {
                val entities = it.getEntities("entities")
                if(!entities.isTextDisplayList(this)) return@executes
                val text: Text by it
                val background: Int by it

                entities.forEach { entity->
                    (entity as TextDisplayEntity).setText(text, registryAccess)
                    entity.background = background
                }
                sendFeedback(Text.literal("문자 표시 ${entities.size}개의 텍스트를 [").append(text).append("](으)로 바꿨습니다"))
            }
        }
        executes {
            val entities = it.getEntities("entities")
            if(!entities.isTextDisplayList(this)) return@executes
            val text: Text by it

            entities.forEach { entity->
                (entity as TextDisplayEntity).setText(text, registryAccess)
            }
            sendFeedback(Text.literal("문자 표시 ${entities.size}개의 텍스트를 [").append(text).append("](으)로 바꿨습니다"))
        }
    }
}


private fun BrigadierNode.runWithPosition(run: ServerCommandSource.(CommandContext<ServerCommandSource>, WorldlessLocation?) -> TextDisplayEntity) {
    this.executes {
        run(this, it, null)
    }
    this.then("location" to vec3()) {
        this.then("rotation" to rotation()) {
            then("background" to integer()) {
                executes {
                    val positionResolver: PosArgument = it["location"]
                    val position = positionResolver.getPos(this)
                    val rotation = it.get<PosArgument>("rotation").getRotation(this)
                    val background: Int by it
                    run(this, it, WorldlessLocation(position, rotation)).also {
                        it.background = background
                    }
                }
            }
            executes {
                val positionResolver: PosArgument = it["location"]
                val position = positionResolver.getPos(this)
                val rotation = it.get<PosArgument>("rotation").getRotation(this)
                run(this, it, WorldlessLocation(position, rotation))
            }
        }
        executes {
            val positionResolver: PosArgument = it["location"]
            val position = positionResolver.getPos(this)
            run(this, it, WorldlessLocation(position))
        }
    }
}
fun BrigadierNode.thenTextDisplaySpawn() {
    then("text" to text(registryAccess)) {
        runWithPosition { it, worldless ->
            val text: Text by it
            val location = worldless?.toLocation(world)?: this.location

            this.sendFeedback(Text.literal("새로운 문자 표시를 소환했습니다"))
            location.spawnEntity<TextDisplayEntity>(EntityType.TEXT_DISPLAY) {
                it.setText(text, registryAccess)
            }
        }
    }
}