package me.syari.sec_story.paper.core.rank.req

import me.syari.sec_story.paper.core.guild.Guild.guildPlayer
import org.bukkit.entity.Player

data class WarReq(val win: Int): RankReq {
    override fun check(p: Player) = win <= p.guildPlayer.win
}