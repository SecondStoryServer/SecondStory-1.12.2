package me.syari.sec_story.player

import me.syari.sec_story.lib.CreateBossBar.createBossBar
import me.syari.sec_story.lib.message.SendMessage.action
import me.syari.sec_story.lib.message.SendMessage.send
import me.syari.sec_story.lib.command.CreateCommand.createCmd
import me.syari.sec_story.lib.command.CreateCommand.element
import me.syari.sec_story.lib.command.CreateCommand.tab
import me.syari.sec_story.plugin.Init
import me.syari.sec_story.plugin.Plugin.plugin
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

object Hide : Listener, Init(){


    private val now = mutableListOf<UUID>()

    override fun init() {
        createCmd("hide",
                tab { element("true", "false") }
        ){ sender, args ->
            if(sender is Player){
                val bool = when(args.whenIndex(0)){
                    "true" -> true
                    "false" -> false
                    null -> !now.contains(sender.uniqueId)
                    else -> return@createCmd sender.send("""
                            &b[Hide] &fコマンド
                            &7- &a/hide true
                            &7- &a/hide false
                        """.trimIndent())
                }
                sender.setHide(bool)
            }
        }
    }

    private val hideBar = createBossBar("&a&lHide now", BarColor.GREEN, BarStyle.SOLID)

    private fun Player.setHide(bool: Boolean){
        if(bool){
            plugin.server.onlinePlayers.forEach { f ->
                if(!f.isOp && this@setHide != f) {
                    f.hidePlayer(plugin, this@setHide)
                }
            }
            addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 2147483647, 1, false, false))
            action("&aYou are hiding now")
            now.add(uniqueId)
            hideBar.addPlayer(this)
        } else {
            plugin.server.onlinePlayers.forEach { f ->
                if(!f.isOp && this@setHide != f) {
                    f.showPlayer(plugin, this@setHide)
                }
            }
            removePotionEffect(PotionEffectType.GLOWING)
            action("&7You are no longer hiding")
            now.remove(uniqueId)
            hideBar.removePlayer(this)
        }
    }

    @EventHandler
    fun on(e: PlayerJoinEvent){
        val p = e.player
        if(p.uniqueId in now){
            p.setHide(true)
        }
        if(!p.isOp){
            object : BukkitRunnable(){
                override fun run() {
                    now.forEach { uuid ->
                        val h = plugin.server.getPlayer(uuid)
                        if(h != null){
                            p.hidePlayer(plugin, h)
                        }
                    }
                }
            }.runTaskLater(plugin, 5)
        }
    }
}