package me.syari.sec_story.paper.core.game.mobArena.wave.boss.skill

import me.syari.sec_story.paper.core.game.mobArena.data.MobArenaData
import org.bukkit.entity.LivingEntity
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class MobArenaBossSkillFreeze(private val duration: Int, private val radius: Double): MobArenaBossSkillBase("フリーズ") {
    override fun run(e: LivingEntity, arena: MobArenaData) {
        for(p in MobArenaBossSkillUtil.getNearByPlayers(arena, e, radius)) {
            p.addPotionEffect(PotionEffect(PotionEffectType.SLOW, duration, 10))
        }
    }
}