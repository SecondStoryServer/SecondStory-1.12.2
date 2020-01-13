package me.syari.sec_story.paper.core.tour.action

import me.syari.sec_story.paper.library.message.SendMessage.action
import org.bukkit.entity.Player

class TourActionMessage(private val msg: String): TourAction {
    override fun run(p: Player) {
        p.action(msg.replace("\$player", p.displayName))
    }
}