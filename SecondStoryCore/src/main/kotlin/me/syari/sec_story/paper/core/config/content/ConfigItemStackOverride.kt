package me.syari.sec_story.paper.core.config.content

import me.syari.sec_story.paper.core.item.GiveItem.give
import me.syari.sec_story.paper.library.config.content.ConfigItemStack
import me.syari.sec_story.paper.library.item.CustomItemStack
import org.bukkit.entity.Player

class ConfigItemStackOverride(item: CustomItemStack): ConfigItemStack(item) {
    override fun add(p: Player) {
        p.give(item, ignore = true)
    }

    override fun display(p: Player) = item
}