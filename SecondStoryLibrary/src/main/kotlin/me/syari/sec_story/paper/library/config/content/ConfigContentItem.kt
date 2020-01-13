package me.syari.sec_story.paper.library.config.content

import me.syari.sec_story.paper.library.item.CustomItemStack
import org.bukkit.entity.Player

interface ConfigContentItem: ConfigContent {
    fun display(p: Player): CustomItemStack
}