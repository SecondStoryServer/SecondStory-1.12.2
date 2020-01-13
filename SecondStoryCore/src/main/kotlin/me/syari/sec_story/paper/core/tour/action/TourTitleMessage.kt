package me.syari.sec_story.paper.core.tour.action

import me.syari.sec_story.paper.library.message.SendMessage.title
import org.bukkit.entity.Player

class TourTitleMessage(
    private val main: String, private val sub: String, private val fadeIn: Int, private val stay: Int, private val fadeOut: Int
): TourAction {
    override fun run(p: Player) {
        p.title(main.replace("\$player", p.displayName), sub.replace("\$player", p.displayName), fadeIn, stay, fadeOut)
    }
}