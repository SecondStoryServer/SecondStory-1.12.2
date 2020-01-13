package me.syari.sec_story.hook.mythicMobs.mechanic

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity
import io.lumine.xikage.mythicmobs.io.MythicLineConfig
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill
import io.lumine.xikage.mythicmobs.skills.SkillMechanic
import io.lumine.xikage.mythicmobs.skills.SkillMetadata
import org.bukkit.entity.Player

class FoodItemMechanic(skill: String, mlc: MythicLineConfig) : SkillMechanic(skill, mlc), ITargetedEntitySkill {
    private val saturation = mlc.getFloat(arrayOf("saturation", "s"))
    private val foodLevel = mlc.getInteger(arrayOf("amount", "a"))

    override fun castAtEntity(data: SkillMetadata, target: AbstractEntity): Boolean {
        val p = target.bukkitEntity as? Player ?: return false
        p.inventory.itemInMainHand.amount --
        p.foodLevel = foodLevel
        p.saturation = saturation
        return true
    }
}