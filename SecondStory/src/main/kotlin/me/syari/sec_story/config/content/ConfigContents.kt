package me.syari.sec_story.config.content

import me.syari.sec_story.lib.CustomItemStack
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory

class ConfigContents {
    private val contents = mutableListOf<ConfigContent>()

    fun addContent(c: ConfigContent){
        contents.add(c)
    }

    fun addContentsToPlayer(p: Player){
        contents.forEach { c ->
            c.add(p)
        }
    }

    fun removeContentsFromPlayer(p: Player){
        contents.forEach { c ->
            c.rem(p)
        }
    }

    fun getContents() = contents

    fun hasContents(p: Player): Boolean{
        fun Inventory.rem(cItem: CustomItemStack): Inventory?{
            val item = cItem.toOneItemStack
            var count = item.amount
            forEach { f ->
                if(f != null && f.type == item.type && f.durability == item.durability && (f.hasItemMeta() == item.hasItemMeta())&& f.itemMeta?.displayName == item.itemMeta?.displayName) {
                    val a = f.amount
                    if(a < count){
                        count -= a
                        f.amount = 0
                    } else {
                        f.amount = a - count
                        return this
                    }
                }
            }
            return null
        }

        val inv = Bukkit.createInventory(null, InventoryType.PLAYER)
        inv.contents = p.inventory.contents
        contents.forEach { c ->
            when(c){
                is ConfigExp,
                is ConfigMoneyJPY,
                is ConfigMoneyEme,
                is ConfigMagicSP,
                is ConfigGuildPoint -> if(!c.has(p)) return false
                is ConfigItemStack -> if(inv.rem(c.item) == null) return false
                is ConfigRunCmd,
                is ConfigMyPet -> return false
            }
        }
        return true
    }

    fun isNotEmpty() = contents.any { f -> f !is ConfigContentError }
}