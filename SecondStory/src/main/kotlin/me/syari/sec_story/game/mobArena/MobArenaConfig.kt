package me.syari.sec_story.game.mobArena

import me.syari.sec_story.config.content.ConfigContents
import me.syari.sec_story.game.mobArena.MobArena.setArenas
import me.syari.sec_story.game.mobArena.data.MobArenaData
import me.syari.sec_story.game.mobArena.wave.MobArenaWave
import me.syari.sec_story.game.mobArena.wave.MobArenaWaveAll
import me.syari.sec_story.game.mobArena.wave.boss.MobArenaBoss
import me.syari.sec_story.game.mobArena.wave.boss.MobArenaBossEquip
import me.syari.sec_story.game.mobArena.wave.boss.MobArenaCustomBoss
import me.syari.sec_story.game.mobArena.wave.boss.MobArenaMythicMobsBoss
import me.syari.sec_story.game.mobArena.wave.boss.skill.*
import me.syari.sec_story.game.mobArena.wave.mob.MobArenaMob
import me.syari.sec_story.game.mobArena.wave.mob.MobArenaMythicMobsMob
import me.syari.sec_story.game.mobArena.wave.mob.MobArenaVanillaMob
import me.syari.sec_story.lib.CustomItemStack
import me.syari.sec_story.lib.Region
import me.syari.sec_story.lib.config.CreateConfig.getConfigDir
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType

object MobArenaConfig {
    fun CommandSender.loadMobArena(){
        val arenas = mutableListOf<MobArenaData>()
        getConfigDir("MobArena").forEach { (yml, config) ->
            val id = yml.substringBefore(".yml")
            config.with {
                output = this@loadMobArena

                val name = getString("name", id)
                val lobbySpawn = getLocation("lobby.spawn")
                val lobby = Region.fromNullable(getLocation("lobby.pos1"), getLocation("lobby.pos2"))
                val playerSpawn = getLocation("play.spawn")
                val play = Region.fromNullable(getLocation("play.pos1"), getLocation("play.pos2"))
                val specSpawn = getLocation("spec.spawn")
                val spec = Region.fromNullable(getLocation("spec.pos1"), getLocation("spec.pos2"))
                val mobSpawn = getLocationList("spawn", listOf())
                val kits = getStringList("kit", listOf())
                val playerLimit = getInt("limit.player", 5)
                val kitLimit = getInt("limit.kit", 1)
                val waveInterval = getLong("wave-interval", 200, false)
                var enable = getBoolean("enable", true, sendNotFound = false)
                if(lobby == null){
                    send("&cLobby Region is null")
                    if(enable) enable = false
                }
                if(play == null || playerSpawn == null){
                    send("&cPlay Region is null")
                    if(enable) enable = false
                }
                if(spec == null){
                    send("&cSpec Region is null")
                    if(enable) enable = false
                }
                if(mobSpawn.isEmpty()){
                    send("&cSpawn List is Empty")
                    if(enable) enable = false
                }
                val arena = MobArenaData(
                    id,
                    name,
                    kits,
                    play,
                    playerSpawn,
                    lobby,
                    lobbySpawn,
                    spec,
                    specSpawn,
                    mobSpawn,
                    waveInterval,
                    playerLimit,
                    kitLimit,
                    enable,
                    this
                )
                val waveNums = mutableListOf<Int>()
                getSection("wave")?.forEach { rawNum ->
                    val num = rawNum.toIntOrNull()
                    if(num != null){
                        waveNums.add(num)
                    } else {
                        send("&cWave $rawNum is not Int")
                    }
                }
                if(waveNums.isNotEmpty()){
                    waveNums.sort()
                    val waveList = mutableListOf<MobArenaWave>()
                    if(waveNums.first() == 1){
                        var preNum = 1
                        val all = MobArenaWaveAll(0, 0)
                        var health = 1.0
                        var spawnOverride: Location? = null
                        var stop = false
                        var mobs = listOf<MobArenaMob>()
                        var boss: MobArenaBoss? = null
                        var upgrade = listOf<CustomItemStack>()
                        var reward = ConfigContents()
                        waveNums.forEach { num ->
                            if(num != 1) waveList.add(
                                MobArenaWave(
                                    arena,
                                    preNum until num,
                                    health,
                                    all,
                                    spawnOverride,
                                    stop,
                                    mobs,
                                    boss,
                                    upgrade,
                                    reward
                                )
                            )
                            health = getDouble("wave.$num.health", false) ?: health
                            all.base = getInt("wave.$num.all.base", false) ?: all.base
                            all.per = getInt("wave.$num.all.per", false) ?: all.per
                            spawnOverride = getLocation("wave.$num.spawn", false)
                            stop = getBoolean("wave.$num.stop", false, sendNotFound = false)
                            mobs = getSection("wave.$num.mob", false)?.map {
                                val type = EntityType.fromName(it)
                                val priority = getInt("wave.$num.mob.$it", 1)
                                if(type != null){
                                    MobArenaVanillaMob(type, priority)
                                } else {
                                    MobArenaMythicMobsMob(it, priority)
                                }
                            } ?: mobs
                            boss = getString("wave.$num.boss.id", false)?.let {
                                val type = EntityType.fromName(it)
                                if(type != null){
                                    val display = getString("wave.$num.boss.display", type.name)
                                    val equip = MobArenaBossEquip(
                                        getCustomItemStackFromString("wave.$num.boss.equip.m", false),
                                        getCustomItemStackFromString("wave.$num.boss.equip.s", false),
                                        getCustomItemStackFromString("wave.$num.boss.equip.h", false),
                                        getCustomItemStackFromString("wave.$num.boss.equip.c", false),
                                        getCustomItemStackFromString("wave.$num.boss.equip.l", false),
                                        getCustomItemStackFromString("wave.$num.boss.equip.b", false)
                                    )
                                    val bossHealth = getDouble("wave.$num.boss.health", 50.0)
                                    val damage = getDouble("wave.$num.boss.damage", 1.0)
                                    val skillInterval = getLong("wave.$num.boss.skill-interval", 100)
                                    val skills = mutableListOf<MobArenaBossSkillBase>()
                                    getStringList("wave.$num.boss.skill")?.forEach { rawSkill ->
                                        val splitSkill = rawSkill.split("\\s+".toRegex())
                                        when(splitSkill[0].toLowerCase()){
                                            "teleport" -> skills.add(MobArenaBossSkillTeleport())
                                            "shuffle" -> skills.add(MobArenaBossSkillShuffle())
                                            "gather" -> skills.add(MobArenaBossSkillGather())
                                            "arrow" -> skills.add(MobArenaBossSkillArrow())
                                            "fireball" -> skills.add(MobArenaBossSkillFireBall())
                                            "push" -> skills.add(MobArenaBossSkillPush(splitSkill.getOrNull(1)?.toDoubleOrNull() ?: 5.0))
                                            "fire" -> skills.add(MobArenaBossSkillFire(splitSkill.getOrNull(1)?.toIntOrNull() ?: 20, splitSkill.getOrNull(2)?.toDoubleOrNull() ?: 5.0))
                                            "freeze" -> skills.add(MobArenaBossSkillFreeze(splitSkill.getOrNull(1)?.toIntOrNull() ?: 20, splitSkill.getOrNull(2)?.toDoubleOrNull() ?: 5.0))
                                        }
                                    }
                                    MobArenaCustomBoss(type, display, equip, bossHealth, damage, skillInterval, skills)
                                } else {
                                    MobArenaMythicMobsBoss(it)
                                }
                            }
                            upgrade = getCustomItemStackListFromStringList("wave.$num.boss.upgrade", listOf(), false)
                            reward = getConfigContentsFromList("MobArena", "wave.$num.boss.reward", false)
                            preNum = num
                        }
                        waveList.add(
                            MobArenaWave(
                                arena,
                                preNum..preNum,
                                health,
                                all,
                                spawnOverride,
                                true,
                                mobs,
                                boss,
                                upgrade,
                                reward
                            )
                        )
                        arena.waveList = waveList
                        arena.lastWave = preNum
                    } else {
                        send("&cWave 1 is not defined")
                    }
                } else {
                    send("&cWave is Empty")
                }
                arenas.add(arena)
            }
        }
        setArenas(arenas)
    }
}