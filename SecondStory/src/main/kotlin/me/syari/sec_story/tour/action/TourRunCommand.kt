package me.syari.sec_story.tour.action

import me.syari.sec_story.plugin.Plugin.cmd
import org.bukkit.entity.Player

class TourRunCommand(val cmd: String): TourAction {
    override fun run(p: Player) {
        p.cmd(cmd.replace("\$player", p.name))
    }
}