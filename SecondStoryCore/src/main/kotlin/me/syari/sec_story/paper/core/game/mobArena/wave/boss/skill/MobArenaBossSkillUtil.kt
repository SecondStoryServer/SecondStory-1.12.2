package me.syari.sec_story.paper.core.game.mobArena.wave.boss.skill

import me.syari.sec_story.paper.core.game.mobArena.data.MobArenaData
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

object MobArenaBossSkillUtil {
    fun getNearByPlayers(arena: MobArenaData, entity: LivingEntity, radius: Double): Set<Player> {
        val ret = mutableSetOf<Player>()
        entity.getNearbyEntities(radius, radius, radius).forEach { f ->
            if(f is Player) {
                val m = arena.getPlayer(f) ?: return@forEach
                if(m.play) {
                    ret.add(m.player)
                }
            }
        }
        return ret
    }
}