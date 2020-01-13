package me.syari.sec_story.paper.core.server

import me.syari.sec_story.paper.library.init.EventInit
import me.syari.sec_story.paper.library.item.CustomItemStack
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.InventoryClickEvent

object Repair: EventInit {
    var RepairItem: CustomItemStack? = null

    @EventHandler
    fun on(e: InventoryClickEvent) {
        val cursor = e.cursor ?: return
        if(RepairItem?.isSimilar(cursor) == true) {
            val target = e.currentItem ?: return
            val max = target.type.maxDurability
            if(max > 0 && target.durability > 0) {
                e.currentItem.durability = 0
                e.cursor.amount --
                e.isCancelled = true
            }
        }
    }
}