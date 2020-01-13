package me.syari.sec_story.tour.action

import me.syari.sec_story.lib.message.SendMessage.action
import org.bukkit.entity.Player

class TourActionMessage(private val msg: String): TourAction {
    override fun run(p: Player) {
        p.action(msg.replace("\$player", p.displayName))
    }
}