package me.syari.sec_story.paper.core.rank.req

import me.syari.sec_story.paper.core.player.Time.time
import org.bukkit.entity.Player

data class TimeReq(val minutes: Int): RankReq {
    override fun check(p: Player) = minutes <= p.time
}