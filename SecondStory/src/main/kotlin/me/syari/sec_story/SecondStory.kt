package me.syari.sec_story

import me.syari.sec_story.config.Config.loadAllConfig
import me.syari.sec_story.game.mobArena.MobArena
import me.syari.sec_story.game.summonArena.SummonArena
import me.syari.sec_story.guild.Guild
import me.syari.sec_story.lib.CreateBossBar
import me.syari.sec_story.lib.CustomItemStack
import me.syari.sec_story.lib.inv.CreateInventory.menuEvent
import me.syari.sec_story.perm.Permission.loadPerm
import me.syari.sec_story.plugin.Init
import me.syari.sec_story.plugin.Plugin.plugin
import me.syari.sec_story.plugin.SQL
import me.syari.sec_story.server.Server
import me.syari.sec_story.server.Server.resetDay
import me.syari.sec_story.shop.player.PlayerShop
import org.bukkit.GameMode
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable

class SecondStory : JavaPlugin() {
    override fun onEnable() {
        plugin = this
        with(server.consoleSender){
            loadAllConfig()
            resetDay()
        }
        SQL.load()
        ConfigurationSerialization.registerClass(CustomItemStack::class.java)
        Init.register()
        Server.timer.runTaskTimer(this, 0, 20)
        object: BukkitRunnable(){
            override fun run() {
                server.onlinePlayers.forEach { p ->
                    p.loadPerm()
                    if(p.gameMode != GameMode.CREATIVE){
                        p.isFlying = false
                        p.allowFlight = false
                    }
                }
            }
        }.runTaskLater(this, 5)
        PlayerShop.onEnable()
    }

    override fun onDisable() {
        Server.timer.cancel()
        server.onlinePlayers.forEach { p ->
            if(p.menuEvent.isNotEmpty()) p.closeInventory()
            if(p.gameMode != GameMode.CREATIVE){
                p.isFlying = false
                p.allowFlight = false
            }
        }
        SummonArena.onDisable()
        MobArena.onDisable()
        PlayerShop.onDisable()
        Guild.onDisable()
        CreateBossBar.onDisable()
    }
}