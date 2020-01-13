package me.syari.sec_story.item

import me.syari.sec_story.lib.CreateBossBar.createBossBar
import me.syari.sec_story.lib.CustomItemStack
import me.syari.sec_story.lib.ItemStackPlus.give
import me.syari.sec_story.lib.message.SendMessage.send
import me.syari.sec_story.lib.command.CreateCommand.createCmd
import me.syari.sec_story.lib.command.CreateCommand.element
import me.syari.sec_story.lib.command.CreateCommand.tab
import me.syari.sec_story.lib.config.CreateConfig.containsFile
import me.syari.sec_story.lib.config.CreateConfig.getConfigDir
import me.syari.sec_story.lib.config.CreateConfig.getConfigFile
import me.syari.sec_story.lib.config.CustomConfig
import me.syari.sec_story.lib.inv.CreateInventory.inventory
import me.syari.sec_story.lib.inv.CreateInventory.open
import me.syari.sec_story.plugin.Init
import me.syari.sec_story.plugin.Plugin.plugin
import me.syari.sec_story.server.Server.today
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import java.time.LocalDate

object ItemPost : Init(), Listener {
    override fun init() {
        createCmd("post",
            tab { element("create", "edit") }
        ){ sender, args ->
            when(args.whenIndex(0)){
                "create" -> {
                    val p = plugin.server.getOfflinePlayer(args.getOrNull(1) ?: return@createCmd sender.send("&b[ItemPost] &cプレイヤーを入力してください"))
                    val name = args.getOrNull(2) ?: return@createCmd sender.send("&b[ItemPost] &cポストの名前を入力してください")
                    val limit = args.getOrNull(3)?.toIntOrNull() ?: return@createCmd sender.send("&b[ItemPost] &c期限を入力してください")
                    if(limit < 0) return@createCmd sender.send("&b[ItemPost] &c受け取り期限を0日以上にしてください")
                    val type = args.getOrNull(4) ?: return@createCmd sender.send("&b[ItemPost] &cアイテムタイプを入力してください")
                    val id = args.getOrNull(5) ?: return@createCmd sender.send("&b[ItemPost] &cアイテムのIDを入力してください")
                    val amount = args.getOrNull(6)?.toIntOrNull() ?: 1
                    val item = p.postOrCreate.getItem(type, id, amount) ?: return@createCmd sender.send("&b[ItemPost] &cアイテムが見つかりませんでした")
                    if(p is Player){
                        p.give(item)
                    } else {
                        p.addPost(name, limit, item)
                    }
                }
                "edit" -> {
                    sender.send("&b[ItemPost] &eComing Soon")
                }
                "open" -> {
                    if(sender is Player) {
                        sender.openPost()
                    }
                }
                else -> {
                    if(sender is Player && !sender.isOp){
                        sender.openPost()
                    } else {
                        sender.send("""
                            &b[ItemPost] &fコマンド
                            &7- &a/post create <Player> <Name> <Limit> [mm, cs, mc, mg] <ID> <Amount>
                            &7- &a/post open <Player>
                        """.trimIndent())
                    }
                }
            }
        }
    }

    private val itemPostDir by lazy { getConfigDir("ItemPost") }

    private val OfflinePlayer.isEmptyPost get() = !containsFile("ItemPost/$uniqueId.yml")

    private val OfflinePlayer.postOrCreate get() = post ?: getConfigFile("ItemPost/$uniqueId.yml")

    private val OfflinePlayer.post get() = itemPostDir["$uniqueId.yml"]

    private fun OfflinePlayer.setPost(path: String, value: List<CustomItemStack>?){
        val post = post ?: return
        post.set(path, value)
        if(post.hasPostItem){
            post.save()
        } else {
            post.delete()
        }
        if(this is Player) infoPost()
    }

    private val CustomConfig.hasPostItem: Boolean
        get() {
            getSection("")?.forEach { limit ->
                getSection(limit)?.forEach { day ->
                    getSection("$limit.$day")?.forEach { name ->
                        val items = getCustomItemStackList("$limit.$day.$name", listOf(), false)
                        if (items.isNotEmpty()) return true
                    }
                }
            }
            return false
        }

    private val bar = createBossBar("&f&lポストにアイテムが届いています &a&l/post &f&lで受け取りましょう", BarColor.GREEN, BarStyle.SOLID)

    fun Player.infoPost(){
        if(isEmptyPost){
            if(bar.containPlayer(this)){
                bar.removePlayer(this)
            }
        } else {
            if(!bar.containPlayer(this)){
                bar.addPlayer(this)
            }
        }
    }

    private fun Player.openPost(){
        val post = post
        inventory("&9&lアイテムポスト") {
            var index = 0
            post?.getSection("")?.forEach { limit ->
                post.getSection(limit)?.forEach { day ->
                    post.getSection("$limit.$day")?.forEach { name ->
                        if(index < 26){
                            val path = "$limit.$day.$name"
                            val items = post.getCustomItemStackList(path, listOf())
                            val lore = mutableListOf<String>()
                            lore.add("&d${day.replace('-', '/')} に届きました")
                            lore.add("")
                            var cnt = 0
                            items.forEachIndexed { i, f ->
                                if(2 < i) {
                                    cnt ++
                                } else {
                                    lore.add("&f${f.display ?: f.type.name} &a×${f.amount}")
                                }
                            }
                            if(cnt != 0) lore.add("&a       他 ${cnt}アイテム")
                            lore.add("")
                            lore.add("&c${limit.replace('-', '/')} までに受け取ってください")
                            item(index, Material.STORAGE_MINECART, "&6$name", *lore.toTypedArray())
                                .event(ClickType.LEFT){
                                    getPostContent(path, items)
                                    openPost()
                                }
                            index ++
                        }
                    }
                }
            }
            item(26, Material.CHEST, "&a${index}件届いています")
        }.open(this)
    }

    private fun Player.getPostContent(path: String, items: List<CustomItemStack>){
        give(items)
        send("&b[Post] &fアイテムを受け取りました")
        setPost(path, null)
    }

    private fun getDate(t: String): LocalDate? {
        val s = t.split("-")
        if(s.size != 3) return null
        val y = s[0].toIntOrNull() ?: return null
        val m = s[1].toIntOrNull() ?: return null
        val d = s[2].toIntOrNull() ?: return null
        return LocalDate.of(y, m, d)
    }

    fun checkPostLimit(){
        itemPostDir.forEach{ (_, c) ->
            c.getSection("")?.forEach loop@ { limit ->
                val date = getDate(limit) ?: return@loop
                val diff = date.compareTo(today)
                if(diff < 0){
                    c.set(limit, null)
                }
            }
        }
    }

    fun OfflinePlayer.addPost(name: String, period: Int, item: CustomItemStack){
        addPost(name, period, listOf(item))
    }

    private fun List<CustomItemStack>.compress(target: Collection<CustomItemStack>): List<CustomItemStack> {
        val already = mutableMapOf<ItemStack, Int>()
        (this + target).forEach { c ->
            val item = c.item
            val amount = c.amount + already.getOrDefault(item, 0)
            already[item] = amount
        }
        return already.map { (i, amount) -> CustomItemStack(i, amount) }
    }

    fun OfflinePlayer.addPost(name: String, period: Int, items: Collection<CustomItemStack>){
        val limit = today.plusDays(period.toLong())
        val post = postOrCreate
        val path = "${limit.year}-${limit.month.value}-${limit.dayOfMonth}.${today.year}-${today.month.value}-${today.dayOfMonth}.$name"
        val list = post.getCustomItemStackList(path, listOf(), false)
        setPost(path, list.compress(items))
    }
}