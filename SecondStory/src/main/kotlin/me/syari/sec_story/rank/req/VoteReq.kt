package me.syari.sec_story.rank.req

import me.syari.sec_story.item.Vote.voteCnt
import org.bukkit.entity.Player

data class VoteReq(val count: Int) : RankReq {
    override fun check(p: Player) = count <= p.voteCnt
}