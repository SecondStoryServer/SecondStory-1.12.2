package me.syari.sec_story.tour.action

import me.syari.sec_story.lib.message.SendMessage.send
import org.bukkit.entity.Player

class TourEchoMessage(private val msg: String): TourAction {
    override fun run(p: Player) {
        p.send(msg.replace("\$player", p.displayName))
    }
}