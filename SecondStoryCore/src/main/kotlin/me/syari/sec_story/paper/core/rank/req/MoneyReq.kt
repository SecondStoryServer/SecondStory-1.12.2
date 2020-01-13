package me.syari.sec_story.paper.core.rank.req

import me.syari.sec_story.paper.core.player.Money.hasMoney
import org.bukkit.entity.Player

data class MoneyReq(val money: Long): RankReq {
    override fun check(p: Player) = p.hasMoney(money)
}