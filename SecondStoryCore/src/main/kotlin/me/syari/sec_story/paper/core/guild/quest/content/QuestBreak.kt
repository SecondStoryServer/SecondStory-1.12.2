package me.syari.sec_story.paper.core.guild.quest.content

import org.bukkit.Material

class QuestBreak(val name: String, val block: Material, override val need: Int): QuestContent {
    override fun toString(): String {
        return "&a${name}&fを&a${need}ブロック&f破壊する"
    }
}