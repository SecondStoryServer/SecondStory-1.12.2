package me.syari.sec_story.paper.core.game.mobArena.wave.boss.skill

import me.syari.sec_story.paper.core.game.mobArena.data.MobArenaData
import org.bukkit.entity.LivingEntity

open class MobArenaBossSkillBase(val name: String) {
    open fun run(e: LivingEntity, arena: MobArenaData) {

    }
}