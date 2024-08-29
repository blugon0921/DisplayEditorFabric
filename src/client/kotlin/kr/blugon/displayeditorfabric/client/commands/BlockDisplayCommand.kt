package kr.blugon.displayeditorfabric.client.commands

import kr.blugon.displayeditorfabric.client.*
import kr.blugon.displayeditorfabric.client.api.*
import kr.blugon.displayeditorfabric.client.api.display.blockState
import kr.blugon.kotlinbrigadierfabric.BrigadierNode
import kr.blugon.kotlinbrigadierfabric.getValue
import net.minecraft.block.BlockState
import net.minecraft.command.argument.BlockStateArgument
import net.minecraft.command.argument.BlockStateArgumentType.blockState
import net.minecraft.command.argument.RotationArgumentType.rotation
import net.minecraft.command.argument.Vec3ArgumentType.vec3
import net.minecraft.entity.EntityType
import net.minecraft.entity.decoration.DisplayEntity.BlockDisplayEntity
import net.minecraft.nbt.NbtElement
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import org.joml.Vector3f



fun BrigadierNode.thenBlockDisplayEdit() {
    then("state" to blockState(registryAccess)) {
        executes {
            val entities = it.getEntities("entities")
            if(!entities.isBlockDisplayList(this)) return@executes
//            val state: BlockStateArgument by it
            val state: BlockStateArgument by it

            entities.forEach {
                (it as BlockDisplayEntity).blockState = state.blockState
                this.sendFeedback(it.nbt.getCompound("transformation").getList("translation", NbtElement.FLOAT_TYPE.toInt()).asString())
            }
            this.sendFeedback(Text.literal("블럭 표시 ${entities.size}개의 블록을 [").append(state.blockState.block.displayName()).append("](으)로 바꿨습니다"))
        }
    }
}


private fun ServerCommandSource.spawn(state: BlockState, location: Location, isCenter: Boolean = false) {
    location.spawnEntity<BlockDisplayEntity>(EntityType.BLOCK_DISPLAY) {
        it.blockState = state
        if(isCenter) {
            it.transformation.translation = Vector3f(-0.5f, -0.5f, -0.5f)
        }
    }
    this.sendFeedback(Text.literal("새로운 블럭 표시를 소환했습니다"))
}

fun BrigadierNode.thenBlockDisplaySpawn() {
    then("state" to blockState(registryAccess)) {
        then("position" to vec3()) {
            then("rotation" to rotation()) {
                then("-center") {
                    executes {
                        val state: BlockStateArgument by it
                        val location = it.getLocation(world, "position", "rotation")
                        spawn(state.blockState, location, true)
                    }
                }

                executes {
                    val state: BlockStateArgument by it
                    val location = it.getLocation(world, "position", "rotation")
                    spawn(state.blockState, location)
                }
            }

            executes {
                val state: BlockStateArgument by it
                val location = it.getLocationWithoutRotation(world, "position")
                spawn(state.blockState, location)
            }
        }

        executes {
            val state: BlockStateArgument by it
            spawn(state.blockState, this.location)
        }
    }
}