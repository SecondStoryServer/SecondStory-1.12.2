package me.syari.sec_story.paper.core.item

import me.syari.sec_story.paper.core.itemPost.ItemPost.addPost
import me.syari.sec_story.paper.library.Main.Companion.plugin
import me.syari.sec_story.paper.library.code.StringEditor.toColor
import me.syari.sec_story.paper.library.item.CustomItemStack
import me.syari.sec_story.paper.library.scheduler.CustomScheduler.runLater
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

object GiveItem {
    fun OfflinePlayer.give(
        cItem: CustomItemStack, postName: String = "", postPeriod: Int = 7, ignore: Boolean = false
    ) {
        give(listOf(cItem), postName, postPeriod, ignore)
    }

    fun OfflinePlayer.give(
        items: Collection<CustomItemStack>, postName: String = "", postPeriod: Int = 7, ignore: Boolean = false
    ) {
        val e = GiveItemEvent(this, ignore)
        e.callEvent()
        if((! ignore && e.isAddPost) || this !is Player) {
            addPost(postName, postPeriod, items)
        } else {
            val loc = location
            items.forEach { cItem ->
                cItem.toItemStack.forEach { item ->
                    if(inventory.firstEmpty() in 0 until 36) {
                        inventory.addItem(item.clone())
                    } else {
                        val d = loc.world.dropItem(loc, item.clone())
                        d.customName = "&a$displayName".toColor
                        d.isCustomNameVisible = true
                        runLater(plugin, 20) {
                            d.isCustomNameVisible = false
                        }
                    }
                }
            }
        }
    }
}