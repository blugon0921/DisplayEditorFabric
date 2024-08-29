package kr.blugon.displayeditorfabric.client.api.display

import kr.blugon.displayeditorfabric.client.api.editNbt
import net.minecraft.block.BlockState
import net.minecraft.entity.decoration.DisplayEntity.BlockDisplayEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtHelper


var BlockDisplayEntity.blockState: BlockState?
    get() = this.data?.blockState
    set(value) {
        editNbt {
            if(value == null) {
                it.getCompound("block_state").also { block_state->
                    block_state.putString("Name", "")
                    block_state.put("Properties", NbtCompound())
                    return@editNbt
                }
            }
            it.put("block_state", NbtHelper.fromBlockState(value))
//            it.getCompound("block_state").also { block_state->
//                if(value == null) {
//                    block_state.putString("Name", "")
//                    block_state.put("Properties", NbtCompound())
//                    return@editNbt
//                }
//                block_state.putString("Name", value.block.id)
//                block_state.put("Properties", NbtCompound().apply {
//                    for (entry: Map.Entry<Property<*>, Comparable<*>> in value.entries) {
//                        this.putString(entry.key.name, entry.value.toString().lowercase())
//                    }
//                })
//            }
        }
    }