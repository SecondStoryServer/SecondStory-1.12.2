package me.syari.sec_story.paper.library.hook

import me.syari.sec_story.paper.library.item.CustomItemStack
import org.bukkit.Material

object Minecraft {
    fun getItemFromMineCraft(id: String): CustomItemStack? {
        val mat = id.substringBefore(':')
        val meta = id.substringAfter(':').toShortOrNull()
        val material = Material.getMaterial(mat.toUpperCase()) ?: return null
        return CustomItemStack(
            material, null, durability = meta ?: 0
        )
    }
}