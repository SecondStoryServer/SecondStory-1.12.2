package me.syari.sec_story.rank.req

import me.syari.sec_story.player.Time.time
import org.bukkit.entity.Player

data class TimeReq(val minutes: Int) : RankReq {
    override fun check(p: Player) = minutes <= p.time
}