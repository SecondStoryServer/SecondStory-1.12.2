package me.syari.sec_story.game.mobArena.wave.boss

import me.syari.sec_story.game.mobArena.data.MobArenaData
import org.bukkit.Location
import org.bukkit.entity.LivingEntity

interface MobArenaBoss {
    fun spawn(loc: Location, arena: MobArenaData): LivingEntity?
}