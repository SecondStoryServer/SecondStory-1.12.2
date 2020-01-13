package me.syari.sec_story.shop.other

import me.syari.sec_story.plugin.Plugin.cmd
import me.syari.sec_story.plugin.Plugin.console
import me.syari.sec_story.shop.Shop
import me.syari.sec_story.shop.need.NeedList
import org.bukkit.entity.Player

class CmdShop(npc: String, id: String, name: String, private val cmd: String, private val console: Boolean, val need: NeedList) : Shop(npc, id, name, 1){
    override fun open(p: Player) {
        if(need.getDisplay(p).isNotEmpty()) return
        val c = cmd.replace("{sender}", p.name)
        if(console){
            console(c)
        } else {
            p.cmd(c)
        }
    }
}