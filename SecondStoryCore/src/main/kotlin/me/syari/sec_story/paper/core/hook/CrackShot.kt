package me.syari.sec_story.paper.core.hook

import com.shampaggon.crackshot.CSUtility
import me.syari.sec_story.paper.library.item.CustomItemStack
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

object CrackShot {
    private val hook = CSUtility()

    fun getItemFromCrackShot(id: String): CustomItemStack? {
        val cItem = CustomItemStack.fromNullable(hook.generateWeapon(id)) ?: return null
        cItem.unbreakable = true
        cItem.addItemFlag(ItemFlag.HIDE_UNBREAKABLE)
        return cItem
    }

    fun getTitleFromCrackShot(item: ItemStack): String? = hook.getWeaponTitle(item)
}