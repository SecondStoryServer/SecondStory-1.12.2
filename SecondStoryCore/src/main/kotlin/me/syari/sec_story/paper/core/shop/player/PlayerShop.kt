package me.syari.sec_story.paper.core.shop.player

import me.syari.sec_story.paper.core.Main.Companion.plugin
import me.syari.sec_story.paper.library.command.CreateCommand.createCmd
import me.syari.sec_story.paper.library.command.CreateCommand.element
import me.syari.sec_story.paper.library.command.CreateCommand.tab
import me.syari.sec_story.paper.library.config.CreateConfig.config
import me.syari.sec_story.paper.library.config.CreateConfig.getConfigDir
import me.syari.sec_story.paper.library.config.CustomConfig
import me.syari.sec_story.paper.library.init.EventInit
import me.syari.sec_story.paper.library.init.FunctionInit
import me.syari.sec_story.paper.library.message.SendMessage.send
import me.syari.sec_story.paper.library.scheduler.CustomScheduler.schedule
import me.syari.sec_story.paper.library.server.Server.getOfflinePlayer
import me.syari.sec_story.paper.library.world.CustomLocation
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import java.util.*

object PlayerShop: FunctionInit, EventInit {
    override fun init() {
        createCmd("pshop", tab { element("add", "spawn", "check", "reload") }) { sender, args ->
            if(sender is Player) {
                when(args.whenIndex(0)) {
                    "add" -> {
                        val id = args.getOrNull(1) ?: return@createCmd sender.send("&b[PlayerShop] &cショップのIDを入力してください")
                        if(getNPC(id) != null) return@createCmd sender.send("&b[PlayerShop] &c既に存在するIDです")
                        addNewNPC(id, sender.location)
                    }
                    "spawn" -> {
                        val player = args.getOrNull(1)
                        if(player != null) {
                            spawnShopNPC(UUID.randomUUID().toString(), player, sender.location)
                        } else {
                            spawnBuyableNPC(UUID.randomUUID().toString(), sender.location)
                        }
                    }
                    "check" -> {
                        checkAllNPC()
                    }
                    "reload" -> {
                        clearNPC()
                        loadNPC()
                    }
                }
            }
        }
    }

    private val npc = mutableListOf<PlayerShopBase>()

    private fun getNPC(id: String) = npc.firstOrNull { f -> f.id == id }

    private val Entity.isNPC get() = npc.firstOrNull { f -> f.entity == this } != null

    private fun spawnShopNPC(id: String, player: String, loc: Location) {
        val n = getOfflinePlayer(player)?.let { PlayerShopData(id, it, loc) } ?: return
        npc.add(n)
    }

    private fun addNewNPC(id: String, loc: Location) {
        config.set("$id.loc", CustomLocation(loc).toString())
        spawnBuyableNPC(id, loc)
    }

    private fun spawnBuyableNPC(id: String, loc: Location) {
        val n = PlayerShopBuyable(id, loc)
        npc.add(n)
    }

    private fun checkAllNPC() {
        npc.forEach { n ->
            n.checkNPC()
        }
    }

    private fun clearNPC() {
        npc.forEach { n ->
            n.remove()
        }
        npc.clear()
    }

    private val timer = schedule(plugin) {
        checkAllNPC()
    }

    private val configDataDir = getConfigDir(plugin, "Shop/Player/Data")

    lateinit var config: CustomConfig

    private fun loadNPC() {
        config = config(plugin, "Shop/Player/npc.yml", false) {
            getSection("")?.forEach { id ->
                val loc = getLocation("$id.loc")
                if(loc != null) {
                    spawnBuyableNPC(id, loc)
                } else {
                    send("&cLocation Error")
                }
            }
        }
    }

    fun onEnable() {
        plugin.server.worlds.forEach { w ->
            w.entities.forEach { e ->
                if(e is Villager && e.scoreboardTags.contains("2nd-PlayerShop")) {
                    e.remove()
                }
            }
        }
        timer.runTimer(60 * 20)
        loadNPC()
    }

    fun onDisable() {
        timer.cancel()
        clearNPC()
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: EntityDamageEvent) {
        val v = e.entity as? Villager ?: return
        if(v.isNPC) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun on(e: PlayerInteractEntityEvent) {
        val v = e.rightClicked as? Villager ?: return
        if(v.isNPC) {
            e.isCancelled = true
        }
    }
}