package me.syari.sec_story.guild.quest.content

import me.syari.sec_story.hook.MythicMobs.getMythicMobs


class QuestMob(val mob: String, need: Int) : QuestContent(need){
    fun getName(): String {
        return getMythicMobs(mob)?.displayName?.get() ?: "???"
    }

    override fun toString(): String {
        return "&a${getName()}&fを&a${need}体&f討伐する"
    }
}