package me.syari.sec_story.paper.core.plugin

import me.syari.sec_story.paper.core.Main.Companion.plugin
import me.syari.sec_story.paper.core.command.CommandConfig.loadCommandConfig
import me.syari.sec_story.paper.core.config.content.ConfigMoneyEme
import me.syari.sec_story.paper.core.donate.Donate
import me.syari.sec_story.paper.core.donate.Donate.ranks
import me.syari.sec_story.paper.core.game.kit.GameKit.loadKit
import me.syari.sec_story.paper.core.game.mobArena.MobArenaConfig.loadMobArena
import me.syari.sec_story.paper.core.game.summonArena.MobPoint
import me.syari.sec_story.paper.core.game.summonArena.SummonArena
import me.syari.sec_story.paper.core.game.summonArena.SummonArenaData
import me.syari.sec_story.paper.core.game.summonArena.SummonArenaMob
import me.syari.sec_story.paper.core.guild.GuildConfig.loadGuild
import me.syari.sec_story.paper.core.hook.MythicMobs.getItemFromMythicMobs
import me.syari.sec_story.paper.core.hook.MythicMobs.getMythicMobs
import me.syari.sec_story.paper.core.ip.IPBlackList
import me.syari.sec_story.paper.core.item.LoginReward
import me.syari.sec_story.paper.core.itemCode.ItemCodeConfig.loadItemCode
import me.syari.sec_story.paper.core.rank.Rank
import me.syari.sec_story.paper.core.rank.Ranks
import me.syari.sec_story.paper.core.rank.req.*
import me.syari.sec_story.paper.core.rpg.Quest
import me.syari.sec_story.paper.core.rpg.Quests
import me.syari.sec_story.paper.core.rpg.RPG
import me.syari.sec_story.paper.core.server.Repair.RepairItem
import me.syari.sec_story.paper.core.server.Server
import me.syari.sec_story.paper.core.server.Server.textureURL
import me.syari.sec_story.paper.core.shop.ShopConfig.isLoadedSell
import me.syari.sec_story.paper.core.shop.ShopConfig.loadShop
import me.syari.sec_story.paper.core.tour.TourConfig.loadHelp
import me.syari.sec_story.paper.core.vote.Vote
import me.syari.sec_story.paper.core.world.AllowWorld.loadAllow
import me.syari.sec_story.paper.core.world.portal.Portal.loadPortal
import me.syari.sec_story.paper.library.code.StringEditor.toColor
import me.syari.sec_story.paper.library.config.CreateConfig.config
import me.syari.sec_story.paper.library.config.CreateConfig.configDir
import me.syari.sec_story.paper.library.config.content.ConfigContents
import me.syari.sec_story.paper.library.config.content.ConfigExp
import me.syari.sec_story.paper.library.config.content.ConfigItemStack
import me.syari.sec_story.paper.library.config.content.ConfigItemStack.Companion.getItem
import me.syari.sec_story.paper.library.item.CustomItemStack
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.inventory.ItemFlag

object Config {
    fun CommandSender.loadAllConfig() {
        loadConfig()
        loadKit()
        loadPortal()
        loadGuild()
        loadRank()
        loadDonate()
        loadQuest()
        loadItemCode()
        loadHelp()
        loadAllow()
        loadCommandConfig()
        loadShop()
        loadMobArena()
        loadSummon()
    }

    fun CommandSender.loadConfig() {
        config(plugin, "config.yml", false) {
            output = this@loadConfig

            with(SQL) {
                host = getString("SQL.Host")
                port = getInt("SQL.Port")
                db = getString("SQL.Database")
                user = getString("SQL.User")
                pass = getString("SQL.Password")
            }
            Vote.items = getCustomItemStackListFromStringList("VoteItem", listOf())
            LoginReward.daily = getCustomItemStackListFromStringList("LoginReward", listOf())
            LoginReward.first = getCustomItemStackListFromStringList("FirstLoginReward", listOf())
            Server.news = getStringList("News", listOf())
            with(RPG) {
                lobby = getLocation("RPG.Lobby")
                spawn = getLocation("RPG.Spawn")
            }
            RepairItem = getItemFromMythicMobs(getString("Repair", "RepairItem"))
            textureURL = getString("TexturePack")
        }
        config(plugin, "ip.yml", false) {
            output = this@loadConfig

            IPBlackList.IPs = getStringList("BlackList", listOf())
        }
    }

    fun CommandSender.loadSummon() {
        SummonArena.clearMob()
        MobPoint.clearReward()
        config(plugin, "summon.yml", false) {
            output = this@loadSummon

            getSection("mob")?.forEach { id ->
                val e = getMythicMobs(id)
                if(e != null) {
                    config(plugin, "sell.yml") {
                        val list = getStringList("list.${e.internalName}", listOf())
                        e.drops.forEach { f ->
                            val itemID = f.split("\\s+".toRegex())[0]
                            if(! isLoadedSell(itemID)) {
                                val item = getItemFromMythicMobs(itemID)
                                if(item?.display != null) {
                                    list.add("mm $itemID 0")
                                    send("&b[sell.yml] &fAdded New Mob Drop $itemID")
                                }
                            }
                        }
                        set("list.${e.internalName}", list)
                    }
                    val enable = getBoolean("mob.$id.enable", true, sendNotFound = false)
                    val hide = getBoolean("mob.$id.hide", false, sendNotFound = false)
                    val group = if(hide) "Hide" else getString("mob.$id.group", "Hide")
                    val child = getStringList("mob.$id.child", listOf(), sendNotFound = false)
                    val exp = getInt("mob.$id.exp", 0)
                    val summon = getInt("mob.$id.summon", 0, ! hide)
                    val reward = getInt("mob.$id.reward", 0)
                    val icon = getCustomItemStackFromString("mob.$id.icon", CustomItemStack(Material.STONE), ! hide)
                    icon.addItemFlag(ItemFlag.HIDE_ENCHANTS)
                    icon.addItemFlag(ItemFlag.HIDE_POTION_EFFECTS)
                    val author = getString("mob.$id.author", "Admin")
                    val diff = getInt("mob.$id.diff", 0)
                    val mob = SummonArenaMob(
                        id, e.displayName?.get() ?: "", enable, group, child, exp, summon, reward, icon, author, diff
                    )
                    SummonArena.addMob(mob)
                } else {
                    send("&cSummonMob - $id Null")
                }
            }
            val arenas = mutableListOf<SummonArenaData>()
            getSection("arena")?.forEach { id ->
                val name = getString("arena.$id.name", "Arena")
                val arenaGroup = getString("arena.$id.display", "Default")
                val player = getInt("arena.$id.player", 1)
                val group = getStringList("arena.$id.group", listOf())
                val line = getInt("arena.$id.line", 3, false)
                val spawn = getLocation("arena.$id.spawn")
                val button = getLocation("arena.$id.button")
                val frame = getLocation("arena.$id.frame")
                val tpTo = getLocation("arena.$id.to")
                val tpBack = getLocation("arena.$id.back")
                val pos1 = getLocation("arena.$id.pos1")
                val pos2 = getLocation("arena.$id.pos2")
                if(spawn != null && button != null && frame != null && tpTo != null && tpBack != null && pos1 != null && pos2 != null) {
                    val arena = SummonArenaData(
                        id, arenaGroup, player, name, line, group, spawn, button, frame, tpTo, tpBack, pos1, pos2
                    )
                    arenas.add(arena)
                }
            }
            SummonArena.setArenas(arenas)
            var pre = 0
            getSection("reward.rank")?.forEach { rank ->
                val r = rank.toIntOrNull()
                if(r != null) {
                    val items = getCustomItemStackListFromStringList("reward.rank.$rank", listOf())
                    if(items.isNotEmpty()) {
                        MobPoint.addRankReward((pre + 1)..r, items)
                        if(pre != r) pre = r
                    }
                } else {
                    send("&cSummonMob Reward Rank - $rank is not Int")
                }
            }
            getSection("reward.point")?.forEach { point ->
                val p = point.toIntOrNull()
                if(p != null) {
                    val items = getCustomItemStackListFromStringList("reward.point.$point", listOf())
                    if(items.isNotEmpty()) {
                        MobPoint.addPointReward(p, items)
                    }
                } else {
                    send("&cSummonMob Reward Point - $point is not Int")
                }
            }
        }
    }

    fun CommandSender.loadRank() {
        Ranks.clear()
        config(plugin, "rank.yml", false) {
            output = this@loadRank

            Ranks.default = getString("default", "guest")
            getSection("")?.forEach { f ->
                if(f != "default") {
                    val r = Rank(f)
                    r.prefix = getString("$f.prefix", f.toUpperCase())
                    r.summon = getInt("$f.summon", 0)
                    getSection("$f.next", false)?.forEach { g ->
                        val req = mutableSetOf<RankReq>()
                        getStringList("$f.next.$g", listOf()).forEach { s ->
                            val t = s.split(Regex("\\s+"))
                            when(t[0].toLowerCase()) {
                                "time" -> {
                                    if(t.size == 3) {
                                        val time = t[1].toIntOrNull()
                                        if(time != null) {
                                            req.add(
                                                TimeReq(
                                                    when(t[2].toLowerCase()) {
                                                        "m" -> time
                                                        "h" -> time * 60
                                                        "d" -> time * 60 * 24
                                                        else -> {
                                                            send("&eRank $f.next.$g - Time(m, h, d) else warn")
                                                            time
                                                        }
                                                    }
                                                )
                                            )
                                        } else {
                                            send("&cRank $f.next.$g - Time null")
                                        }
                                    }
                                }
                                "money" -> {
                                    if(t.size == 2) {
                                        val money = t[1].toLongOrNull()
                                        if(money != null) {
                                            req.add(MoneyReq(money))
                                        } else {
                                            send("&cRank $f.next.$g - Money null")
                                        }
                                    }
                                }
                                "exp" -> {
                                    if(t.size == 2) {
                                        val lv = t[1].toIntOrNull()
                                        if(lv != null) {
                                            req.add(ExpReq(lv))
                                        } else {
                                            send("&cRank $f.next.$g - Exp Level null")
                                        }
                                    }
                                }
                                "vote" -> {
                                    if(t.size == 2) {
                                        val cnt = t[1].toIntOrNull()
                                        if(cnt != null) {
                                            req.add(VoteReq(cnt))
                                        } else {
                                            send("&cRank $f.next.$g - Vote count null")
                                        }
                                    }
                                }
                                "guildwar" -> {
                                    if(t.size == 2) {
                                        val win = t[1].toIntOrNull()
                                        if(win != null) {
                                            req.add(WarReq(win))
                                        } else {
                                            send("&cRank $f.next.$g - GuildWar win null")
                                        }
                                    }
                                }
                                "item" -> {
                                    if(t.size == 4 || t.size == 5) {
                                        val use = t.size == 5 && t[4].toLowerCase() == "use"
                                        val i = getItem(t[1], t[2])
                                        if(i != null) {
                                            val display = i.display
                                            if(display != null) {
                                                val amount = t[3].toIntOrNull()
                                                if(amount != null) {
                                                    req.add(ItemReq(display, amount, use))
                                                } else {
                                                    send("&cRank $f.next.$g - ${t[3]} amount null")
                                                }
                                            } else {
                                                send("&cRank $f.next.$g - ${t[2]} name null")
                                            }
                                        } else {
                                            send("&cRank $f.next.$g - ${t[2]} item null")
                                        }
                                    }
                                }
                                else -> {
                                    send("&cRank $f.next.$g - $s error")
                                }
                            }
                        }
                        r.next[g] = req.toSet()
                    }
                    r.depend.addAll(getStringList("$f.depend", listOf(), false))
                    r.ncmd.addAll(getStringList("$f.cmd", listOf(), false))
                    r.nperm.addAll(getStringList("$f.perm", listOf(), false))
                    r.desc.addAll(getStringList("$f.desc", listOf()))
                    getSection("$f.reset", false)?.forEach { rr ->
                        r.reset.add(rr to getCustomItemStackListFromStringList("$f.reset.$rr", listOf()))
                    }
                    Ranks.add(f, r)
                }
            }
        }
        Ranks.set()
    }

    private fun CommandSender.loadDonate() {
        ranks.clear()
        config(plugin, "donate.yml", false) {
            output = this@loadDonate

            getSection("")?.forEach { f ->
                val price = f.toIntOrNull()
                if(price != null) {
                    val cmd = getStringList("$f.cmd", listOf(), false)
                    val perm = getStringList("$f.perm", listOf(), false)
                    ranks.add(Donate.DonateRank(price, cmd, perm))
                }
            }
        }
    }

    fun CommandSender.loadQuest() {
        Quests.clear()
        configDir(plugin, "RPG/Quest", false) {
            output = this@loadQuest

            getSection("")?.forEach { name ->
                val npc = getString("$name.npc", "NPCNAME").toColor
                val desc = getStringList("$name.desc", listOf()).toColor
                val req = ConfigContents()
                getCustomItemStackListFromStringList("$name.request", listOf()).forEach { i ->
                    req.addContent(ConfigItemStack(i))
                }
                val rew = ConfigContents()
                getCustomItemStackListFromStringList("$name.reward", listOf()).forEach { i ->
                    rew.addContent(ConfigItemStack(i))
                }
                rew.addContent(ConfigMoneyEme(getInt("$name.money", 0)))
                rew.addContent(ConfigExp(getInt("$name.exp", 0)))
                Quests.add(Quest(name, npc, desc, req, rew))
            }
        }
    }
}