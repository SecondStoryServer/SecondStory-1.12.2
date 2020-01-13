package me.syari.sec_story.game.pvp

import me.syari.sec_story.lib.command.CreateCommand.createCmd
import me.syari.sec_story.lib.command.CreateCommand.element
import me.syari.sec_story.lib.command.CreateCommand.tab
import me.syari.sec_story.plugin.Init
import org.bukkit.entity.Player
import org.bukkit.event.Listener

object PvP: Init(), Listener {
    override fun init() {
        createCmd("pvp",
            tab { element("join", "leave") }
        ){ sender, args ->
            when (args.whenIndex(0)) {
                "join" -> {
                    if (sender is Player) {

                    }
                }
                "leave" -> {
                    if (sender is Player) {

                    }
                }
            }
        }
    }
}