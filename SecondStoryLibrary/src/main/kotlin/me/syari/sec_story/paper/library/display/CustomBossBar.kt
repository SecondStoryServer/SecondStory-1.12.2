package me.syari.sec_story.paper.library.display

import me.syari.sec_story.paper.library.Main
import me.syari.sec_story.paper.library.Main.Companion.plugin
import me.syari.sec_story.paper.library.code.StringEditor.toColor
import org.bukkit.OfflinePlayer
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player

class CustomBossBar(title: String, color: BarColor, style: BarStyle, val public: Boolean) {
    private val bar: BossBar = plugin.server.createBossBar(title.toColor, color, style)

    init {
        if(public) {
            plugin.server.onlinePlayers.forEach { p ->
                if(p != null) bar.addPlayer(p)
            }
        }
        CreateBossBar.bars.add(this)
    }

    private val addOnLogin = mutableListOf<OfflinePlayer>()

    fun containPlayer(player: OfflinePlayer): Boolean {
        if(public) return true
        return if(player is Player) bar.players.contains(player) else addOnLogin.contains(player)
    }

    fun addPlayer(player: OfflinePlayer) {
        if(public || containPlayer(player)) return
        if(player is Player) bar.addPlayer(player)
        else addOnLogin.add(player)
    }

    fun addAllPlayer(players: Collection<OfflinePlayer>) {
        players.forEach {
            addPlayer(it)
        }
    }

    fun removePlayer(player: OfflinePlayer) {
        if(public || ! containPlayer(player)) return
        if(player is Player) bar.removePlayer(player)
        else addOnLogin.remove(player)
    }

    fun clearPlayer() {
        bar.removeAll()
    }

    fun setPlayer(players: Collection<OfflinePlayer>) {
        clearPlayer()
        addAllPlayer(players)
    }

    var title
        get() = bar.title
        set(value) {
            bar.setTitle(value.toColor)
        }

    /*
    fun removeAllPlayer(players: Collection<OfflinePlayer>){
        players.forEach { player ->
            removePlayer(player)
        }
    }
    */

    fun onLogin(player: Player) {
        if(public) {
            bar.addPlayer(player)
        } else if(addOnLogin.contains(player)) {
            bar.addPlayer(player)
            addOnLogin.remove(player)
        }
    }

    fun onLogout(player: Player) {
        if(public) return
        bar.removePlayer(player)
        addOnLogin.add(player)
    }

    var progress
        get() = bar.progress
        set(value) {
            bar.progress = value
        }

    fun delete() {
        CreateBossBar.bars.remove(this)
        clearPlayer()
    }
}