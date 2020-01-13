package me.syari.sec_story.rank.req

import org.bukkit.entity.Player

data class ExpReq(val level: Int) : RankReq {
    override fun check(p: Player) = level <= p.level
}