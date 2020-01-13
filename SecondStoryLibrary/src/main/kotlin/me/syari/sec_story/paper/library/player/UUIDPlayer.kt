package me.syari.sec_story.paper.library.player

import me.syari.sec_story.paper.library.server.Server.getOfflinePlayer
import me.syari.sec_story.paper.library.server.Server.getPlayer
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.*

data class UUIDPlayer(private val uniqueId: UUID) {
    constructor(player: OfflinePlayer): this(player.uniqueId)

    val player get(): Player? = getPlayer(uniqueId)

    val offlinePlayer get(): OfflinePlayer? = getOfflinePlayer(uniqueId)

    val isOnline get() = offlinePlayer?.isOnline ?: false

    override fun toString() = uniqueId.toString()
}