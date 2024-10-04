package kr.blugon.kotlinbrigadierfabric.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import kr.blugon.displayeditorfabric.client.api.Billboard
import kr.blugon.kotlinbrigadierfabric.SharedSuggestionProvider
import net.minecraft.command.CommandSource
import java.util.concurrent.CompletableFuture

class BillboardArgumentType: ArgumentType<Billboard> {
    companion object {
        fun billboard(): BillboardArgumentType {
            return BillboardArgumentType()
        }
    }

    override fun parse(reader: StringReader): Billboard {
        val string = reader.readString()
        return Billboard[string?: "fixed"]!!
    }

    override fun <S : Any> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        return SharedSuggestionProvider.suggest(
            arrayListOf<String>().apply {
                Billboard.entries.forEach {this.add(it.name.lowercase())}
            },
            context,
            builder
        )
    }
}