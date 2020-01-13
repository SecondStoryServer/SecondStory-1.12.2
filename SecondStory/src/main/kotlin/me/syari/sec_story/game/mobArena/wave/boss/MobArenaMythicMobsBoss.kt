package me.syari.sec_story.game.mobArena.wave.boss

import me.syari.sec_story.game.mobArena.data.MobArenaData
import me.syari.sec_story.hook.MythicMobs.spawnMythicMobs
import org.bukkit.Location

class MobArenaMythicMobsBoss(val id: String): MobArenaBoss {
    override fun spawn(loc: Location, arena: MobArenaData) = spawnMythicMobs(id, loc)?.livingEntity
}