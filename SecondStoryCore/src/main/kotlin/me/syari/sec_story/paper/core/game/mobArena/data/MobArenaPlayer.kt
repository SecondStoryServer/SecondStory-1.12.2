package me.syari.sec_story.paper.core.game.mobArena.data

import me.syari.sec_story.paper.core.game.kit.GameKit.getKit
import me.syari.sec_story.paper.library.config.content.ConfigContents
import org.bukkit.Location
import org.bukkit.entity.Player

class MobArenaPlayer(val arena: MobArenaData, val player: Player, var play: Boolean) {
    var ready = false
        set(value) {
            field = value
            if(value) {
                arena.checkReady(player)
            } else {
                arena.announce("&b[MobArena] &a${player.displayName}&fが準備完了を取り消しました")
            }
        }
    var kit: String? = null

    fun getKit() = kit?.let { getKit(it) }

    val reward = mutableListOf<ConfigContents>()

    fun isAllowMove(loc: Location): Boolean {
        return if(play) {
            if(arena.status == MobArenaStatus.WaitReady) {
                arena.lobby?.inRegion(loc)
            } else {
                arena.play?.inRegion(loc)
            }
        } else {
            arena.spec?.inRegion(loc)
        } ?: true
    }
}