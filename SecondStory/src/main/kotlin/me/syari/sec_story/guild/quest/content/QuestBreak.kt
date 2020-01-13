package me.syari.sec_story.guild.quest.content

import org.bukkit.Material

class QuestBreak(val name: String, val block: Material, need: Int) : QuestContent(need){
    override fun toString(): String {
        return "&a${name}&fを&a${need}ブロック&f破壊する"
    }
}