package me.syari.sec_story.paper.core.tour.action

import me.syari.sec_story.paper.core.tour.Tour.getPlay
import me.syari.sec_story.paper.library.message.JsonAction
import me.syari.sec_story.paper.library.message.JsonClickType
import me.syari.sec_story.paper.library.message.SendMessage.send
import org.bukkit.entity.Player

class TourJump(val to: String, val prefix: String, val message: String, val suffix: String): TourAction {
    override fun run(p: Player) {
        val play = p.getPlay() ?: return
        val number = play.number
        p.send(
            prefix to null,
            message to JsonAction(hover = "&aクリック", click = JsonClickType.RunCommand to "/tour $number $to"),
            suffix to null
        )
    }
}