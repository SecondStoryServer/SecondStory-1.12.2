package me.syari.sec_story.paper.core.config.content

import me.syari.sec_story.paper.core.guild.Guild.guild
import me.syari.sec_story.paper.library.config.content.ConfigContentAdd
import me.syari.sec_story.paper.library.config.content.ConfigContentRemove
import me.syari.sec_story.paper.library.item.CustomItemStack
import org.bukkit.Material
import org.bukkit.entity.Player

class ConfigGuildPoint(val value: Int): ConfigContentAdd, ConfigContentRemove {
    override fun add(p: Player) {
        val g = p.guild ?: return
        g.point += value
    }

    override fun remove(p: Player) {
        val g = p.guild ?: return
        g.point -= value
    }

    override fun has(p: Player): Boolean {
        val g = p.guild ?: return false
        return value <= g.point
    }

    override fun display(p: Player) = CustomItemStack(
        Material.BLAZE_POWDER,
        "&c${String.format("%,d", value)}GP",
        "&aギルドクエストを完了させることで手に入るポイント",
        "&d現在のギルドポイント: ${String.format("%,d", p.guild?.point ?: 0)}"
    )
}