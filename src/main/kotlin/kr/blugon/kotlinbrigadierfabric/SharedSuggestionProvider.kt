package kr.blugon.kotlinbrigadierfabric

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.command.CommandSource
import net.minecraft.server.command.ServerCommandSource
import java.util.concurrent.CompletableFuture


class SharedSuggestionProvider(val suggestions: List<String>): SuggestionProvider<ServerCommandSource> {

    companion object {
        fun suggest(suggestions: List<String>, context: CommandContext<ServerCommandSource>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
            return SharedSuggestionProvider(suggestions).getSuggestions(context, builder)
        }
    }

    override fun getSuggestions(context: CommandContext<ServerCommandSource>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        suggestions.forEach {
            if(!CommandSource.shouldSuggest(builder.remaining, it)) return@forEach
            builder.suggest(it)
        }

        return builder.buildFuture()
    }
}