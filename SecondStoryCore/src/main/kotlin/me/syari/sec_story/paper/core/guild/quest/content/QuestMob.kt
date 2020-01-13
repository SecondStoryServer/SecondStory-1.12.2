package me.syari.sec_story.paper.core.guild.quest.content

import me.syari.sec_story.paper.core.hook.MythicMobs.getMythicMobs

class QuestMob(val mob: String, override val need: Int): QuestContent {
    fun getName(): String {
        return getMythicMobs(mob)?.displayName?.get() ?: "???"
    }

    override fun toString(): String {
        return "&a${getName()}&fを&a${need}体&f討伐する"
    }
}