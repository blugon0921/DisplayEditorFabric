package kr.blugon.displayeditorfabric.client.commands.worldedit

import com.sk89q.worldedit.EmptyClipboardException
import com.sk89q.worldedit.fabric.FabricAdapter
import com.sk89q.worldedit.math.BlockVector3
import kr.blugon.displayeditorfabric.client.Location
import kr.blugon.displayeditorfabric.client.api.*
import kr.blugon.displayeditorfabric.client.api.display.blockState
import kr.blugon.displayeditorfabric.client.location
import kr.blugon.displayeditorfabric.client.worldedit
import kr.blugon.kotlinbrigadierfabric.registerCommandHandlers
import net.minecraft.block.Blocks
import net.minecraft.entity.EntityType
import net.minecraft.entity.decoration.DisplayEntity.BlockDisplayEntity
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.util.math.BlockPos

fun registerWorldEditCommand() {
    registerCommandHandlers {
        register("/pastedisplay", "/paste-d") {
            require { this.hasPermissionLevel(2) }
            require { player != null }

            then("-not-remove") {
                executes {
                    this.pasteDisplay(false)
                }
            }

            executes {
                this.pasteDisplay(true)
            }
        }
    }
}


private fun ServerCommandSource.pasteDisplay(removeBlock: Boolean = true) {
    val world = location.world
    val player = this.player?: return this.sendFeedback("알 수 없는 오류가 발생했습니다. :(".literal.color(NamedTextColor.RED))
    val session = worldedit!!.sessionManager.getIfPresent(FabricAdapter.adaptPlayer(player)) ?: return this.sendFeedback("현재 클립보드가 비어있습니다. //copy를 먼저 사용하세요.".literal.color(NamedTextColor.RED))
    val clipboard = try {
        session.clipboard?.clipboard
    } catch (e: EmptyClipboardException) { return this.sendFeedback("현재 클립보드가 비어있습니다. //copy를 먼저 사용하세요.".literal.color(NamedTextColor.RED)) }
    if(clipboard == null) return
//    this.sendFeedback("${clipboard.region.minimumPoint} ${clipboard.region.maximumPoint}".literal)
//    this.sendFeedback("${clipboard.origin.x()} ${clipboard.origin.y()} ${clipboard.origin.z()}".literal)
    val relatively = (clipboard.minimumPoint to clipboard.maximumPoint).relative(clipboard.origin, player.location)
    val minimum = relatively.minimumPoint
    val maximum = relatively.maximumPoint


    var count = 0
    for (x in minimum.x()..maximum.x()) {
        for (y in minimum.y()..maximum.y()) {
            for (z in minimum.z()..maximum.z()) {
                val state = clipboard.getBlock(BlockVector3(x, y, z).unRelative(clipboard.origin, location))
//                val block = world.getBlockState(BlockPos(x, y, z))
                when(FabricAdapter.adapt(state.blockType).defaultState) {
                    Blocks.AIR,
                    Blocks.CAVE_AIR,
                    Blocks.VOID_AIR,
                    Blocks.LAVA,
                    Blocks.WATER -> {
                        continue
                    }
                    else -> {
                        if(removeBlock) world.setBlockState(BlockPos(x, y, z), Blocks.AIR.defaultState)
//                       block.type = BukkitAdapter.adapt(state.blockType)
//                       block.blockData = BukkitAdapter.adapt(state)
                        Location(world, BlockPos(x, y, z)).spawnEntity<BlockDisplayEntity>(EntityType.BLOCK_DISPLAY) {
                            it.blockState = FabricAdapter.adapt(state)
                        }
                        count++
                    }
                }
            }
        }
    }
    this.sendFeedback("클립보드를 (${location.blockX}, ${location.blockY}, ${location.blockZ}) 에 블럭 표시로 붙여졌습니다. ($count) ".literal.color(NamedTextColor.LIGHT_PURPLE))
}