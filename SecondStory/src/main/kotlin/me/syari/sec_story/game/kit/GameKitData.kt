package me.syari.sec_story.game.kit

import me.syari.sec_story.game.kit.event.KitSetEvent
import me.syari.sec_story.lib.CustomItemStack
import org.bukkit.entity.Player

class GameKitData(val id: String, val name: String, icon: CustomItemStack){
    val icon: CustomItemStack

    init {
        val tmp = icon
        tmp.display = name
        tmp.lore = listOf("&aこのキットを選択する")
        this.icon = tmp
    }

    private val list = mutableMapOf<Int, CustomItemStack>()

    fun addItem(index: Int, item: CustomItemStack){
        list[index] = item
    }

    fun setKit(p: Player){
        p.inventory.clear()
        list.forEach { (index, item) ->
            p.inventory.setItem(index, item.toOneItemStack)
        }
        KitSetEvent(p, this).callEvent()
    }
}