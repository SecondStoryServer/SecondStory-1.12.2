package me.syari.sec_story.paper.core.game.mobArena.wave.boss.skill

import me.syari.sec_story.paper.core.game.mobArena.data.MobArenaData
import org.bukkit.entity.LivingEntity

class MobArenaBossSkillTeleport: MobArenaBossSkillBase("テレポート") {
    override fun run(e: LivingEntity, arena: MobArenaData) {
        e.teleport(arena.livingPlayers.random().player)
    }
}