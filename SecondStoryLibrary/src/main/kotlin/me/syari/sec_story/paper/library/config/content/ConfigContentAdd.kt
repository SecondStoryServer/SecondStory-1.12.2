package me.syari.sec_story.paper.library.config.content

import org.bukkit.entity.Player

interface ConfigContentAdd: ConfigContentItem {
    fun add(p: Player)
}