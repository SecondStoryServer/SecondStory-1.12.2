package me.syari.sec_story.config.content

import me.syari.sec_story.lib.CustomItemStack
import me.syari.sec_story.rpg.RPG.eme
import org.bukkit.Material
import org.bukkit.entity.Player

class ConfigMoneyEme(val value: Int) : ConfigContent(){
    override fun add(p: Player) {
        p.eme += value
    }

    override fun rem(p: Player) {
        p.eme -= value
    }

    override fun has(p: Player): Boolean {
        return value <= p.eme
    }

    override fun display(p: Player) = CustomItemStack(Material.EMERALD, "&a${String.format("%,d", value)}EME")
}