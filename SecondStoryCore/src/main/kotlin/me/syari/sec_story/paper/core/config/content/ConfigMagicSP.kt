package me.syari.sec_story.paper.core.config.content

import me.syari.sec_story.paper.core.hook.Magic.getMage
import me.syari.sec_story.paper.library.config.content.ConfigContentAdd
import me.syari.sec_story.paper.library.config.content.ConfigContentRemove
import me.syari.sec_story.paper.library.item.CustomItemStack
import org.bukkit.Material
import org.bukkit.entity.Player

class ConfigMagicSP(val value: Int): ConfigContentAdd, ConfigContentRemove {
    override fun add(p: Player) {
        val m = getMage(p) ?: return
        m.skillPoints += value
    }

    override fun remove(p: Player) {
        val m = getMage(p) ?: return
        m.skillPoints -= value
    }

    override fun has(p: Player): Boolean {
        val m = getMage(p) ?: return false
        return value <= m.skillPoints
    }

    override fun display(p: Player) = CustomItemStack(
        Material.INK_SACK,
        "&d${String.format("%,d", value)}SP",
        "&a魔法を使用することで手に入るポイント",
        "&d現在のスキルポイント: ${String.format("%,d", getMage(p)?.skillPoints ?: 0)}",
        durability = 4
    )
}