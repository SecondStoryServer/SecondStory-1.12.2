package me.syari.sec_story.paper.core.measure

import me.syari.sec_story.paper.library.init.EventInit
import me.syari.sec_story.paper.library.item.CustomItemStack
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.CraftItemEvent

object CancelHasLoreItemCraft: EventInit {
    @EventHandler
    fun on(e: CraftItemEvent) {
        for(i in 0..8) {
            val item = CustomItemStack(e.clickedInventory.getItem(i))
            if(item.hasLore) {
                e.isCancelled = true
                return
            }
        }
    }
}