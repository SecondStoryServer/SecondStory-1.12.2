package me.syari.sec_story.paper.core.shop.need

import org.bukkit.entity.Player

class NeedEditConfig: Need {
    override fun check(p: Player): Boolean {
        return false
    }

    override val reqMessage = "&cNull."
}