package me.syari.sec_story.shop.need

import me.syari.sec_story.player.Donate.donate
import org.bukkit.entity.Player

class NeedDonate(private val value: Int): Need {
    override fun check(p: Player): Boolean {
        return value <= p.donate
    }

    override val reqMessage = "&c寄付者限定です ${value}円以上"
}