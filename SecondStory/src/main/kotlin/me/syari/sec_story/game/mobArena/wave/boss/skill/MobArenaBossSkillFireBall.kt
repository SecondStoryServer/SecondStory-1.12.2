package me.syari.sec_story.game.mobArena.wave.boss.skill

import me.syari.sec_story.game.mobArena.data.MobArenaData
import org.bukkit.entity.Fireball
import org.bukkit.entity.LivingEntity

class MobArenaBossSkillFireBall : MobArenaBossSkillBase("ファイアーボール") {
    override fun run(e: LivingEntity, arena: MobArenaData) {
        e.launchProjectile(Fireball::class.java)
    }
}
