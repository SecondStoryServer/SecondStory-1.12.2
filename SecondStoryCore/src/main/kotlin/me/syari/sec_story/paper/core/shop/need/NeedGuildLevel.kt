package me.syari.sec_story.paper.core.shop.need

import me.syari.sec_story.paper.core.guild.Guild.guild
import org.bukkit.entity.Player

class NeedGuildLevel(private val lv: Int): Need {
    override fun check(p: Player): Boolean {
        val g = p.guild ?: return false
        return lv <= g.altarData.level
    }

    override val reqMessage = "&cギルド祭壇を${lv}レベル以上にする必要があります"
}