package me.syari.sec_story.measure

import me.syari.sec_story.lib.CustomItemStack
import me.syari.sec_story.plugin.Init
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.CraftItemEvent

object CancelHasLoreItemCraft: Init(), Listener {
    @EventHandler
    fun on(e: CraftItemEvent){
        for(i in 0..8){
            val item = CustomItemStack(e.clickedInventory.getItem(i))
            if(item.hasLore){
                e.isCancelled = true
                return
            }
        }
    }
}