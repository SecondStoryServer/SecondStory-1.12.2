package me.syari.sec_story.paper.core.game.mobArena.wave.boss

import me.syari.sec_story.paper.library.item.CustomItemStack
import org.bukkit.entity.LivingEntity

data class MobArenaBossEquip(
    val main: CustomItemStack?, val sub: CustomItemStack?, val helmet: CustomItemStack?, val chestPlate: CustomItemStack?, val leggings: CustomItemStack?, val boots: CustomItemStack?
) {
    fun setEquipment(entity: LivingEntity) {
        val equip = entity.equipment
        equip.itemInMainHand = main?.toOneItemStack
        equip.itemInOffHand = sub?.toOneItemStack
        equip.helmet = helmet?.toOneItemStack
        equip.chestplate = chestPlate?.toOneItemStack
        equip.leggings = leggings?.toOneItemStack
        equip.boots = boots?.toOneItemStack
    }
}