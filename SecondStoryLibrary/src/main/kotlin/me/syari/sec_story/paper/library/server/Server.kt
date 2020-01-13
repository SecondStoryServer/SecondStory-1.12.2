package me.syari.sec_story.paper.library.server

import me.syari.sec_story.paper.library.Main.Companion.plugin
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.util.*

object Server {
    fun getPlayer(uuid: UUID): Player? {
        return plugin.server.getPlayer(uuid)
    }

    fun getPlayer(name: String): Player? {
        return plugin.server.getPlayer(name)
    }

    fun getOfflinePlayer(uuid: UUID): OfflinePlayer? {
        return plugin.server.getOfflinePlayer(uuid)
    }

    fun getOfflinePlayer(name: String): OfflinePlayer? {
        return plugin.server.getOfflinePlayer(name)
    }

    fun getEntity(uuid: UUID): Entity? {
        return plugin.server.getEntity(uuid)
    }

    fun getWorldSafe(name: String): World? {
        return plugin.server.getWorld(name)
    }

    fun getWorld(name: String): World {
        return plugin.server.getWorld(name)
    }

    fun toUUIDSafe(raw: String): UUID? {
        return if(raw.split("-").size == 5) UUID.fromString(raw) else null
    }

    fun toUUID(raw: String): UUID {
        return UUID.fromString(raw)
    }

    val maxPlayers get() = plugin.server.maxPlayers
}