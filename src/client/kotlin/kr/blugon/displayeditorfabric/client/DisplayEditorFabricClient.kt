package kr.blugon.displayeditorfabric.client

import com.mojang.brigadier.context.CommandContext
import kr.blugon.displayeditorfabric.client.api.NamedTextColor
import kr.blugon.displayeditorfabric.client.api.color
import kr.blugon.displayeditorfabric.client.api.literal
import kr.blugon.displayeditorfabric.client.api.sendFeedback
import kr.blugon.displayeditorfabric.client.commands.registerCommand
import kr.blugon.displayeditorfabric.client.commands.worldedit.registerWorldEditCommand
import kr.blugon.displayeditorfabric.client.events.registerJoinEvent
import kr.blugon.kotlinbrigadierfabric.get
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.block.BlockState
import net.minecraft.command.EntitySelector
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnReason
import net.minecraft.entity.decoration.DisplayEntity
import net.minecraft.entity.decoration.DisplayEntity.BlockDisplayEntity
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World


fun CommandContext<ServerCommandSource>.getEntities(name: String): List<Entity> {
    return ArrayList<Entity>().apply {
        this@getEntities.get<EntitySelector>(name).getEntities(source).forEach {
            this.add(it)
        }
    }
}
val ServerCommandSource.location: Location
    get() {
        return Location(this.world, this.position)
    }


fun Collection<Entity>.isDisplayList(source: ServerCommandSource, type: EntityType<*>? = null, typeName: String = "표시"): Boolean {
    if(this.isEmpty()) {
        source.sendFeedback("개체를 찾을 수 없습니다".literal.color(NamedTextColor.RED))
        return false
    }
    this.forEach { entity ->
        if(type != null && entity.type != type) {
            if(this.size == 1) source.sendFeedback(Text.literal("개체가 ${typeName}가 아닙니다").color(NamedTextColor.RED))
            else if(1 < this.size) source.sendFeedback(Text.literal("${typeName}가 아닌 개체가 포함되어있습니다").color(NamedTextColor.RED))
            return false
        } else if(type == null) {
            if(entity !is DisplayEntity) {
                if(this.size == 1) source.sendFeedback(Text.literal("개체가 표시가 아닙니다").color(NamedTextColor.RED))
                else if(1 < this.size) source.sendFeedback(Text.literal("표시가 아닌 개체가 포함되어있습니다").color(NamedTextColor.RED))
                return false
            }
        }
    }
    return true
}
fun Collection<Entity>.isBlockDisplayList(source: ServerCommandSource): Boolean {
    return this.isDisplayList(source, EntityType.BLOCK_DISPLAY, "블럭 표시")
}
fun Collection<Entity>.isItemDisplayList(source: ServerCommandSource): Boolean {
    return this.isDisplayList(source, EntityType.ITEM_DISPLAY, "아이템 표시")
}
fun Collection<Entity>.isTextDisplayList(source: ServerCommandSource): Boolean {
    return this.isDisplayList(source, EntityType.TEXT_DISPLAY, "텍스트 표시")
}

inline fun <reified T> World.spawnEntityBukkitStyle(location: WorldlessLocation, entityType: EntityType<*>, spawnBefore: (T) -> Unit = {}): T {
    return entityType.create(this, SpawnReason.COMMAND).also {
        it?.refreshPositionAndAngles(location.position, location.yaw, location.pitch)
        spawnBefore(it as T)
        this.spawnEntity(it)
    } as T
}

val undoList = ArrayList<UndoData>()
data class UndoData(
    val world: World,
    val blocks: HashMap<BlockPos, BlockState>,
    val displays: List<BlockDisplayEntity>,
)

class DisplayEditorFabricClient : ClientModInitializer {
    override fun onInitializeClient() {
        //Commands
        registerCommand()
        if(FabricLoader.getInstance().isModLoaded("worldedit")) {
            registerWorldEditCommand()
        }

        //Events
        registerJoinEvent()
    }
}