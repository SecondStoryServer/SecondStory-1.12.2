package me.syari.sec_story.hook

import com.shampaggon.crackshot.CSUtility
import me.syari.sec_story.lib.CustomItemStack
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

object CrackShot{
    private val hook = CSUtility()

    fun getItemFromCrackShot(id: String, amount: Int): CustomItemStack?{
        val cItem = CustomItemStack.fromNullable(hook.generateWeapon(id), amount) ?: return null
        cItem.unbreakable = true
        cItem.addItemFlag(ItemFlag.HIDE_UNBREAKABLE)
        return cItem
    }

    fun getTitleFromCrackShot(item: ItemStack): String? = hook.getWeaponTitle(item)
}