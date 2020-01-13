package me.syari.sec_story.guild.quest.content

import me.syari.sec_story.lib.CustomItemStack

class QuestItem(val item: CustomItemStack, need: Int) : QuestContent(need){
    override fun toString(): String {
        return "&a${item.display ?: item.type.name}&fを&a${need}個&f納品する"
    }
}