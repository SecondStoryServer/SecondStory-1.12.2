package me.syari.sec_story.guild

import me.syari.sec_story.guild.altar.GuildAltar.loadGuildAltarConfig
import me.syari.sec_story.guild.area.GuildArea.loadGuildAreaConfig
import me.syari.sec_story.guild.quest.GuildQuest.loadGuildQuestConfig
import me.syari.sec_story.guild.war.GuildWar.loadGuildWarConfig
import org.bukkit.command.CommandSender

object GuildConfig {
    fun CommandSender.loadGuild(){
        loadGuildQuestConfig()
        loadGuildAltarConfig()
        loadGuildWarConfig()
        loadGuildAreaConfig()
    }
}