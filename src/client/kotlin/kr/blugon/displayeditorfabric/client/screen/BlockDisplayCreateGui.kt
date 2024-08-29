package kr.blugon.displayeditorfabric.client.screen

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription
import io.github.cottonmc.cotton.gui.widget.*
import io.github.cottonmc.cotton.gui.widget.data.Insets
import io.github.cottonmc.cotton.gui.widget.icon.ItemIcon
import kr.blugon.displayeditorfabric.client.api.literal
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.block.BlockTypes
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.Text

@Environment(EnvType.CLIENT)
class BlockDisplayCreateGui: LightweightGuiDescription() {

    init {
        val root = WGridPanel()
        setRootPanel(root)
        root.setSize(300, 200)
        root.insets = Insets.ROOT_PANEL

        val blockName = WTextField().apply {
            this.text = "minecraft:oak_log"
        }

        root.add(WItemSlot.of(MinecraftClient.getInstance().player!!.inventory, 0), 0, 0, 1, 1)
        root.add(WItem(ItemStack(Items.OAK_LOG)), 0, 0, 1, 1)
        root.add(blockName, 1, 0, 1, 5)

        root.button("취소".literal, 9, 9, 3, 1, button = {

        }) {

        }
        root.button("완료".literal, 13, 9, 3, 1) {

        }

//        root.add(WTabPanel())

        root.add(WLabel("Test".literal, 0xFFFFFF), 0, 6, 2, 1)

        root.validate(this)
    }
}

fun WGridPanel.button(text: Text,x: Int, y: Int, width: Int, height: Int, button: (WButton) -> Unit = {}, onClick: () -> Unit) {
    this.add(WButton(text).also(button).setOnClick {
        onClick()
    }, x, y, width, height)
}