package me.syari.sec_story.paper.core.guild.quest

import me.syari.sec_story.paper.core.guild.quest.content.QuestContent

data class QuestData(
    val type: QuestType, val id: String, val name: String, val req: QuestContent, val money: Int, val point: Int
)