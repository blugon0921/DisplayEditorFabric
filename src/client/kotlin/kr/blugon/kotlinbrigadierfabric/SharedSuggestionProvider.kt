package kr.blugon.kotlinbrigadierfabric

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.command.CommandSource
import java.util.concurrent.CompletableFuture


class SharedSuggestionProvider <S> (val suggestions: List<String>): SuggestionProvider<S> {

    companion object {
        fun <S> suggest(suggestions: List<String>, context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
            return SharedSuggestionProvider<S>(suggestions).getSuggestions(context, builder)
        }
    }

    override fun getSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        suggestions.forEach {
            if(!CommandSource.shouldSuggest(builder.remaining, it)) return@forEach
            builder.suggest(it)
        }

        return builder.buildFuture()
    }
}