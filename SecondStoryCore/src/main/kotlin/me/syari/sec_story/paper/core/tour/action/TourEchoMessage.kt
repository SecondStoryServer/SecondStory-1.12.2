package me.syari.sec_story.paper.core.tour.action

import me.syari.sec_story.paper.library.message.SendMessage.send
import org.bukkit.entity.Player

class TourEchoMessage(private val msg: String): TourAction {
    override fun run(p: Player) {
        p.send(msg.replace("\$player", p.displayName))
    }
}