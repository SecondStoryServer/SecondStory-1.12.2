package me.syari.sec_story.config.content

import me.syari.sec_story.lib.CustomItemStack
import me.syari.sec_story.lib.ItemStackPlus.give
import me.syari.sec_story.lib.ItemStackPlus.removeItem
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType

class ConfigItemStack(val item: CustomItemStack) : ConfigContent(){
    override fun add(p: Player) {
        p.give(item, ignore = true)
    }

    override fun rem(p: Player) {
        p.removeItem(item.toOneItemStack)
    }

    override fun has(p: Player): Boolean {
        var count = item.amount
        val inv = Bukkit.createInventory(null, InventoryType.PLAYER)
        inv.contents = p.inventory.contents
        inv.contents.forEach { f ->
            val cf = CustomItemStack(f)
            if(cf.type == item.type && cf.durability == item.durability && (cf.hasItemMeta == item.hasItemMeta)&& cf.display == item.display) {
                val a = f.amount
                if(a < count){
                    count -= a
                    f.amount = 0
                } else {
                    f.amount = a - count
                    return true
                }
            }
        }
        return false
    }

    override fun display(p: Player) = item
}