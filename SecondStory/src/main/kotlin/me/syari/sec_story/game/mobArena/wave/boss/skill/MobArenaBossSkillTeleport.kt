package me.syari.sec_story.game.mobArena.wave.boss.skill

import me.syari.sec_story.game.mobArena.data.MobArenaData
import org.bukkit.entity.LivingEntity

class MobArenaBossSkillTeleport: MobArenaBossSkillBase("テレポート"){
    override fun run(e: LivingEntity, arena: MobArenaData) {
        e.teleport(arena.getLivingPlayers().random().player)
    }
}