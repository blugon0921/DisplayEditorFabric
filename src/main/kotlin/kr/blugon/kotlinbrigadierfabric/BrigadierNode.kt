package kr.blugon.kotlinbrigadierfabric

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import kotlin.reflect.KProperty


interface BrigadierNode {
    val registryAccess: CommandRegistryAccess
    val environment: CommandManager.RegistrationEnvironment
    fun then(literal: String, node: LiteralBrigadierNode.() -> Unit = {})
    fun <T> then(argument: Pair<String, ArgumentType<T>>, node: RequiredBrigadierNode<T>.() -> Unit = {})
    fun require(require: (ServerCommandSource) -> Boolean)
    fun requires(requires: (ServerCommandSource) -> List<Boolean>)
    fun executes(execute: ServerCommandSource.(CommandContext<ServerCommandSource>) -> Unit)
    operator fun String.invoke(node: LiteralBrigadierNode.() -> Unit) = then(this, node)
}

class LiteralBrigadierNode(val builder: LiteralArgumentBuilder<ServerCommandSource>, override val registryAccess: CommandRegistryAccess, override val environment: CommandManager.RegistrationEnvironment): BrigadierNode {
    override fun then(literal: String, node: LiteralBrigadierNode.() -> Unit) {
        builder.then(LiteralArgumentBuilder.literal<ServerCommandSource?>(literal).apply {
            node(LiteralBrigadierNode(this, registryAccess, environment))
        })
    }
    override fun <T> then(argument: Pair<String, ArgumentType<T>>, node: RequiredBrigadierNode<T>.() -> Unit) {
        builder.then(argument<ServerCommandSource, T>(argument.first, argument.second).apply {
            node(RequiredBrigadierNode(this, registryAccess, environment))
        })
    }

    override fun require(require: ServerCommandSource.() -> Boolean) {
        builder.requires(require)
    }
    override fun requires(requires: ServerCommandSource.() -> List<Boolean>) {
        var isRequire = true
        builder.requires { source ->
            val conditions = requires(source)
            conditions.forEach {
                isRequire = isRequire && it
            }
            isRequire
        }
    }

    override fun executes(execute: ServerCommandSource.(CommandContext<ServerCommandSource>) -> Unit) {
        builder.executes { c->
            execute(c.source, c)
            return@executes 1
        }
    }
}
class RequiredBrigadierNode <T> (val builder: RequiredArgumentBuilder<ServerCommandSource, T>, override val registryAccess: CommandRegistryAccess, override val environment: CommandManager.RegistrationEnvironment): BrigadierNode {
    override fun then(literal: String, node: LiteralBrigadierNode.() -> Unit) {
        builder.then(LiteralArgumentBuilder.literal<ServerCommandSource>(literal).apply {
            node(LiteralBrigadierNode(this, registryAccess, environment))
        })
    }
    override fun <T> then(argument: Pair<String, ArgumentType<T>>, node: RequiredBrigadierNode<T>.() -> Unit) {
        builder.then(argument<ServerCommandSource, T>(argument.first, argument.second).apply {
            node(RequiredBrigadierNode(this, registryAccess, environment))
        })
    }
    override fun require(require: ServerCommandSource.() -> Boolean) {
        builder.requires(require)
    }
    override fun requires(requires: ServerCommandSource.() -> List<Boolean>) {
        var isRequire = true
        builder.requires { source ->
            val conditions = requires(source)
            conditions.forEach {
                isRequire = isRequire && it
            }
            isRequire
        }
    }
    fun suggests(isSharedSuggestion: Boolean = true, suggest: ServerCommandSource.(CommandContext<ServerCommandSource>) -> List<String>) {
        builder.suggests { context, suggestionsBuilder ->
            if(isSharedSuggestion) SharedSuggestionProvider.suggest(suggest(context.source, context), context, suggestionsBuilder)
            else {
                suggest(context.source, context).forEach { suggestion->
                    suggestionsBuilder.suggest(suggestion)
                }
                suggestionsBuilder.buildFuture()
            }
        }
    }
    fun suggests(suggestions: List<String>, isSharedSuggestion: Boolean = true) {
        suggests(isSharedSuggestion) {
            suggestions
        }
    }
    fun suggestsWithBuilder(suggest: ServerCommandSource.(CommandContext<ServerCommandSource>, SuggestionsBuilder) -> Unit) {
        builder.suggests { commandContext, suggestionsBuilder ->
            suggest(commandContext.source, commandContext, suggestionsBuilder)
            suggestionsBuilder.buildFuture()
        }
    }

    override fun executes(execute: ServerCommandSource.(CommandContext<ServerCommandSource>) -> Unit) {
        builder.executes { c->
            execute(c.source, c)
            return@executes 1
        }
    }
}

inline operator fun <reified T> CommandContext<ServerCommandSource>.get(name: String): T {
    return this.getArgument(name, T::class.java)
}

inline operator fun <reified T> CommandContext<ServerCommandSource>.getValue(thisRef: Any?, property: KProperty<*>): T {
    return this.getArgument(property.name, T::class.java)
}