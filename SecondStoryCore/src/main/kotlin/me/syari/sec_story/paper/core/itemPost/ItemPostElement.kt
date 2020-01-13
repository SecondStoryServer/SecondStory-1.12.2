package me.syari.sec_story.paper.core.itemPost

import me.syari.sec_story.paper.library.item.CustomItemStack

class ItemPostElement {
    val lore = mutableMapOf<String, Int>()

    val items = mutableListOf<CustomItemStack>()

    fun add(item: CustomItemStack) {
        items.add(item)
        val name = item.display ?: item.type.name
        lore[name] = lore.getOrDefault(name, 0) + item.amount
    }
}