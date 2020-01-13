package me.syari.sec_story.config.content

import me.syari.sec_story.lib.CustomItemStack
import org.bukkit.Material
import org.bukkit.entity.Player

open class ConfigContent{
    open fun add(p: Player){

    }

    open fun rem(p: Player){

    }

    open fun has(p: Player): Boolean{
        return false
    }

    open fun display(p: Player) = CustomItemStack(Material.STONE)
}