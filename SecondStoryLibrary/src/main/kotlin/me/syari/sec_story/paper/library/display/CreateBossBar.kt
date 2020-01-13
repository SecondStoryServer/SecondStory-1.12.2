package me.syari.sec_story.paper.library.display

import me.syari.sec_story.paper.library.init.EventInit
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

object CreateBossBar: EventInit {
    val bars = mutableListOf<CustomBossBar>()

    @EventHandler
    fun on(e: PlayerJoinEvent) {
        val p = e.player
        bars.forEach { b ->
            b.onLogin(p)
        }
    }

    @EventHandler
    fun on(e: PlayerQuitEvent) {
        val p = e.player
        bars.forEach { b ->
            b.onLogout(p)
        }
    }

    fun onDisable() {
        bars.forEach { b ->
            b.clearPlayer()
        }
    }

    fun createBossBar(
        title: String, color: BarColor, style: BarStyle, public: Boolean = false
    ) = CustomBossBar(title, color, style, public)
}