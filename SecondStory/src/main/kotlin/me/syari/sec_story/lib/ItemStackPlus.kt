package me.syari.sec_story.lib

import me.syari.sec_story.data.SaveData.hasSave
import me.syari.sec_story.item.ItemPost.addPost
import me.syari.sec_story.lib.StringEditor.toColor
import me.syari.sec_story.plugin.Plugin
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable

object ItemStackPlus{
    fun Player.removeItem(item: ItemStack){
        val inv =  inventory.contents
        var count = item.amount
        inv.forEach { f ->
            if(f != null && f.type == item.type && f.durability == item.durability && (f.hasItemMeta() == item.hasItemMeta()) && f.itemMeta?.displayName == item.itemMeta?.displayName) {
                val a = f.amount
                if(a < count){
                    count -= a
                    f.amount = 0
                } else{
                    f.amount = a - count
                    inventory.contents = inv
                    return
                }
            }
        }
    }

    fun OfflinePlayer.give(cItem: CustomItemStack, postName: String = "", postPeriod: Int = 7, ignore: Boolean = false){
        give(listOf(cItem), postName, postPeriod, ignore)
    }

    fun OfflinePlayer.give(items: Collection<CustomItemStack>, postName: String = "", postPeriod: Int = 7, ignore: Boolean = false){
        val bool = (!ignore && hasSave) || (this !is Player)
        if(bool){
            addPost(postName, postPeriod, items)
        } else if (this is Player){
            val loc = location
            items.forEach { cItem ->
                cItem.toItemStack.forEach { item ->
                    if(inventory.firstEmpty() in 0 until 36){
                        inventory.addItem(item.clone())
                    } else {
                        val d = loc.world.dropItem(loc, item.clone())
                        d.customName = "&a$displayName".toColor
                        d.isCustomNameVisible = true
                        object : BukkitRunnable(){
                            override fun run() {
                                d.isCustomNameVisible = false
                            }
                        }.runTaskLater(Plugin.plugin, 20 * 60)
                    }
                }
            }
        }
    }
}