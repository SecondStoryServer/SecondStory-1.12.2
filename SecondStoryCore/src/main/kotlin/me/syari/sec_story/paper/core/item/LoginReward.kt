package me.syari.sec_story.paper.core.item

import me.syari.sec_story.paper.core.item.GiveItem.give
import me.syari.sec_story.paper.library.item.CustomItemStack
import org.bukkit.entity.Player

object LoginReward {
    var daily = listOf<CustomItemStack>()
    var first = listOf<CustomItemStack>()

    fun Player.getDaily() {
        give(daily, postName = "&dログイン報酬", postPeriod = 14)
    }

    fun Player.getFirst() {
        give(first, postName = "&d初ログイン報酬", postPeriod = 14)
    }
}