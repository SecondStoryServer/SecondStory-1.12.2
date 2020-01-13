package me.syari.sec_story.game.mobArena.wave.boss.skill

import me.syari.sec_story.game.mobArena.data.MobArenaData
import org.bukkit.entity.LivingEntity

class MobArenaBossSkillGather: MobArenaBossSkillBase("ギャザー"){
    override fun run(e: LivingEntity, arena: MobArenaData) {
        arena.getLivingPlayers().forEach { it.player.teleport(e) }
    }
}