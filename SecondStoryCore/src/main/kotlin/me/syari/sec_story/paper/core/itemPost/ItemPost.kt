package me.syari.sec_story.paper.core.itemPost

import me.syari.sec_story.paper.core.item.GiveItem.give
import me.syari.sec_story.paper.core.plugin.SQL.sql
import me.syari.sec_story.paper.library.command.CreateCommand.createCmd
import me.syari.sec_story.paper.library.command.CreateCommand.elementIfOp
import me.syari.sec_story.paper.library.command.CreateCommand.offlinePlayers
import me.syari.sec_story.paper.library.command.CreateCommand.tab
import me.syari.sec_story.paper.library.config.content.ConfigItemStack.Companion.getItem
import me.syari.sec_story.paper.library.date.Date.today
import me.syari.sec_story.paper.library.init.FunctionInit
import me.syari.sec_story.paper.library.inv.CreateInventory.inventory
import me.syari.sec_story.paper.library.inv.CreateInventory.reopen
import me.syari.sec_story.paper.library.item.CustomItemStack
import me.syari.sec_story.paper.library.message.SendMessage.send
import me.syari.sec_story.paper.library.player.UUIDPlayer
import me.syari.sec_story.paper.library.server.Server.getOfflinePlayer
import me.syari.sec_story.paper.library.server.Server.toUUIDSafe
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import java.sql.Date

object ItemPost: FunctionInit {
    override fun init() {
        createCmd(
            "post",
            tab { sender -> elementIfOp(sender, "create", "send", "open") },
            tab("create", "send") { sender -> elementIfOp(sender, offlinePlayers) },
            tab("open") { sender -> elementIfOp(sender, postPlayers) }) { sender, args ->
            fun help() {
                sender.send(
                    """
                    &b[ItemPost] &fコマンド
                    &7- &a/post create <Player> <Name> <Limit> <Type> <ID> <Amount> &7コマンドからポストを作成します
                    &7- &a/post send <Player> <Name> <Limit> &7アイテムからポストを作成します
                    &7- &a/post open <Player> &7アイテムポストを開きます
                    """.trimIndent()
                )
            }

            when(args.whenIndex(0)) {
                "create", "send" -> {
                    val p = getOfflinePlayer(
                        args.getOrNull(1) ?: return@createCmd sender.send("&b[ItemPost] &cプレイヤーを入力してください")
                    )
                    val name = args.getOrNull(2) ?: return@createCmd sender.send("&b[ItemPost] &cポストの名前を入力してください")
                    val limit = args.getOrNull(3)?.toIntOrNull() ?: return@createCmd sender.send(
                        "&b[ItemPost] &c期限を入力してください"
                    )
                    if(limit < 0) return@createCmd sender.send("&b[ItemPost] &c受け取り期限を0日以上にしてください")
                    val item = when(args.whenIndex(0)) {
                        "create" -> {
                            val type = args.getOrNull(4) ?: return@createCmd sender.send(
                                "&b[ItemPost] &cアイテムタイプを入力してください"
                            )
                            val id = args.getOrNull(5) ?: return@createCmd sender.send(
                                "&b[ItemPost] &cアイテムのIDを入力してください"
                            )
                            val amount = args.getOrNull(6)?.toIntOrNull() ?: 1
                            sender.send("&b[ItemPost] &fアイテムを送りました")
                            getItem(type, id, amount) ?: return@createCmd sender.send("&b[ItemPost] &cアイテムが見つかりませんでした")
                        }
                        "send" -> {
                            if(sender is Player) {
                                val item = CustomItemStack(sender.inventory.itemInMainHand)
                                if(item.isAir) return@createCmd sender.send("&b[ItemPost] &cアイテムを手に持ってください")
                                sender.send("&b[ItemPost] &fアイテムを送りました")
                                item
                            } else return@createCmd sender.send("&b[ItemPost] &cコンソールから実行できないコマンドです")
                        }
                        else -> return@createCmd help()
                    }
                    p?.give(item, postName = name, postPeriod = limit)
                }
                "open" -> {
                    if(sender is Player) {
                        val target = if(sender.isOp) args.getOrNull(1)?.let { getOfflinePlayer(it) } else null
                        sender.openPost(target)
                    }
                }
                else -> {
                    if(sender is Player && ! sender.isOp) {
                        sender.openPost()
                    } else {
                        help()
                    }
                }
            }
        }
    }

    private fun reopen(uuidPlayer: UUIDPlayer) {
        reopen("ItemPost-$uuidPlayer-true") {
            it.openPost(uuidPlayer.offlinePlayer)
        }
        reopen("ItemPost-$uuidPlayer-false") {
            it.openPost()
        }
    }

    private fun Player.openPost(target: OfflinePlayer? = null) {
        val post = (target ?: this).post
        val isSelfPost = target == null
        inventory("&9&lアイテムポスト") {
            id = "ItemPost-${post.uuidPlayer}-${target != null}"

            var index = 0
            if(! post.isLoaded) post.loadPost(true)
            post.element.forEach { (data, element) ->
                if(index < 26) {
                    val name = data.name
                    val lore = mutableListOf<String>()
                    lore.add("&d${data.receive.toString().replace('-', '/')} に届きました")
                    lore.add("")
                    var cnt = 0
                    element.lore.forEach { line ->
                        cnt ++
                        if(cnt < 4) {
                            lore.add("&f${line.key} &a×${line.value}")
                        }
                    }
                    if(4 <= cnt) lore.add("&a       他 ${cnt - 4}アイテム")
                    lore.add("")
                    lore.add("&c${data.limit.toString().replace('-', '/')} までに受け取ってください")
                    if(! isSelfPost) {
                        lore.add("")
                        lore.add("&cシフト右クリックで削除")
                    }
                    item(index, Material.STORAGE_MINECART, "&6$name", *lore.toTypedArray()).event(ClickType.LEFT) {
                        post.givePost(data, this@openPost, isSelfPost)
                        reopen(post.uuidPlayer)
                    }.event(ClickType.SHIFT_RIGHT) {
                        if(! isSelfPost) {
                            post.deletePost(data)
                            reopen(post.uuidPlayer)
                            player.send("&b[Post] &fアイテムを削除しました")
                        }
                    }
                }
                index ++
            }
            item(26, Material.CHEST, "&a${index}件届いています")
        }.open(this)
    }

    private val postList = mutableMapOf<UUIDPlayer, PlayerItemPost>()

    fun createPost(player: OfflinePlayer) = PlayerItemPost(UUIDPlayer(player))

    val OfflinePlayer.post
        get(): PlayerItemPost {
            return postList.getOrPut(UUIDPlayer(this)) {
                createPost(this).loadPost(true)
            }
        }

    fun OfflinePlayer.addPost(name: String, period: Int, item: CustomItemStack) {
        addPost(name, period, listOf(item))
    }

    fun OfflinePlayer.addPost(name: String, period: Int, items: Collection<CustomItemStack>) {
        val limit = today.plusDays(period.toLong())
        val data = ItemPostData(
            name, Date.valueOf(today), Date.valueOf(limit)
        )
        post.addPost(data, items)
    }

    fun checkPostLimit() {
        sql {
            executeUpdate("DELETE FROM Story.ItemPost WHERE LimitDate < '${Date.valueOf(today)}';")
        }
        clearCache()
    }

    fun clearCache() {
        postList.clear()
        sql {
            val res = executeQuery("SELECT DISTINCT UUID FROM ItemPost;")
            while(res.next()) {
                val player = toUUIDSafe(res.getString("UUID"))?.let { getOfflinePlayer(it) } ?: continue
                val post = createPost(player)
                if(player.isOnline) {
                    post.loadPost(true)
                }
            }
        }
    }

    private val postPlayers get() = postList.mapNotNull { it.key.offlinePlayer?.name }
}