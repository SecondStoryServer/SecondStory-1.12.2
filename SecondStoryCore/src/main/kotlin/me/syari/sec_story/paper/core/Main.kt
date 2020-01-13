package me.syari.sec_story.paper.core

import me.syari.sec_story.paper.core.config.ConfigContentRegister
import me.syari.sec_story.paper.core.game.mobArena.MobArena
import me.syari.sec_story.paper.core.game.summonArena.SummonArena
import me.syari.sec_story.paper.core.guild.Guild
import me.syari.sec_story.paper.core.init.Init
import me.syari.sec_story.paper.core.perm.Permission.loadPerm
import me.syari.sec_story.paper.core.plugin.Config.loadAllConfig
import me.syari.sec_story.paper.core.plugin.SQL
import me.syari.sec_story.paper.core.server.DoubleJump
import me.syari.sec_story.paper.core.shop.player.PlayerShop
import me.syari.sec_story.paper.library.scheduler.CustomScheduler.runLater
import org.bukkit.GameMode
import org.bukkit.plugin.java.JavaPlugin

class Main: JavaPlugin() {
    companion object {
        lateinit var plugin: JavaPlugin
    }

    override fun onEnable() {
        plugin = this
        ConfigContentRegister.register()
        with(server.consoleSender) {
            loadAllConfig()
        }
        SQL.load()
        Init.register()
        runLater(plugin, 5) {
            server.onlinePlayers.forEach { p ->
                p.loadPerm()
                if(p.gameMode != GameMode.CREATIVE) {
                    p.isFlying = false
                    p.allowFlight = false
                }
            }
        }
        PlayerShop.onEnable()
    }

    override fun onDisable() {
        DoubleJump.onDisable()
        SummonArena.onDisable()
        MobArena.onDisable()
        PlayerShop.onDisable()
        Guild.onDisable()
    }
}