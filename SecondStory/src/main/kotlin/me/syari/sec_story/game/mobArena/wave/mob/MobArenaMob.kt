package me.syari.sec_story.game.mobArena.wave.mob

import org.bukkit.Location
import org.bukkit.entity.LivingEntity

open class MobArenaMob(val priority: Int){
    open fun spawn(healthMulti: Double, loc: Location): LivingEntity?{
        return null
    }
}