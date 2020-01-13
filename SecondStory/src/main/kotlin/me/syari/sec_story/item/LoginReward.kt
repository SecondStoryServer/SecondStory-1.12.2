package me.syari.sec_story.item

import me.syari.sec_story.lib.CustomItemStack
import me.syari.sec_story.lib.ItemStackPlus.give
import org.bukkit.entity.Player

object LoginReward{
    var daily = listOf<CustomItemStack>()
    var first = listOf<CustomItemStack>()

    fun Player.getDaily(){
        give(daily, postName = "&dログイン報酬")
    }

    fun Player.getFirst(){
        give(first, postName = "&d初ログイン報酬")
    }
}