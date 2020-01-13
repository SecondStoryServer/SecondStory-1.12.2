package me.syari.sec_story.paper.core.game.mobArena.wave.mob

import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter
import me.syari.sec_story.paper.core.hook.MythicMobs.getMythicMobs
import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity

class MobArenaMythicMobsMob(val id: String, priority: Int): MobArenaMob(priority) {
    override fun spawn(healthMulti: Double, loc: Location): LivingEntity? {
        val mob = getMythicMobs(id) ?: return null
        // mob.baseHealth *= healthMulti
        val e = mob.spawn(BukkitAdapter.adapt(loc), 1).livingEntity ?: return null
        e.getAttribute(Attribute.GENERIC_MAX_HEALTH).baseValue *= healthMulti
        e.health *= healthMulti
        return e
    }
}