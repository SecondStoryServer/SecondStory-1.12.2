package me.syari.sec_story.paper.core.guild.quest.content

import me.syari.sec_story.paper.library.item.CustomItemStack

class QuestItem(val item: CustomItemStack, override val need: Int): QuestContent {
    override fun toString(): String {
        return "&a${item.display ?: item.type.name}&fを&a${need}個&f納品する"
    }
}