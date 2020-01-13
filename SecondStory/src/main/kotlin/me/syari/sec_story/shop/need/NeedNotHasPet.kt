package me.syari.sec_story.shop.need

import me.syari.sec_story.hook.MyPet.hasPet
import org.bukkit.entity.Player

class NeedNotHasPet : Need {
    override fun check(p: Player): Boolean {
        return !p.hasPet
    }

    override val reqMessage = "&cペットを持っています"
}