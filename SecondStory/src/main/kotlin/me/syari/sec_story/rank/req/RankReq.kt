package me.syari.sec_story.rank.req

import org.bukkit.entity.Player

interface RankReq{
    fun check(p: Player): Boolean
}