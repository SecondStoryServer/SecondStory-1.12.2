package me.syari.sec_story.paper.library.item

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object ItemStackPlus {
    fun Player.removeItem(item: ItemStack) {
        val inv = inventory.contents
        var count = item.amount
        inv.forEach { f ->
            if(f != null && f.type == item.type && f.durability == item.durability && (f.hasItemMeta() == item.hasItemMeta()) && f.itemMeta?.displayName == item.itemMeta?.displayName) {
                val a = f.amount
                if(a < count) {
                    count -= a
                    f.amount = 0
                } else {
                    f.amount = a - count
                    inventory.contents = inv
                    return
                }
            }
        }
    }
}