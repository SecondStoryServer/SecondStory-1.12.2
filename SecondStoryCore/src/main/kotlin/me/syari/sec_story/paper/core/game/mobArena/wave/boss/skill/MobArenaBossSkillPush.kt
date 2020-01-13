package me.syari.sec_story.paper.core.game.mobArena.wave.boss.skill

import me.syari.sec_story.paper.core.game.mobArena.data.MobArenaData
import org.bukkit.entity.LivingEntity
import org.bukkit.util.Vector

class MobArenaBossSkillPush(private val radius: Double): MobArenaBossSkillBase("フキトバシ") {
    override fun run(e: LivingEntity, arena: MobArenaData) {
        val eLoc = e.location

        for(p in MobArenaBossSkillUtil.getNearByPlayers(arena, e, radius)) {
            val loc = p.location
            val v = Vector(loc.x - eLoc.x, 0.0, loc.z - eLoc.z)
            p.velocity = v.normalize().setY(0.8)
        }
    }
}