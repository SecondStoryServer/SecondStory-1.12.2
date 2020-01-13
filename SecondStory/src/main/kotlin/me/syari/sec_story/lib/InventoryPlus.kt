package me.syari.sec_story.lib

import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack

object InventoryPlus {
    val InventoryClickEvent.insertItem get(): ItemStack?{
        val p = whoClicked as? Player ?: return null
        return if(p.openInventory.topInventory.type != InventoryType.CRAFTING){
            if(clickedInventory == p.inventory){
                currentItem
            } else if(clickedInventory != p.inventory){
                if(click == ClickType.NUMBER_KEY){
                    p.inventory.getItem(hotbarButton)
                } else {
                    cursor
                }
            } else {
                null
            }
        } else {
            null
        }
    }
}