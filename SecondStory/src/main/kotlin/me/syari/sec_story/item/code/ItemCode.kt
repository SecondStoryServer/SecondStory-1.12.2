package me.syari.sec_story.item.code

import me.syari.sec_story.config.content.ConfigContents
import me.syari.sec_story.lib.message.SendMessage
import me.syari.sec_story.lib.message.SendMessage.Action
import me.syari.sec_story.lib.message.SendMessage.send
import me.syari.sec_story.lib.command.CreateCommand.createCmd
import me.syari.sec_story.lib.command.CreateCommand.elementIfOp
import me.syari.sec_story.lib.command.CreateCommand.tab
import me.syari.sec_story.lib.inv.CreateInventory.inventory
import me.syari.sec_story.lib.inv.CreateInventory.open
import me.syari.sec_story.plugin.Init
import me.syari.sec_story.plugin.Plugin.plugin
import me.syari.sec_story.server.Server.today
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.scheduler.BukkitRunnable
import java.text.SimpleDateFormat

object ItemCode : Init(){
    override fun init() {
        createCmd("code",
            tab { sender ->
                elementIfOp(sender, "info", "check", "delete")
            },
            tab("info", "check", "delete"){ sender ->
                elementIfOp(sender, codes.map { it.code })
            }
        ){ sender, args ->
            if(sender.isOp){
                when(args.whenIndex(0)){
                    "info" -> {
                        val code = args.getOrNull(1) ?: return@createCmd sender.send("&b[ItemCode] &cアイテムコードを入力してください")
                        sender.sendInfo(code)
                    }
                    "check" -> {
                        if(sender is Player){
                            val code = args.getOrNull(1) ?: return@createCmd sender.send("&b[ItemCode] &cアイテムコードを入力してください")
                            val items = sender.getItemFromCode(code, true) ?: return@createCmd sender.send("&b[ItemCode] &c存在しないアイテムコードです")
                            inventory("&9&lアイテムコード確認"){
                                items.getContents().forEachIndexed { i, c ->
                                    item(i, c.display(sender))
                                }
                                item(26, Material.ARROW, "&c受け取る")
                                    .event(ClickType.LEFT){
                                        sender.closeInventory()
                                        object : BukkitRunnable(){
                                            override fun run() {
                                                items.addContentsToPlayer(sender)
                                            }
                                        }.runTaskLater(plugin, 20)
                                    }
                            }.open(sender)
                        }
                    }
                    "delete" -> {
                        val code = args.getOrNull(1) ?: return@createCmd sender.send("&b[ItemCode] &cアイテムコードを入力してください")
                        val res = deleteCode(code)
                        if(!res) return@createCmd sender.send("&b[ItemCode] &c存在しないアイテムコードです")
                        return@createCmd sender.send("&b[ItemCode] &a$code&fを削除しました")
                    }
                    else -> {
                        sender.send("""
                            &b[ItemCode] &fコマンド
                            &7- &a/code info <Code> &7コードの詳細を確認します
                            &7- &a/code check <Code> &7コードのアイテムを確認します
                            &7- &a/code delete <Code> &7コードを削除します
                        """.trimIndent())
                    }
                }
            } else if(sender is Player){
                val code = args.getOrNull(0) ?: return@createCmd sender.send("&b[ItemCode] &cアイテムコードを入力してください")
                val items = sender.getItemFromCode(code) ?: return@createCmd if(args.getOrNull(1) != "-s") sender.send("&b[ItemCode] &c存在しないアイテムコードです") else Unit
                items.addContentsToPlayer(sender)
                if(args.getOrNull(1) != "-s") sender.send("&b[ItemCode] &a$code&fの報酬を手に入れました")
            }
        }
    }

    fun checkCodeLimit(){
        codes.forEach {
            it.checkLimit(today)
        }
    }

    var codes = mutableSetOf<ItemCodeData>()

    private fun getCode(code: String) = codes.firstOrNull { it.code == code }

    private fun Player.getItemFromCode(code: String, justGet: Boolean = false): ConfigContents?{
        return getCode(code)?.getItemCode(this, justGet)
    }

    private val format = SimpleDateFormat("yyyy/MM/dd")

    private fun CommandSender.sendInfo(code: String){
        val data = getCode(code) ?: return
        val limit = data.limit?.let { format.format(data.limit) } ?: "なし"
        val received = data.received
        send(
            "&b[ItemCode] &fアイテムコード\n" to null,
            "&7コード: &a$code\n" to null,
            "&7期限: &a$limit\n" to null,
            "&7受け取り人数: &a${received.size}\n" to null,
            "&a[確認]" to Action(hover = "&aアイテムを受け取る", click = SendMessage.ClickType.TypeText to "/code check $code"), "  " to null, "&c[削除]" to Action(hover = "&cコードを削除する", click = SendMessage.ClickType.TypeText to "/code delete $code")
        )
    }

    private fun deleteCode(code: String): Boolean{
        val data = getCode(code) ?: return false
        data.delete()
        return true
    }
}