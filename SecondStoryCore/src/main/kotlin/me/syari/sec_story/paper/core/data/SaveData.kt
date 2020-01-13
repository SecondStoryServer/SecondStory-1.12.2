package me.syari.sec_story.paper.core.data

import me.syari.sec_story.paper.core.Main.Companion.plugin
import me.syari.sec_story.paper.core.data.event.load.ExpLoadEvent
import me.syari.sec_story.paper.core.data.event.load.InventoryLoadEvent
import me.syari.sec_story.paper.core.data.event.load.LocationLoadEvent
import me.syari.sec_story.paper.core.data.event.save.ExpSaveEvent
import me.syari.sec_story.paper.core.data.event.save.InventorySaveEvent
import me.syari.sec_story.paper.core.data.event.save.LocationSaveEvent
import me.syari.sec_story.paper.library.command.CreateCommand.createCmd
import me.syari.sec_story.paper.library.command.CreateCommand.element
import me.syari.sec_story.paper.library.command.CreateCommand.onlinePlayers
import me.syari.sec_story.paper.library.command.CreateCommand.tab
import me.syari.sec_story.paper.library.config.CreateConfig.containsFile
import me.syari.sec_story.paper.library.config.CreateConfig.getConfigDir
import me.syari.sec_story.paper.library.config.CreateConfig.getConfigFile
import me.syari.sec_story.paper.library.init.FunctionInit
import me.syari.sec_story.paper.library.message.SendMessage.send
import me.syari.sec_story.paper.library.server.Server.getPlayer
import me.syari.sec_story.paper.library.world.CustomLocation
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

object SaveData: FunctionInit {
    override fun init() {
        createCmd(
            "data",
            tab { element("save", "load") },
            tab("load") { onlinePlayers },
            tab("save") { element("inv", "loc", "exp") },
            tab("save inv", "save loc", "save exp") { onlinePlayers }) { sender, args ->
            when(args.whenIndex(0)) {
                "save" -> {
                    when(args.whenIndex(1)) {
                        "inv" -> {
                            val p = args.getOrNull(2)?.let { getPlayer(it) } ?: return@createCmd sender.send(
                                "&b[SaveInv] &cプレイヤーを入力してください"
                            )
                            p.saveInventory()
                        }
                        "loc" -> {
                            val p = args.getOrNull(2)?.let { getPlayer(it) } ?: return@createCmd sender.send(
                                "&b[SaveInv] &cプレイヤーを入力してください"
                            )
                            p.saveLocation()
                        }
                        "exp" -> {
                            val p = args.getOrNull(2)?.let { getPlayer(it) } ?: return@createCmd sender.send(
                                "&b[SaveInv] &cプレイヤーを入力してください"
                            )
                            p.saveExp()
                        }
                        else -> sender.send(
                            """
                            &b[Data] &fコマンド
                            &7- &a/data save <Player> inv &7インベントリを保存します
                            &7- &a/data save <Player> loc &7現在座標を保存します
                            &7- &a/data save <Player> exp &7経験値を保存します
                        """.trimIndent()
                        )
                    }
                }
                "load" -> {
                    val p = args.getOrNull(1)?.let { getPlayer(it) } ?: return@createCmd sender.send(
                        "&b[SaveInv] &cプレイヤーを入力してください"
                    )
                    p.loadSave()
                }
                else -> sender.send(
                    """
                    &b[Data] &fコマンド
                    &7- &a/data save <Player> &7プレイヤーデータを保存します
                    &7- &a/data load <Player> &7プレイヤーデータを読み込みます
                """.trimIndent()
                )
            }
        }
    }

    private val loading = mutableListOf<UUID>()

    private fun getSaveDataDir() = getConfigDir(plugin, "SaveData")

    private fun Player.getDataConfig() = getSaveDataDir()["$uniqueId.yml"]

    private fun Player.getDataConfigOrCreate() = getDataConfig() ?: getConfigFile(plugin, "SaveData/$uniqueId.yml")

    fun Player.loadSave() {
        if(! isOnline) return
        if(isDead) {
            waitLoad = true
            return
        }
        val cfg = getDataConfig() ?: return
        loading.add(uniqueId)
        cfg.getSection("")?.forEach { i ->
            when(i) {
                "loc" -> {
                    val loc = cfg.getLocation(i)
                    if(loc != null) {
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
                        if(index != null && item != null) {
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

    fun Player.saveInventory() {
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

    fun Player.saveLocation() {
        val cfg = getDataConfigOrCreate()
        if(cfg.contains("loc")) return
        LocationSaveEvent(this).callEvent()
        val loc = CustomLocation(location)
        cfg.set("loc", loc.toStringWithYawPitch)
    }

    fun Player.saveExp() {
        val cfg = getDataConfigOrCreate()
        if(cfg.contains("exp")) return
        ExpSaveEvent(this).callEvent()
        cfg.set("exp", totalExperience, false)
        totalExperience = 0
        cfg.save()
    }

    val OfflinePlayer.hasSave get() = ! loading.contains(uniqueId) && containsFile(plugin, "SaveData/$uniqueId.yml")

    private val wait = mutableListOf<UUID>()

    var Player.waitLoad: Boolean
        get() = wait.contains(this.uniqueId)
        set(value) {
            if(value) wait.add(uniqueId)
            else wait.remove(uniqueId)
        }
}