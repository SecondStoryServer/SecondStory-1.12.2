package me.syari.sec_story.paper.core.shop.need

import org.bukkit.entity.Player

interface Need {
    fun check(p: Player): Boolean

    val reqMessage: String
}