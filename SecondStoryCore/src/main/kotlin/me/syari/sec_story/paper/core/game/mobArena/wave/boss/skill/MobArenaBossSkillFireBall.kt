package me.syari.sec_story.paper.core.game.mobArena.wave.boss.skill

import me.syari.sec_story.paper.core.game.mobArena.data.MobArenaData
import org.bukkit.entity.Fireball
import org.bukkit.entity.LivingEntity

class MobArenaBossSkillFireBall: MobArenaBossSkillBase("ファイアーボール") {
    override fun run(e: LivingEntity, arena: MobArenaData) {
        e.launchProjectile(Fireball::class.java)
    }
}
