package me.syari.sec_story.hook

import com.elmakers.mine.bukkit.api.magic.Mage
import com.elmakers.mine.bukkit.api.magic.MagicAPI
import me.syari.sec_story.lib.CustomItemStack
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object Magic {
    private val hook get() = Bukkit.getPluginManager().getPlugin("Magic") as MagicAPI

    fun getMagicWand(id: String, amount: Int) = CustomItemStack.fromNullable(hook.createWand(id)?.item, amount)

    fun getMagicSpell(id: String, amount: Int) = CustomItemStack.fromNullable(hook.createSpellItem(id), amount)

    fun getMage(p: Player): Mage? = hook.getMage(p)
}