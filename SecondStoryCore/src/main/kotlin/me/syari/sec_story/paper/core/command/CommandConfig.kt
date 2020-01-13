package me.syari.sec_story.paper.core.command

import me.syari.sec_story.paper.core.Main.Companion.plugin
import me.syari.sec_story.paper.core.command.CommandCancel.hideTab
import me.syari.sec_story.paper.library.config.CreateConfig.config
import org.bukkit.command.CommandSender

object CommandConfig {
    fun CommandSender.loadCommandConfig() {
        config(plugin, "Command/hide.yml", false) {
            output = this@loadCommandConfig

            hideTab = getStringList("list", listOf())
        }
        config(plugin, "Command/auto.yml", false) {
            output = this@loadCommandConfig

            AutoCommand.clear()
            getSection("")?.forEach { f ->
                AutoCommand.add(
                    f.substringBefore('-').toInt(), f.substringAfter('-'), getStringList(f, listOf()).toSet()
                )
            }
        }
    }
}