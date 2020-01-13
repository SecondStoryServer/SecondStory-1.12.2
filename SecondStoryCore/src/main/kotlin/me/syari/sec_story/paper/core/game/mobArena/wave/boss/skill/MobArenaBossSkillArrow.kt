package me.syari.sec_story.paper.core.game.mobArena.wave.boss.skill

import me.syari.sec_story.paper.core.game.mobArena.data.MobArenaData
import org.bukkit.entity.Arrow
import org.bukkit.entity.LivingEntity

class MobArenaBossSkillArrow: MobArenaBossSkillBase("アロー") {
    override fun run(e: LivingEntity, arena: MobArenaData) {
        e.launchProjectile(Arrow::class.java)
    }
}