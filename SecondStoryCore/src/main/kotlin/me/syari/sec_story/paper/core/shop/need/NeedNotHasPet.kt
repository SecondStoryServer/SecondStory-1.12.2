package me.syari.sec_story.paper.core.shop.need

import me.syari.sec_story.paper.core.hook.MyPet.hasPet
import org.bukkit.entity.Player

class NeedNotHasPet: Need {
    override fun check(p: Player): Boolean {
        return ! p.hasPet
    }

    override val reqMessage = "&cペットを持っています"
}