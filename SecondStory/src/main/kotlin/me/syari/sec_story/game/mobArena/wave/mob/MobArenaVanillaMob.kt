package me.syari.sec_story.game.mobArena.wave.mob

import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity

class MobArenaVanillaMob(val type: EntityType, priority: Int): MobArenaMob(priority){
    override fun spawn(healthMulti: Double, loc: Location): LivingEntity? {
        val e= loc.world.spawnEntity(loc, type)
        return if(e is LivingEntity){
            e.getAttribute(Attribute.GENERIC_MAX_HEALTH).baseValue *= healthMulti
            e.health  *= healthMulti
            e
        } else {
            e.remove()
            null
        }
    }
}