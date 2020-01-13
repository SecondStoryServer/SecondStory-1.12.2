package me.syari.sec_story.game.mobArena.wave.boss.skill

import me.syari.sec_story.game.mobArena.data.MobArenaData
import org.bukkit.entity.LivingEntity


class MobArenaBossSkillFire(private val tick: Int, private val radius: Double): MobArenaBossSkillBase("ファイアー"){
    override fun run(e: LivingEntity, arena: MobArenaData) {
        for (p in MobArenaBossSkillUtil.getNearByPlayers(arena, e, radius)) {
            p.fireTicks = tick
        }
    }
}