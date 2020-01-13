package me.syari.sec_story.rank

import com.shampaggon.crackshot.events.WeaponDamageEntityEvent
import com.shampaggon.crackshot.events.WeaponPrepareShootEvent
import com.shampaggon.crackshot.events.WeaponTriggerEvent
import me.syari.sec_story.guild.Guild.guildPlayer
import me.syari.sec_story.hook.CrackShot.getItemFromCrackShot
import me.syari.sec_story.item.Vote.voteCnt
import me.syari.sec_story.lib.CustomItemStack
import me.syari.sec_story.lib.ItemStackPlus.give
import me.syari.sec_story.lib.message.SendMessage.action
import me.syari.sec_story.lib.message.SendMessage.broadcast
import me.syari.sec_story.lib.message.SendMessage.send
import me.syari.sec_story.lib.message.SendMessage.title
import me.syari.sec_story.lib.StringEditor.toColor
import me.syari.sec_story.lib.StringEditor.toUncolor
import me.syari.sec_story.lib.command.CreateCommand.createCmd
import me.syari.sec_story.lib.command.CreateCommand.elementIfOp
import me.syari.sec_story.lib.command.CreateCommand.onlinePlayers
import me.syari.sec_story.lib.command.CreateCommand.tab
import me.syari.sec_story.lib.inv.CreateInventory.inventory
import me.syari.sec_story.lib.inv.CreateInventory.open
import me.syari.sec_story.perm.Permission.loadPerm
import me.syari.sec_story.perm.PermissionLoadEvent
import me.syari.sec_story.player.Time.show
import me.syari.sec_story.player.Time.time
import me.syari.sec_story.plugin.Init
import me.syari.sec_story.plugin.SQL.sql
import me.syari.sec_story.rank.req.*
import me.syari.sec_story.server.CommandBlock
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType

object Ranks : Listener, Init(){
    override fun init() {
        createCmd("rank",
                tab { sender ->
                    elementIfOp(sender, "set")
                },
                tab("set"){ sender ->
                    elementIfOp(sender, onlinePlayers())
                },
                tab("set *") { sender ->
                    elementIfOp(sender, ranks.keys)
                }
        ) { sender, args ->
            when(args.whenIndex(0)){
                "set" -> {
                    if (args[0].toLowerCase() == "set") {
                        val t = (if (2 <= args.size) Bukkit.getOfflinePlayer(args[1]) else null)
                            ?: return@createCmd sender.send("&b[Rank] &cプレイヤーを入力してください")
                        if (args.size < 3) return@createCmd sender.send("&b[Rank] &cランク名を入力してください")
                        val set = get(args[2])
                        sender.send("&b[Rank] &a${t.name}&fのランクを&r${t.rank.name}&fから&r${set.name}&fに変えました")
                        t.rank = set
                    }
                }
                null -> {
                    if (sender is Player) {
                        openRank(sender)
                    }
                }
            }
        }
    }

    var default = ""

    private val ranks = mutableMapOf<String, Rank>()

    fun clear(){
        ranks.clear()
    }

    fun add(name: String, rank: Rank){
        ranks[name] = rank
    }

    fun set(){
        ranks.forEach{ f ->
            f.value.set()
        }
    }

    fun get(name: String?) = ranks[name] ?: ranks.getOrPut(default){ Rank(default) }

    var OfflinePlayer.rank: Rank
        get() {
            var r: String? = ""
            sql {
                val res = executeQuery("SELECT Rank FROM Story.PlayerData WHERE UUID = '$uniqueId';")
                if (res.next()) {
                    r = res.getString("Rank")
                }
            }
            return get(r)
        }
        set(value) {
            sql {
                executeUpdate("UPDATE Story.PlayerData SET Rank = '${value.name}' WHERE UUID = '$uniqueId';")
            }
            if(this is Player) loadPerm()
        }

    val rankCmd: Set<String>
        get() {
            val ret = mutableSetOf<String>()
            ranks.forEach { (_, u) ->
                u.ncmd.forEach { c ->
                    ret.add(c)
                }
            }
            return ret
        }

    private val Player.rankView get(): CustomItemStack {
        val lore = mutableListOf<String>()
        val gp = guildPlayer
        lore.addAll(rank.desc)
        lore.add("")
        lore.add("&aプレイ時間 &7: &f${time.show}")
        lore.add("&a投票回数 &7: &f${voteCnt}回")
        lore.add("&a戦争勝利回数 &7: &f${gp.win}回")
        return CustomItemStack(Material.ANVIL, "${rank.prefix}&f${displayName}".toColor, *lore.toTypedArray())
    }

    private fun openRank(p: Player){
        inventory("&0&lランクアップ", 1) {
            item(0, p.rankView)
            var count = 2
            p.rank.next.forEach { n ->
                val f = get(n.key)
                val l = mutableListOf<String>()
                var can = true
                l.addAll(f.desc)
                l.add("")
                n.value.forEach { g ->
                    when(g){
                        is MoneyReq -> {
                            l.add("${if(g.check(p)) "&a" else "&c"}所持金 &7: &f${String.format("%,d", g.money)}JPY")
                        }
                        is TimeReq -> {
                            l.add("${if(g.check(p)) "&a" else "&c"}プレイ時間 &7: &f${g.minutes.show}")
                        }
                        is ExpReq -> {
                            l.add("${if(g.check(p)) "&a" else "&c"}経験値レベル &7: &f${g.level}レベル")
                        }
                        is VoteReq -> {
                            l.add("${if(g.check(p)) "&a" else "&c"}投票回数 &7: &f${g.count}回")
                        }
                        is WarReq -> {
                            l.add("${if(g.check(p)) "&a" else "&c"}戦争勝利回数 &7: &f${g.win}回")
                        }
                        is ItemReq -> {
                            l.add("${if(g.check(p)) "&a" else "&c"}${if(g.use) "消費アイテム" else "所持アイテム"} &7: &f${g.display}&f ×${g.amount}")
                        }
                    }
                    if(!g.check(p) && can) can = false
                }
                l.add("")
                l.add((if(can) "&6&lこのランクになる" else "&c&l条件達成していません"))
                item(count, Material.EMERALD, "&6${n.key}", *l.toTypedArray()).event(ClickType.LEFT) { if(can) p.rankUp(f, n.value) }
                count ++
            }
            item(8, Material.INK_SACK, "&6ランクリセット先一覧", "&aクリックで開く", damage = 8).event(ClickType.LEFT){ openRankReset(p) }
        }.open(p)
    }

    private fun openRankReset(p: Player){
        inventory("&0&lランクリセット一覧", 1) {
            item(0, p.rankView)
            var count = 2
            p.rank.reset.forEach { f ->
                val l = mutableListOf<String>()
                l.add("&a返却アイテム")
                if(f.second.isNotEmpty()){
                    f.second.forEach { s ->
                        val name = if(s.hasItemMeta && s.hasDisplay) s.display else s.type.name
                        l.add("&f$name&6 ×${s.amount}")
                    }
                } else {
                    l.add("&f無し")
                }
                l.add("")
                l.add("&6&lこのランクになる")
                item(count, Material.INK_SACK, "&6${f.first}", *l.toTypedArray(), damage = 8)
                        .event(ClickType.LEFT){ p.rankReset(get(f.first), f.second) }
                count ++
            }
            item(8, Material.ARROW, "&c戻る").event(ClickType.LEFT){ openRank(p) }
        }.open(p)
    }

    private fun Player.rankUp(rank: Rank, req: Set<RankReq>){
        req.forEach { f ->
            if(!f.check(this)) return
            if(f is ItemReq && f.use){
                f.remove(this)
                Enchantment.ARROW_DAMAGE
            }
        }
        this.rank = rank
        closeInventory()
        title("&6&lランクアップ！！".toColor, "&a${rank.name}になりました".toColor, 5, 40, 5)
        broadcast("&7 >> &d&lRankUp &f&l$displayName &d&l${rank.name}")
    }

    private fun Player.rankReset(rank: Rank, ret: List<CustomItemStack>){
        give(ret, postName = "&eランクリセットアイテム")
        this.rank = rank
        closeInventory()
        title("&6&lランクリセット！！".toColor, "&a${rank.name}になりました".toColor, 5, 40, 5)
        broadcast("&7 >> &d&lRankReset &f&l$displayName &d&l${rank.name}")
    }

    private val CustomItemStack.reqPerm get(): String? {
        if(hasItemMeta && hasLore){
            lore.forEach { s ->
                val req = s.reqPerm
                if(req != null) return req
            }
        }
        return null
    }

    private val String.reqPerm get(): String? {
        val t = toUncolor.split(Regex("\\s+"))
        if(t.size == 2 && t[0].toLowerCase() == "必要等級:"){
            return t[1]
        }
        return null
    }

    private fun Player?.checkWeapon(title: String): Boolean {
        val p = this ?: return true
        val g = getItemFromCrackShot(title, 1) ?: return true
        val r = g.reqPerm ?: return true
        if(p.hasPermission(r)) return true
        p.action("&c必要等級に達していません &7必要等級: $r")
        return false
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: WeaponPrepareShootEvent){
        if(e.player.checkWeapon(e.weaponTitle)) return
        e.isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: WeaponDamageEntityEvent){
        if(e.player.checkWeapon(e.weaponTitle)) return
        e.isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: WeaponTriggerEvent){
        if(e.player.checkWeapon(e.weaponTitle)) return
        e.isCancelled = true
    }

    @EventHandler
    fun on(e: PermissionLoadEvent){
        val p = e.player
        e.addPermission(p.rank.perm)
        e.setAllowCommand(CommandBlock.CommandAddCause.Rank, p.rank.cmd)
    }
}