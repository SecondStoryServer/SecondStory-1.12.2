package me.syari.sec_story.paper.core.shop.need

import org.bukkit.entity.Player

class NeedOP: Need {
    override fun check(p: Player): Boolean {
        return p.isOp
    }

    override val reqMessage = "&c未実装です"
}