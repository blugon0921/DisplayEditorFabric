package kr.blugon.displayeditorfabric.client.screen

import io.github.cottonmc.cotton.gui.GuiDescription
import io.github.cottonmc.cotton.gui.client.CottonClientScreen
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandler
import net.minecraft.text.Text

//class BlockDisplayCreateScreen(val handler: ScreenHandler, val inventory: PlayerInventory, private val title: Text) : HandledScreen<ScreenHandler>(handler, inventory, title) {
//
//    override fun drawBackground(context: DrawContext, delta: Float, mouseX: Int, mouseY: Int) {
//        context.fill(x, y, x+width, y+height, 0xFFFFFFFF.toInt())
//    }
//
//    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
//        this.renderBackground(context, mouseX, mouseY, delta)
//        super.render(context, mouseX, mouseY, delta)
//        this.drawMouseoverTooltip(context, mouseX, mouseY)
//    }
//}
class BlockDisplayCreateScreen(description: GuiDescription?) : CottonClientScreen(description) {
}