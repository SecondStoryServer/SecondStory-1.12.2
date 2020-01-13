package me.syari.sec_story.paper.core.shop.need

import org.bukkit.entity.Player

class NeedPerm(private val perm: String): Need {
    override fun check(p: Player): Boolean {
        return p.hasPermission(perm)
    }

    override val reqMessage = "&c必要権限がありません"
}