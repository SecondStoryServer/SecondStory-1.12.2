package me.syari.sec_story.game.mobArena.wave.boss

import me.syari.sec_story.game.mobArena.data.MobArenaData
import me.syari.sec_story.game.mobArena.wave.boss.skill.MobArenaBossSkillBase
import me.syari.sec_story.lib.StringEditor.toColor
import me.syari.sec_story.plugin.Plugin.plugin
import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.scheduler.BukkitRunnable

class MobArenaCustomBoss(val type: EntityType, private val display: String, private val equip: MobArenaBossEquip, private val health: Double, private val damage: Double, private val skillInterval: Long, private val skills: List<MobArenaBossSkillBase>): MobArenaBoss {
    override fun spawn(loc: Location, arena: MobArenaData): LivingEntity? {
        val e= loc.world.spawnEntity(loc, type)
        return if(e is LivingEntity){
            e.getAttribute(Attribute.GENERIC_MAX_HEALTH).baseValue = health
            e.health = health
            e.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).baseValue = damage
            e.customName = display.toColor
            e.isCustomNameVisible = true
            equip.setEquipment(e)
            object : BukkitRunnable(){
                override fun run() {
                    if(skills.isNotEmpty() && !e.isDead){
                        val s = skills.random()
                        s.run(e, arena)
                        arena.announce("&b[MobArena] &6${display}&fが&6${s.name}&fを使いました")
                    } else {
                        cancel()
                    }
                }
            }.runTaskTimer(plugin, skillInterval / 2, skillInterval)
            e
        } else {
            e.remove()
            null
        }
    }
}