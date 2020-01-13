package me.syari.sec_story.paper.core.hook

import com.elmakers.mine.bukkit.api.magic.Mage
import com.elmakers.mine.bukkit.api.magic.MagicAPI
import me.syari.sec_story.paper.core.Main.Companion.plugin
import me.syari.sec_story.paper.library.item.CustomItemStack
import org.bukkit.entity.Player

object Magic {
    private val hook get() = plugin.server.pluginManager.getPlugin("Magic") as MagicAPI

    fun getMagicWand(id: String) = CustomItemStack.fromNullable(hook.createWand(id)?.item)

    fun getMagicSpell(id: String) = CustomItemStack.fromNullable(hook.createSpellItem(id))

    fun getMage(p: Player): Mage? = hook.getMage(p)
}