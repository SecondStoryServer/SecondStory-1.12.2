package me.syari.sec_story.paper.core.shop.jump

import me.syari.sec_story.paper.library.item.CustomItemStack
import org.bukkit.entity.Player

interface Jump {
    fun run(p: Player)

    fun getDisplay(p: Player): CustomItemStack
}