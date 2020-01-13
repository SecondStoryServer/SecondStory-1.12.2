package me.syari.sec_story.paper.library.config.content

import org.bukkit.entity.Player

interface ConfigContentRemove: ConfigContentItem {
    fun remove(p: Player)

    fun has(p: Player): Boolean
}