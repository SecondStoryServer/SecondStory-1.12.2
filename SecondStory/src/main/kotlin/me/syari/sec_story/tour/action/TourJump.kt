package me.syari.sec_story.tour.action

import me.syari.sec_story.lib.message.SendMessage
import me.syari.sec_story.lib.message.SendMessage.send
import me.syari.sec_story.tour.Tour.getPlay
import org.bukkit.entity.Player

class TourJump(val to: String, val prefix: String, val message: String, val suffix: String): TourAction {
    override fun run(p: Player) {
        val play = p.getPlay() ?: return
        val number = play.number
        p.send(prefix to null, message to SendMessage.Action(hover = "&aクリック", click = SendMessage.ClickType.RunCommand to "/tour $number $to"), suffix to null)
    }
}