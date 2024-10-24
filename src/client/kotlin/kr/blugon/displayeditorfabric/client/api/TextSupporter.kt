package kr.blugon.displayeditorfabric.client.api

import net.minecraft.block.Block
import net.minecraft.component.DataComponentTypes
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.item.ItemStack
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.*
import net.minecraft.text.HoverEvent.ItemStackContent
import net.minecraft.util.Formatting
import net.minecraft.util.Rarity


val String?.literal: MutableText
    get() = Text.literal(this?: "null")

val Block.translateKey: TranslationKey get() = TranslationKey(this.translationKey)
//val ItemStack.translateKey: TranslationKey get() = TranslationKey(this.item.translationKey)
val EntityType<*>.translateKey: TranslationKey get() = TranslationKey(this.translationKey)

fun Block.displayName(): MutableText = this.translateKey.translate()
fun ItemStack.displayName(): MutableText = ( //개같네
        "[".literal.setStyle(Style.EMPTY.withColor(this.rarity.formatting))
    .append("".literal.append(if(this.isEmpty) "공기".literal else this.name).also {
        if(this.isEmpty) return@also
        if(this.components.get(DataComponentTypes.CUSTOM_NAME) != null) {
            it.style = it.style.withItalic(true)
        }
        if(this.name.style.color == null) return@also
        val hasEnchantment = this.hasEnchantments()
        val rarity = this.rarity
        if(hasEnchantment) {
            if(rarity < Rarity.RARE) it.withColor(Formatting.AQUA.colorValue?: 0)
            else it.withColor(Formatting.LIGHT_PURPLE.colorValue?: 0)
        } else {
            when(rarity) {
                Rarity.UNCOMMON -> it.withColor(Formatting.YELLOW.colorValue?: 0)
                Rarity.RARE -> it.withColor(Formatting.AQUA.colorValue?: 0)
                Rarity.EPIC -> it.withColor(Formatting.LIGHT_PURPLE.colorValue?: 0)
                else -> it.withColor(Formatting.WHITE.colorValue?: 0)
            }
        }
    })
    .append("]".literal.setStyle(Style.EMPTY.withColor(this.rarity.formatting)))).styled {
        if(this.isEmpty) return@styled Style.EMPTY
        it.withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_ITEM, ItemStackContent(this)))
    }

fun Entity.displayName(): MutableText {
    return if(this.displayName != null)"".literal.append(this.displayName) else this.type.translateKey.translate()
}

fun ServerCommandSource.sendFeedback(text: Text) = this.sendFeedback({ text }, false)
fun ServerCommandSource.sendFeedback(text: String) = this.sendFeedback({ Text.literal(text) }, false)
fun MutableText.color(color: NamedTextColor): Text {
    return this.formatted(color.textColor)
}
enum class NamedTextColor(val textColor: Formatting) {
    BLACK(Formatting.BLACK),
    DARK_BLUE(Formatting.DARK_BLUE),
    DARK_GREEN(Formatting.DARK_GREEN),
    DARK_AQUA(Formatting.DARK_AQUA),
    DARK_RED(Formatting.DARK_RED),
    DARK_PURPLE(Formatting.DARK_PURPLE),
    GOLD(Formatting.GOLD),
    GRAY(Formatting.GRAY),
    DARK_GRAY(Formatting.DARK_GRAY),
    BLUE(Formatting.BLUE),
    GREEN(Formatting.GREEN),
    AQUA(Formatting.AQUA),
    RED(Formatting.RED),
    LIGHT_PURPLE(Formatting.LIGHT_PURPLE),
    YELLOW(Formatting.YELLOW),
    WHITE(Formatting.WHITE),
    OBFUSCATED(Formatting.OBFUSCATED),
    BOLD(Formatting.BOLD),
    STRIKETHROUGH(Formatting.STRIKETHROUGH),
    UNDERLINE(Formatting.UNDERLINE),
    ITALIC(Formatting.ITALIC),
    RESET(Formatting.RESET)
}

class TranslationKey(val key: String) {
    fun translate(): MutableText = Text.translatable(this.key)
}