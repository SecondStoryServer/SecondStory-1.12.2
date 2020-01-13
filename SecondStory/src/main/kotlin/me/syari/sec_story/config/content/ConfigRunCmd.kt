package me.syari.sec_story.config.content

import me.syari.sec_story.lib.CustomItemStack
import me.syari.sec_story.plugin.Plugin.cmd
import org.bukkit.Material
import org.bukkit.entity.Player

class ConfigRunCmd(val cmd: String) : ConfigContent() {
    override fun add(p: Player) {
        p.cmd(cmd)
    }

    override fun display(p: Player) = CustomItemStack(Material.NAME_TAG, "&f/${cmd}")
}