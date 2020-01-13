package me.syari.sec_story.paper.core.rank.req

import me.syari.sec_story.paper.core.vote.Vote.voteCnt
import org.bukkit.entity.Player

data class VoteReq(val count: Int): RankReq {
    override fun check(p: Player) = count <= p.voteCnt
}