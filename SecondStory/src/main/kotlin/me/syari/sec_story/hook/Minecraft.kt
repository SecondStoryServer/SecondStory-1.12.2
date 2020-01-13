package me.syari.sec_story.hook

import me.syari.sec_story.lib.CustomItemStack
import org.bukkit.Material

object Minecraft {
    fun getItemFromMineCraft(id: String, amount: Int): CustomItemStack?{
        val mat = id.substringBefore(':')
        val meta = id.substringAfter(':').toShortOrNull()
        val material = Material.getMaterial(mat.toUpperCase()) ?: return null
        return CustomItemStack(material, null, durability = meta ?: 0, amount = amount)
    }
}