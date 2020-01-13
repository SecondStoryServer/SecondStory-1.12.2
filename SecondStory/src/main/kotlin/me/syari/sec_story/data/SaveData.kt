package me.syari.sec_story.data

import me.syari.sec_story.data.event.load.ExpLoadEvent
import me.syari.sec_story.data.event.load.InventoryLoadEvent
import me.syari.sec_story.data.event.load.LocationLoadEvent
import me.syari.sec_story.data.event.save.ExpSaveEvent
import me.syari.sec_story.data.event.save.InventorySaveEvent
import me.syari.sec_story.data.event.save.LocationSaveEvent
import me.syari.sec_story.hook.Magic.getMage
import me.syari.sec_story.lib.CustomLocation
import me.syari.sec_story.lib.message.SendMessage.send
import me.syari.sec_story.lib.command.CreateCommand.createCmd
import me.syari.sec_story.lib.command.CreateCommand.element
import me.syari.sec_story.lib.command.CreateCommand.onlinePlayers
import me.syari.sec_story.lib.command.CreateCommand.tab
import me.syari.sec_story.lib.config.CreateConfig.containsFile
import me.syari.sec_story.lib.config.CreateConfig.getConfigDir
import me.syari.sec_story.lib.config.CreateConfig.getConfigFile
import me.syari.sec_story.plugin.Init
import me.syari.sec_story.plugin.Plugin.plugin
import me.syari.sec_story.trade.TradeInviteEvent
import me.syari.sec_story.trade.TradeStartEvent
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

object SaveData : Listener, Init(){
    override fun  init(){
        createCmd("data",
            tab { element("save", "load") },
            tab("load"){ onlinePlayers() },
            tab("save"){ element("inv", "loc", "exp") },
            tab("save inv", "save loc", "save exp"){ onlinePlayers() }
        ){ sender, args ->
            when(args.whenIndex(0)){
                "save" -> {
                    when(args.whenIndex(1)){
                        "inv" -> {
                            val p = args.getOrNull(2)?.let { plugin.server.getPlayer(it) } ?: return@createCmd sender.send("&b[SaveInv] &cプレイヤーを入力してください")
                            p.saveInventory()
                        }
                        "loc" -> {
                            val p = args.getOrNull(2)?.let { plugin.server.getPlayer(it) } ?: return@createCmd sender.send("&b[SaveInv] &cプレイヤーを入力してください")
                            p.saveLocation()
                        }
                        "exp" -> {
                            val p = args.getOrNull(2)?.let { plugin.server.getPlayer(it) } ?: return@createCmd sender.send("&b[SaveInv] &cプレイヤーを入力してください")
                            p.saveExp()
                        }
                        else -> sender.send("""
                            &b[Data] &fコマンド
                            &7- &a/data save <Player> inv &7インベントリを保存します
                            &7- &a/data save <Player> loc &7現在座標を保存します
                            &7- &a/data save <Player> exp &7経験値を保存します
                        """.trimIndent())
                    }
                }
                "load" -> {
                    val p = args.getOrNull(1)?.let { plugin.server.getPlayer(it) } ?: return@createCmd sender.send("&b[SaveInv] &cプレイヤーを入力してください")
                    p.loadSave()
                }
                else -> sender.send("""
                    &b[Data] &fコマンド
                    &7- &a/data save <Player> &7プレイヤーデータを保存します
                    &7- &a/data load <Player> &7プレイヤーデータを読み込みます
                """.trimIndent())
            }
        }
    }

    private val loading = mutableListOf<UUID>()

    private fun getSaveDataDir() = getConfigDir("SaveData")

    private fun Player.getDataConfig() = getSaveDataDir()["$uniqueId.yml"]

    private fun Player.getDataConfigOrCreate() = getDataConfig() ?: getConfigFile("SaveData/$uniqueId.yml")

    fun Player.loadSave(){
        if(!isOnline) return
        if(isDead){
            waitLoad = true
            return
        }
        val cfg = getDataConfig() ?: return
        loading.add(uniqueId)
        cfg.getSection("")?.forEach { i ->
            when(i){
                "loc" -> {
                    val loc = cfg.getLocation(i)
                    if(loc != null){
                        loc.yaw = location.yaw
                        loc.pitch = location.pitch
                        teleport(loc)
                        LocationLoadEvent(this).callEvent()
                    }
                }
                "exp" -> {
                    totalExperience = cfg.getInt(i, 0)
                    ExpLoadEvent(this).callEvent()
                }
                "inv" -> {
                    inventory.clear()
                    cfg.getSection(i)?.forEach { f ->
                        val index = f.toIntOrNull()
                        val item = cfg.getItemStack("$i.$f")
                        if(index != null && item != null){
                            inventory.setItem(index, item)
                        }
                    }
                    InventoryLoadEvent(this).callEvent()
                }
            }
        }
        cfg.delete()
        loading.remove(uniqueId)
        waitLoad = false
    }


    fun Player.saveInventory(){
        val cfg = getDataConfigOrCreate()
        if(cfg.contains("inv")) return
        InventorySaveEvent(this).callEvent()
        inventory.contents.forEachIndexed { i, f ->
            if(f != null) cfg.set("inv.$i", f, false)
        }
        cfg.set("inv.clear", ItemStack(Material.AIR), false)
        cfg.save()
        inventory.contents = arrayOf()
    }

    fun Player.saveLocation(){
        val cfg = getDataConfigOrCreate()
        if(cfg.contains("loc")) return
        LocationSaveEvent(this).callEvent()
        val loc = CustomLocation(location)
        cfg.set("loc", loc.toStringWithYawPitch)
    }

    fun Player.saveExp(){
        val cfg = getDataConfigOrCreate()
        if(cfg.contains("exp")) return
        ExpSaveEvent(this).callEvent()
        cfg.set("exp", totalExperience, false)
        totalExperience = 0
        cfg.save()
    }

    val OfflinePlayer.hasSave get() = !loading.contains(uniqueId) && containsFile("SaveData/$uniqueId.yml")

    private val wait = mutableListOf<UUID>()

    private var Player.waitLoad: Boolean
        get() = wait.contains(this.uniqueId)
        set(value) {
            if(value) wait.add(uniqueId)
            else wait.remove(uniqueId)
        }

    @EventHandler
    fun on(e: PlayerRespawnEvent){
        object : BukkitRunnable(){
            override fun run() {
                if(e.player.waitLoad) e.player.loadSave()
            }
        }.runTaskLater(plugin, 3)
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: TradeStartEvent){
        if(e.player.hasSave || e.partner.hasSave){
            e.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: TradeInviteEvent){
        if(e.player.hasSave || e.partner.hasSave){
            e.isCancelled = true
        }
    }

    @EventHandler
    fun on(e: InventorySaveEvent){
        val p = e.player
        val m = getMage(p) ?: return
        val w = m.activeWand ?: return
        w.deactivate()
    }
}