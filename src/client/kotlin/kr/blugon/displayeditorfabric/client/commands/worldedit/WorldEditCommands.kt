package kr.blugon.displayeditorfabric.client.commands.worldedit

import com.sk89q.worldedit.EmptyClipboardException
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.fabric.FabricAdapter
import com.sk89q.worldedit.math.BlockVector3
import kr.blugon.displayeditorfabric.client.Location
import kr.blugon.displayeditorfabric.client.UndoData
import kr.blugon.displayeditorfabric.client.api.*
import kr.blugon.displayeditorfabric.client.api.display.blockState
import kr.blugon.displayeditorfabric.client.location
import kr.blugon.displayeditorfabric.client.undoList
import kr.blugon.kotlinbrigadierfabric.registerCommandHandlers
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.decoration.DisplayEntity.BlockDisplayEntity
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.util.math.BlockPos

fun registerWorldEditCommand() {
    val worldedit = WorldEdit.getInstance() ?: return
    registerCommandHandlers {
        register("/pastedisplays", "/paste-d") {
            require { this.hasPermissionLevel(2) }
            require { player != null }

            then("-not-remove") {
                executes {
                    this.pasteDisplay(false, worldedit)
                }
            }

            executes {
                this.pasteDisplay(true, worldedit)
            }
        }

        register("/undodisplays", "/undo-d") {
            require { this.hasPermissionLevel(2) }
            require { player != null }

            executes {
                if(undoList.isEmpty()) {
                    this.sendFeedback("되돌릴 것이 없어요.".literal.color(NamedTextColor.RED))
                    return@executes
                }
                val undoData = undoList.removeLast()
                undoData.blocks.forEach { (pos, state) ->
                    undoData.world.setBlockState(pos, state)
                }
                undoData.displays.forEach { display ->
                    if(display.isRemoved) return@forEach
                    display.remove(Entity.RemovalReason.DISCARDED)
                }
            }
        }
    }
}


private fun ServerCommandSource.pasteDisplay(removeBlock: Boolean = true, worldedit: WorldEdit) {
    val world = location.world
    val player = this.player?: return this.sendFeedback("알 수 없는 오류가 발생했습니다. :(".literal.color(NamedTextColor.RED))
    val session = worldedit.sessionManager.getIfPresent(FabricAdapter.adaptPlayer(player)) ?:
        return this.sendFeedback("현재 클립보드가 비어있습니다. //copy를 먼저 사용하세요.".literal.color(NamedTextColor.RED))
    val clipboard = try {
        session.clipboard?.clipboard
    } catch (_: EmptyClipboardException) {
        return this.sendFeedback("현재 클립보드가 비어있습니다. //copy를 먼저 사용하세요.".literal.color(NamedTextColor.RED))
    }
    if(clipboard == null) return
//    this.sendFeedback("${clipboard.region.minimumPoint} ${clipboard.region.maximumPoint}".literal)
//    this.sendFeedback("${clipboard.origin.x()} ${clipboard.origin.y()} ${clipboard.origin.z()}".literal)
    val relatively = (clipboard.minimumPoint to clipboard.maximumPoint).relative(clipboard.origin, player.location)
    val minimum = relatively.minimumPoint
    val maximum = relatively.maximumPoint

    val blocks = HashMap<BlockPos, BlockState>()
    val displays = ArrayList<BlockDisplayEntity>()
    var count = 0
    for (x in minimum.x()..maximum.x()) {
        for (y in minimum.y()..maximum.y()) {
            for (z in minimum.z()..maximum.z()) {
                val state = clipboard.getBlock(BlockVector3(x, y, z).unRelative(clipboard.origin, location))
                val adaptedState = FabricAdapter.adapt(state)
                val block = world.getBlockState(BlockPos(x, y, z))
                when(FabricAdapter.adapt(state.blockType).defaultState) {
                    Blocks.AIR,
                    Blocks.CAVE_AIR,
                    Blocks.VOID_AIR,
                    Blocks.LAVA,
                    Blocks.WATER -> continue
                    else -> {
                        if(removeBlock) {
                            blocks[BlockPos(x, y, z)] = block
                            world.setBlockState(BlockPos(x, y, z), Blocks.AIR.defaultState)
                        }
                        if(adaptedState.block == Blocks.AIR) continue
//                        block.type = BukkitAdapter.adapt(state.blockType) //이게 뭐하는데 쓰는 코드였지?
//                        block.blockData = BukkitAdapter.adapt(state)
                        Location(world, BlockPos(x, y, z)).spawnEntity<BlockDisplayEntity>(EntityType.BLOCK_DISPLAY) {
                            it.blockState = adaptedState
                            displays.add(it)
                        }
                        count++
                    }
                }
            }
        }
    }
    undoList.add(UndoData(
        world,
        blocks,
        displays,
    ))
    this.sendFeedback("클립보드를 (${location.blockX}, ${location.blockY}, ${location.blockZ}) 에 블럭 표시로 붙여졌습니다. ($count) ".literal.color(NamedTextColor.LIGHT_PURPLE))
}