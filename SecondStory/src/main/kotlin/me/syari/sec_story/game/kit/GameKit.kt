package me.syari.sec_story.game.kit

import me.syari.sec_story.data.SaveData.hasSave
import me.syari.sec_story.data.SaveData.loadSave
import me.syari.sec_story.data.SaveData.saveInventory
import me.syari.sec_story.lib.CustomItemStack
import me.syari.sec_story.lib.message.SendMessage.send
import me.syari.sec_story.lib.command.CreateCommand.createCmd
import me.syari.sec_story.lib.command.CreateCommand.element
import me.syari.sec_story.lib.command.CreateCommand.tab
import me.syari.sec_story.lib.config.CreateConfig.configDir
import me.syari.sec_story.plugin.Init
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object GameKit : Init(){
    override fun init() {
        createCmd("kit",
            tab { element("get", "clear") },
            tab("get"){ element(kits.map { it.id }) }
        ){ sender, args ->
            if(sender is Player){
                when(args.whenIndex(0)){
                    "get" -> {
                        val id = args.getOrNull(1) ?: return@createCmd sender.send("&b[Kit] &cキットを入力してください")
                        val kit = getKit(id) ?: return@createCmd sender.send("&b[Kit] &cキットが見つかりませんでした")
                        if(args.whenIndex(2) != "-o") sender.saveInventory()
                        kit.setKit(sender)
                    }
                    "clear" -> {
                        if(sender.hasSave){
                            sender.loadSave()
                        } else {
                            sender.inventory.clear()
                        }
                    }
                    else -> {
                        sender.send("""
                            &b[Kit] &fコマンド一覧
                            &7- &a/kit get <KitID> &7キットを取得します
                            &7- &a/kit get <KitID> -o &7インベントリ保存をしない
                            &7- &a/kit clear &7アイテムを消去します
                        """.trimIndent())
                    }
                }
            }
        }
    }

    fun CommandSender.loadKit(){
        val newKits = mutableListOf<GameKitData>()
        configDir("Kit", false){
            output = this@loadKit

            getSection("")?.forEach { id ->
                val name = getString("$id.name") ?: return@forEach
                val icon = getCustomItemStackFromString("$id.icon", CustomItemStack(Material.IRON_SWORD))
                val kit = GameKitData(id, name, icon)
                getSection("$id.list")?.forEach { rawIndex ->
                    val index = rawIndex.toIntOrNull()
                    if(index != null){
                        getCustomItemStackFromString("$id.list.$rawIndex")?.let { kit.addItem(index, it) }
                    } else {
                        typeMismatchError("$id.list.$rawIndex", "Int")
                    }
                }
                newKits.add(kit)
            }
        }
        kits = newKits
    }

    private var kits = listOf<GameKitData>()

    fun getKitsWithFilter(filter: List<String>) = kits.filter { it.id in filter }

    fun getKit(id: String) = kits.firstOrNull { f -> f.id.toLowerCase() == id.toLowerCase() }
}