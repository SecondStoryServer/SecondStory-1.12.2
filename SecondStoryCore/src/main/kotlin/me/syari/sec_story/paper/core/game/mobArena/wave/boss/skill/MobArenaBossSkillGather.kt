package me.syari.sec_story.paper.core.game.mobArena.wave.boss.skill

import me.syari.sec_story.paper.core.game.mobArena.data.MobArenaData
import org.bukkit.entity.LivingEntity

class MobArenaBossSkillGather: MobArenaBossSkillBase("ギャザー") {
    override fun run(e: LivingEntity, arena: MobArenaData) {
        arena.livingPlayers.forEach { it.player.teleport(e) }
    }
}