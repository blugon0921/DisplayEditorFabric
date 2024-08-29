package kr.blugon.displayeditorfabric.client

import com.mojang.brigadier.context.CommandContext
import com.sk89q.worldedit.WorldEdit
import kr.blugon.displayeditorfabric.client.commands.registerCommand
import kr.blugon.displayeditorfabric.client.commands.worldedit.registerWorldEditCommand
import kr.blugon.displayeditorfabric.client.screen.BlockDisplayCreateGui
import kr.blugon.displayeditorfabric.client.screen.BlockDisplayCreateScreen
import kr.blugon.displayeditorfabric.client.api.NamedTextColor
import kr.blugon.displayeditorfabric.client.api.color
import kr.blugon.displayeditorfabric.client.api.literal
import kr.blugon.displayeditorfabric.client.api.sendFeedback
import kr.blugon.kotlinbrigadierfabric.get
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.MinecraftClient
import net.minecraft.command.EntitySelector
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
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
        if(entity.type != type) {
            if(this.size == 1) source.sendFeedback(Text.literal("개체가 ${typeName}가 아닙니다").color(NamedTextColor.RED))
            else if(1 < this.size) source.sendFeedback(Text.literal("${typeName}가 아닌 개체가 포함되어있습니다").color(NamedTextColor.RED))
            return false
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

inline fun <reified T> World.spawnEntity(location: WorldlessLocation, entityType: EntityType<*>, spawnBefore: (T) -> Unit = {}): T {
    return entityType.create(this).also {
        it?.refreshPositionAndAngles(location.position, location.yaw, location.pitch)
        spawnBefore(it as T)
        this.spawnEntity(it)
    } as T
}

//fun PlayerEntity.openGui() {
//    val client = MinecraftClient.getInstance()
//    client.execute {
//        client.setScreen(BlockDisplayCreateScreen(BlockDisplayCreateGui()))
//    }
//}

var worldedit: WorldEdit? = null
class DisplayEditorFabricClient : ClientModInitializer {
    override fun onInitializeClient() {
        registerCommand()
        worldedit = WorldEdit.getInstance()
        if(worldedit != null) {
            registerWorldEditCommand()
        }
    }
}