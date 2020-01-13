package me.syari.sec_story.paper.core.hook

import io.lumine.xikage.mythicmobs.MythicMobs
import io.lumine.xikage.mythicmobs.mobs.ActiveMob
import io.lumine.xikage.mythicmobs.mobs.MythicMob
import me.syari.sec_story.paper.library.code.StringEditor.toColor
import me.syari.sec_story.paper.library.item.CustomItemStack
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack

object MythicMobs {
    private val hook = MythicMobs.inst()

    fun getItemFromMythicMobs(id: String) = CustomItemStack.fromNullable(hook.itemManager.getItemStack(id))

    fun getMythicItemFromDisplay(display: String): ItemStack? = io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter.adapt(
        hook.itemManager.items.firstOrNull { i -> i.displayName?.toColor == display.toColor }?.generateItemStack(1)
    )

    fun getMythicMobs(id: String): MythicMob? = hook.mobManager.getMythicMob(id)

    val allMythicMobs: List<LivingEntity>
        get() = hook.mobManager.allMythicEntities

    fun spawnMythicMobs(id: String, loc: Location): ActiveMob? = hook.mobManager.spawnMob(id, loc)

}