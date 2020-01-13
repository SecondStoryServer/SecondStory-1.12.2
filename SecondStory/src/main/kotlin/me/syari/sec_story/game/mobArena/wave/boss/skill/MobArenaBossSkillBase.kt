package me.syari.sec_story.game.mobArena.wave.boss.skill

import me.syari.sec_story.game.mobArena.data.MobArenaData
import org.bukkit.entity.LivingEntity

open class MobArenaBossSkillBase(val name: String) {
    open fun run(e: LivingEntity, arena: MobArenaData){

    }
}