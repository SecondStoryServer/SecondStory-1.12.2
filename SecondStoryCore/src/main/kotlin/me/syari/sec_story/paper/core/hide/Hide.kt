package me.syari.sec_story.paper.core.hide

import me.syari.sec_story.paper.core.Main.Companion.plugin
import me.syari.sec_story.paper.library.command.CreateCommand.createCmd
import me.syari.sec_story.paper.library.command.CreateCommand.element
import me.syari.sec_story.paper.library.command.CreateCommand.tab
import me.syari.sec_story.paper.library.display.CreateBossBar.createBossBar
import me.syari.sec_story.paper.library.init.FunctionInit
import me.syari.sec_story.paper.library.message.SendMessage.action
import me.syari.sec_story.paper.library.message.SendMessage.send
import me.syari.sec_story.paper.library.scheduler.CustomScheduler.runLater
import me.syari.sec_story.paper.library.server.Server.getPlayer
import org.bukkit.OfflinePlayer
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

object Hide: FunctionInit {
    private val now = mutableListOf<UUID>()

    val OfflinePlayer.nowHiding get() = now.contains(uniqueId)

    override fun init() {
        createCmd("hide", tab { element("true", "false") }) { sender, args ->
            if(sender is Player) {
                val bool = when(args.whenIndex(0)) {
                    "true" -> true
                    "false" -> false
                    null -> ! now.contains(sender.uniqueId)
                    else -> return@createCmd sender.send(
                        """
                            &b[Hide] &fコマンド
                            &7- &a/hide true
                            &7- &a/hide false
                        """.trimIndent()
                    )
                }
                sender.setHide(bool)
            }
        }
    }

    private val hideBar = createBossBar("&a&lHide now", BarColor.GREEN, BarStyle.SOLID)

    fun Player.setHide(bool: Boolean) {
        if(bool) {
            plugin.server.onlinePlayers.forEach { f ->
                if(! f.isOp && this@setHide != f) {
                    f.hidePlayer(plugin, this@setHide)
                }
            }
            addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 2147483647, 1, false, false))
            action("&aYou are hiding now")
            now.add(uniqueId)
            hideBar.addPlayer(this)
        } else {
            plugin.server.onlinePlayers.forEach { f ->
                if(! f.isOp && this@setHide != f) {
                    f.showPlayer(plugin, this@setHide)
                }
            }
            removePotionEffect(PotionEffectType.GLOWING)
            action("&7You are no longer hiding")
            now.remove(uniqueId)
            hideBar.removePlayer(this)
        }
    }

    fun applyHide(player: Player) {
        runLater(plugin, 5) {
            now.forEach { uuid ->
                val h = getPlayer(uuid)
                if(h != null) {
                    player.hidePlayer(plugin, h)
                }
            }
        }
    }
}