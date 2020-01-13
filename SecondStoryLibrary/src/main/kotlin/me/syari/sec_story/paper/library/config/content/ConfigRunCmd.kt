package me.syari.sec_story.paper.library.config.content

import me.syari.sec_story.paper.library.command.RunCommand.runCommand
import me.syari.sec_story.paper.library.item.CustomItemStack
import org.bukkit.Material
import org.bukkit.entity.Player

class ConfigRunCmd(private val cmd: String): ConfigContentAdd {
    override fun add(p: Player) {
        runCommand(p, cmd)
    }

    override fun display(p: Player) = CustomItemStack(Material.NAME_TAG, "&f/${cmd}")
}