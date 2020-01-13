package me.syari.sec_story.paper.core.game.mobArena.wave.boss

import me.syari.sec_story.paper.core.game.mobArena.data.MobArenaData
import org.bukkit.Location
import org.bukkit.entity.LivingEntity

interface MobArenaBoss {
    fun spawn(loc: Location, arena: MobArenaData): LivingEntity?
}