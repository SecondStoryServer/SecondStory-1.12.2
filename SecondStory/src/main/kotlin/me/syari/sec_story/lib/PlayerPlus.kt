package me.syari.sec_story.lib

import me.syari.sec_story.server.Server.today
import org.bukkit.OfflinePlayer
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object PlayerPlus {
    val OfflinePlayer.lastPlayedToDay get(): Long {
        val lastPlay = Instant.ofEpochMilli(lastPlayed).atZone(ZoneId.systemDefault()).toLocalDate()
        return ChronoUnit.DAYS.between(lastPlay, today)
    }
}