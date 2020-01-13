package me.syari.sec_story.paper.core.tour.action

import me.syari.sec_story.paper.library.command.RunCommand.runCommand
import org.bukkit.entity.Player

class TourRunCommand(val cmd: String): TourAction {
    override fun run(p: Player) {
        runCommand(p, cmd.replace("\$player", p.name))
    }
}