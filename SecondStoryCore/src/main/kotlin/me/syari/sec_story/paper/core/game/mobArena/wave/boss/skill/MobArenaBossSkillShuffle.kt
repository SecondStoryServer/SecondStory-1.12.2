package me.syari.sec_story.paper.core.game.mobArena.wave.boss.skill

import me.syari.sec_story.paper.core.game.mobArena.data.MobArenaData
import org.bukkit.entity.LivingEntity

class MobArenaBossSkillShuffle: MobArenaBossSkillBase("シャッフル") {
    override fun run(e: LivingEntity, arena: MobArenaData) {
        val list = arena.livingPlayers.map { it.player }.shuffled()
        val entityLoc = e.location
        e.teleport(list.first())
        for(i in 1 until list.size) {
            list[i - 1].teleport(list[i])
        }
        list.last().teleport(entityLoc)
    }
}