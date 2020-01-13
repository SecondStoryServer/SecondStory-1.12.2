package me.syari.sec_story.paper.core.config.content

import me.syari.sec_story.paper.core.rpg.RPG.eme
import me.syari.sec_story.paper.library.config.content.ConfigContentAdd
import me.syari.sec_story.paper.library.config.content.ConfigContentRemove
import me.syari.sec_story.paper.library.item.CustomItemStack
import org.bukkit.Material
import org.bukkit.entity.Player

class ConfigMoneyEme(val value: Int): ConfigContentAdd, ConfigContentRemove {
    override fun add(p: Player) {
        p.eme += value
    }

    override fun remove(p: Player) {
        p.eme -= value
    }

    override fun has(p: Player): Boolean {
        return value <= p.eme
    }

    override fun display(p: Player) = CustomItemStack(
        Material.EMERALD, "&a${String.format("%,d", value)}EME"
    )
}