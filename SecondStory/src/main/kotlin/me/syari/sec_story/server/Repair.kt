package me.syari.sec_story.server

import me.syari.sec_story.lib.CustomItemStack
import me.syari.sec_story.plugin.Init
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

object Repair : Listener, Init() {
    var RepairItem: CustomItemStack? = null

    @EventHandler
    fun on(e: InventoryClickEvent){
        val cursor = e.cursor ?: return
        if(RepairItem?.isSimilar(cursor) == true){
            val target = e.currentItem ?: return
            val max = target.type.maxDurability
            if(max > 0 && target.durability > 0){
                e.currentItem.durability = 0
                e.cursor.amount --
                e.isCancelled = true
            }
        }
    }
}