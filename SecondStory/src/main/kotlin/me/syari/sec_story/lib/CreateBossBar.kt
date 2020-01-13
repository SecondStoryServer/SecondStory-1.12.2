package me.syari.sec_story.lib

import me.syari.sec_story.lib.StringEditor.toColor
import me.syari.sec_story.plugin.Init
import me.syari.sec_story.plugin.Plugin.plugin
import me.syari.sec_story.plugin.Plugin.warn
import org.bukkit.OfflinePlayer
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

object CreateBossBar : Listener, Init(){
    private val bars = mutableListOf<CustomBossBar>()

    @EventHandler
    fun on(e: PlayerJoinEvent){
        val p = e.player
        bars.forEach { b ->
            b.onLogin(p)
        }
    }

    @EventHandler
    fun on(e: PlayerQuitEvent){
        val p = e.player
        bars.forEach { b ->
            b.onLogout(p)
        }
    }

    fun onDisable(){
        bars.forEach { b ->
            b.clearPlayer()
        }
    }

    fun createBossBar(title: String, color: BarColor, style: BarStyle, public: Boolean = false) = CustomBossBar(title, color, style, public)

    class CustomBossBar(title: String, color: BarColor, style: BarStyle, val public: Boolean){
        private val bar: BossBar = plugin.server.createBossBar(title.toColor, color, style)

        init {
            if(public){
                plugin.server.onlinePlayers.forEach { p ->
                    if(p != null) bar.addPlayer(p)
                }
            }
            bars.add(this)
        }
        private val addOnLogin = mutableListOf<OfflinePlayer>()

        fun containPlayer(player: OfflinePlayer): Boolean{
            if(public) return true
            return if(player is Player) bar.players.contains(player) else addOnLogin.contains(player)
        }

        fun addPlayer(player: OfflinePlayer){
            if(public) return warn("Title - $title is Public")
            if(player is Player) bar.addPlayer(player)
            else addOnLogin.add(player)
        }

        fun addAllPlayer(players: Collection<OfflinePlayer>){
            players.forEach {
                addPlayer(it)
            }
        }

        fun removePlayer(player: OfflinePlayer){
            if(public) return warn("Title - $title is Public")
            if(player is Player) bar.removePlayer(player)
            else addOnLogin.remove(player)
        }

        fun clearPlayer(){
            bar.removeAll()
        }

        fun setPlayer(players: Collection<OfflinePlayer>){
            clearPlayer()
            addAllPlayer(players)
        }

        var title
            get() = bar.title ?: ""
            set(value){
                bar.title = value.toColor
            }

        /*
        fun removeAllPlayer(players: Collection<OfflinePlayer>){
            players.forEach { player ->
                removePlayer(player)
            }
        }
        */

        fun onLogin(player: Player){
            if(public){
                bar.addPlayer(player)
            } else if(addOnLogin.contains(player)){
                bar.addPlayer(player)
                addOnLogin.remove(player)
            }
        }

        fun onLogout(player: Player){
            if(public) return
            bar.removePlayer(player)
            addOnLogin.add(player)
        }

        var progress
            get() = bar.progress
            set(value) {
                bar.progress = value
            }

        fun delete(){
            bars.remove(this)
            clearPlayer()
        }
    }
}