package me.syari.sec_story.config.content

import me.syari.sec_story.lib.CustomItemStack
import me.syari.sec_story.player.Money.money
import org.bukkit.Material
import org.bukkit.entity.Player

class ConfigMoneyJPY(val value: Long) : ConfigContent(){
    override fun add(p: Player) {
        p.money += value
    }

    override fun rem(p: Player) {
        p.money -= value
    }

    override fun has(p: Player): Boolean {
        return value <= p.money
    }

    override fun display(p: Player) = CustomItemStack(Material.GOLD_INGOT, "&6${String.format("%,d", value)}JPY")
}