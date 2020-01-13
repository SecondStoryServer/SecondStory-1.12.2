package me.syari.sec_story.paper.library.command

import me.syari.sec_story.paper.library.Main.Companion.plugin
import org.bukkit.command.CommandSender

object RunCommand {
    fun runCommand(sender: CommandSender, cmd: String) {
        plugin.server.dispatchCommand(sender, cmd)
    }

    fun runCommandFromConsole(cmd: String) {
        runCommand(plugin.server.consoleSender, cmd)
    }
}