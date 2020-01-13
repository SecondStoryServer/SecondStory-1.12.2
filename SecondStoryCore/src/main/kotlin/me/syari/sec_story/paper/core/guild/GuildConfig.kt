package me.syari.sec_story.paper.core.guild

import me.syari.sec_story.paper.core.guild.altar.GuildAltar.loadGuildAltarConfig
import me.syari.sec_story.paper.core.guild.area.GuildArea.loadGuildAreaConfig
import me.syari.sec_story.paper.core.guild.quest.GuildQuest.loadGuildQuestConfig
import me.syari.sec_story.paper.core.guild.war.GuildWar.loadGuildWarConfig
import org.bukkit.command.CommandSender

object GuildConfig {
    fun CommandSender.loadGuild() {
        loadGuildQuestConfig()
        loadGuildAltarConfig()
        loadGuildWarConfig()
        loadGuildAreaConfig()
    }
}