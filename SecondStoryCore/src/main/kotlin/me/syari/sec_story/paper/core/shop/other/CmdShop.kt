package me.syari.sec_story.paper.core.shop.other

import me.syari.sec_story.paper.core.shop.Shop
import me.syari.sec_story.paper.core.shop.need.NeedList
import me.syari.sec_story.paper.library.command.RunCommand.runCommand
import me.syari.sec_story.paper.library.command.RunCommand.runCommandFromConsole
import org.bukkit.entity.Player

class CmdShop(
    npc: String, id: String, name: String, private val cmd: String, private val console: Boolean, val need: NeedList
): Shop(npc, id, name, 1) {
    override fun open(p: Player) {
        if(need.getDisplay(p).isNotEmpty()) return
        val c = cmd.replace("{sender}", p.name)
        if(console) {
            runCommandFromConsole(c)
        } else {
            runCommand(p, c)
        }
    }
}