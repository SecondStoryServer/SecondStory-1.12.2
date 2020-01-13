package me.syari.sec_story.paper.core.config.content

import me.syari.sec_story.paper.core.player.Money.money
import me.syari.sec_story.paper.library.config.content.ConfigContentAdd
import me.syari.sec_story.paper.library.config.content.ConfigContentRemove
import me.syari.sec_story.paper.library.item.CustomItemStack
import org.bukkit.Material
import org.bukkit.entity.Player

class ConfigMoneyJPY(val value: Long): ConfigContentAdd, ConfigContentRemove {
    override fun add(p: Player) {
        p.money += value
    }

    override fun remove(p: Player) {
        p.money -= value
    }

    override fun has(p: Player): Boolean {
        return value <= p.money
    }

    override fun display(p: Player) = CustomItemStack(Material.GOLD_INGOT, "&6${String.format("%,d", value)}JPY")
}