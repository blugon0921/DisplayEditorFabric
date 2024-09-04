package kr.blugon.kotlinbrigadierfabric

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

fun registerCommandHandlers(command: BrigadierCommand.() -> Unit) {
    CommandRegistrationCallback.EVENT.register { dispatcher, registryAccess, environment ->
        command(BrigadierCommand(dispatcher, registryAccess, environment))

        dispatcher.register(literal<ServerCommandSource?>("displayeditor").apply {
            executes { context ->
                context.source.sendFeedback({ Text.literal("Command Test") }, false)
                return@executes 1
            }
        })
    }
}

class BrigadierCommand(val dispatcher: CommandDispatcher<ServerCommandSource>, val registryAccess: CommandRegistryAccess, val environment: CommandManager.RegistrationEnvironment) {
    fun register(name: String, vararg aliases: String, node: LiteralBrigadierNode.() -> Unit) {
        val aliasesCollection = mutableListOf<String>()
        aliases.forEach { aliasesCollection.add(it) }
//        val command = dispatcher.register(literal<ServerCommandSource?>(name).apply {
        dispatcher.register(literal<ServerCommandSource?>(name).apply {
            node(LiteralBrigadierNode(this, registryAccess, environment))
        })
        aliases.forEach {
//            dispatcher.register(literal<ServerCommandSource?>(it).redirect(command))
            dispatcher.register(literal<ServerCommandSource?>(it).apply {
                node(LiteralBrigadierNode(this, registryAccess, environment))
            })
        }
    }
}