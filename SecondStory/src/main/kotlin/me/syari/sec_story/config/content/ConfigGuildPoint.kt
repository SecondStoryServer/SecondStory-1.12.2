package me.syari.sec_story.config.content

import me.syari.sec_story.guild.Guild.guild
import me.syari.sec_story.lib.CustomItemStack
import org.bukkit.Material
import org.bukkit.entity.Player

class ConfigGuildPoint(val value: Int) : ConfigContent() {
    override fun add(p: Player) {
        val g = p.guild ?: return
        g.point += value
    }

    override fun rem(p: Player) {
        val g = p.guild ?: return
        g.point -= value
    }

    override fun has(p: Player): Boolean {
        val g = p.guild ?: return false
        return value <= g.point
    }

    override fun display(p: Player) = CustomItemStack(Material.BLAZE_POWDER, "&c${String.format("%,d", value)}GP", "&aギルドクエストを完了させることで手に入るポイント", "&d現在のギルドポイント: ${String.format("%,d", p.guild?.point ?: 0)}")
}