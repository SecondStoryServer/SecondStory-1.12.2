package me.syari.sec_story.paper.core.guild.quest

import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent
import me.syari.sec_story.paper.core.Main.Companion.plugin
import me.syari.sec_story.paper.core.guild.Guild.getGuild
import me.syari.sec_story.paper.core.guild.Guild.guild
import me.syari.sec_story.paper.core.guild.Guild.guilds
import me.syari.sec_story.paper.core.guild.GuildData
import me.syari.sec_story.paper.core.guild.quest.content.QuestBreak
import me.syari.sec_story.paper.core.guild.quest.content.QuestItem
import me.syari.sec_story.paper.core.guild.quest.content.QuestMob
import me.syari.sec_story.paper.core.hook.CoreProtect.isNatural
import me.syari.sec_story.paper.core.item.GiveItem.give
import me.syari.sec_story.paper.library.code.StringEditor.toColor
import me.syari.sec_story.paper.library.config.CreateConfig.configDir
import me.syari.sec_story.paper.library.config.CreateConfig.getConfigDir
import me.syari.sec_story.paper.library.config.CreateConfig.getConfigFile
import me.syari.sec_story.paper.library.config.content.ConfigItemStack.Companion.getItem
import me.syari.sec_story.paper.library.init.EventInit
import me.syari.sec_story.paper.library.inv.CreateInventory.inventory
import me.syari.sec_story.paper.library.inv.CreateInventory.reopen
import me.syari.sec_story.paper.library.inv.CreateInventory.reopenStartsWith
import me.syari.sec_story.paper.library.inv.CustomInventory
import me.syari.sec_story.paper.library.item.CustomItemStack
import me.syari.sec_story.paper.library.scheduler.CustomScheduler.runLater
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import java.util.*

object GuildQuest: EventInit {

    /* GuildQuest Config Dir
    quest-id:
        name: QuestName
        type: [weekly, daily]
        req: item mm ItemID 10
        // req: mob mobID 10
        reward: 100


    weekly:
        quest-id: 0
    daily:
        quest-id: 0
    */

    fun CommandSender.loadGuildQuestConfig() {
        val newQuests = mutableListOf<QuestData>()
        configDir(plugin, "Guild/Quest/Quests", false) {
            output = this@loadGuildQuestConfig

            getSection("")?.forEach { id ->
                val name = getString("$id.name", id)
                val type = when(getString("$id.type")?.toLowerCase()) {
                    "weekly" -> QuestType.Weekly
                    "daily" -> QuestType.Daily
                    else -> {
                        send("&cQuestType is not found")
                        return@forEach
                    }
                }
                val rawReq = getString("$id.req")?.split("\\s+".toRegex()) ?: return@forEach send(
                    "&cQuestReq is not found"
                )
                val req = when(rawReq.getOrNull(0)) {
                    "item" -> {
                        if(rawReq.size == 4) {
                            val item = getItem(rawReq[1], rawReq[2], rawReq[3]) ?: return@forEach send(
                                "&cItem must not be null"
                            )
                            val amount = item.amount
                            QuestItem(item, amount)
                        } else {
                            return@forEach send("&cQuestReq item format error")
                        }
                    }
                    "mob" -> {
                        if(rawReq.size == 3) {
                            val need = rawReq[2].toIntOrNull() ?: 1
                            QuestMob(rawReq[1], need)
                        } else {
                            return@forEach send("&cQuestReq mob format error")
                        }
                    }
                    "break" -> {
                        when(rawReq.size) {
                            3 -> {
                                val mat = Material.getMaterial(rawReq[1]) ?: return@forEach send(
                                    "&cQuestReq break Material ${rawReq[1]} is not found"
                                )
                                val need = rawReq[2].toIntOrNull() ?: 1
                                val dis = type.name
                                QuestBreak(dis, mat, need)
                            }
                            4 -> {
                                val mat = Material.getMaterial(rawReq[1]) ?: return@forEach send(
                                    "&cQuestReq break Material ${rawReq[1]} is not found"
                                )
                                val need = rawReq[2].toIntOrNull() ?: 1
                                val dis = rawReq[3].toColor
                                QuestBreak(dis, mat, need)
                            }
                            else -> return@forEach send("&cQuestReq break format error")
                        }
                    }
                    else -> {
                        send("&cQuestReq format error")
                        return@forEach
                    }
                }
                val money = getInt("$id.money", 0)
                val guildPoint = getInt("$id.point", 0)
                newQuests.add(QuestData(type, id, name, req, money, guildPoint))
            }
        }
        quests = newQuests
    }

    private var quests = listOf<QuestData>()

    private fun getQuest(id: String) = quests.firstOrNull { q -> q.id == id }

    private fun getGuildQuestConfigDir() = getConfigDir(plugin, "Guild/Quest/Data")

    private fun GuildData.getGuildQuestConfig() = getGuildQuestConfigDir()["${id}.yml"]

    private fun GuildData.getGuildQuestConfigOrCreate() = getGuildQuestConfig() ?: getConfigFile(
        plugin, "Guild/Quest/Data/${id}.yml"
    )

    fun GuildData.getWeeklyQuest(): List<QuestGuildData> {
        val cfg = getGuildQuestConfigOrCreate()
        val ret = mutableListOf<QuestGuildData>()
        val loaded = mutableListOf<QuestData>()
        cfg.getSection("weekly")?.forEach { id ->
            val q = getQuest(id)
            if(q != null) {
                val p = cfg.getInt("weekly.$id", 0)
                loaded.add(q)
                ret.add(QuestGuildData(q, p))
            }
        }
        val max = altarData.maxWeekly
        if(ret.size < max) {
            val av = quests.filter { f -> f.type == QuestType.Weekly && f !in loaded }.toMutableList()
            while(av.isNotEmpty() && ret.size < max) {
                val q = av.random()
                av.remove(q)
                val qgd = QuestGuildData(q, 0)
                ret.add(qgd)
                saveQuestProgress(qgd, true)
            }
        }
        return ret
    }

    fun GuildData.getDailyQuest(): List<QuestGuildData> {
        val cfg = getGuildQuestConfigOrCreate()
        val ret = mutableListOf<QuestGuildData>()
        val loaded = mutableListOf<QuestData>()
        cfg.getSection("daily")?.forEach { id ->
            val q = getQuest(id)
            if(q != null) {
                val p = cfg.getInt("daily.$id", 0)
                loaded.add(q)
                ret.add(QuestGuildData(q, p))
            }
        }
        val max = altarData.maxDaily
        if(ret.size < max) {
            val av = quests.filter { f -> f.type == QuestType.Daily && f !in loaded }.toMutableList()
            while(av.isNotEmpty() && ret.size < max) {
                val q = av.random()
                av.remove(q)
                val qgd = QuestGuildData(q, 0)
                ret.add(qgd)
                saveQuestProgress(qgd, true)
            }
        }
        return ret
    }

    private val saveCT = mutableListOf<Pair<UUID, QuestGuildData>>()

    private fun GuildData.saveQuestProgress(q: QuestGuildData, force: Boolean) {
        if(! force && saveCT.contains(id to q)) return
        val cfg = getGuildQuestConfigOrCreate()
        cfg.set("${q.quest.type.name.toLowerCase()}.${q.quest.id}", q.progress)
        saveCT.add(id to q)
        runLater(plugin, 60 * 20) {
            saveCT.remove(id to q)
        }
    }

    private fun Player.endQuest(q: QuestGuildData) {
        val g = guild ?: return
        val money = q.quest.money
        val point = q.quest.point
        g.money += money
        g.point += point
        g.announce(
            "&b[Guild] &a${displayName}&fが&a${q.quest.name}&fをクリアして、&fギルドが&a${String.format(
                "%,d", money
            )}JPY&fと&a${point}ポイント&f獲得しました"
        )
    }

    fun Player.openQuestTop() {
        fun CustomInventory.setItem(index: Int, q: QuestGuildData) {
            if(q.isEnd()) {
                item(index, q.toDisplay())
            } else {
                item(index, q.toDisplay()).event(ClickType.LEFT) {
                    if(q.quest.req is QuestItem) {
                        openQuestDelivery(q)
                    }
                }
            }
        }

        val g = guild ?: return
        inventory("&0&lギルドクエスト") {
            id = "GuildQuest-${g.id}"
            item(0, Material.DIAMOND, "&bウィークリークエスト")
            var index = 2
            g.weeklyQuest.forEach { q ->
                setItem(index, q)
                index ++
            }
            item(9, Material.EMERALD, "&aデイリークエスト")
            index = 11
            g.dailyQuest.forEach { q ->
                setItem(index, q)
                index ++
            }
            item(26, Material.BARRIER, "&c閉じる").event(ClickType.LEFT) {
                closeInventory()
            }
        }.open(this)
    }

    private fun Player.openQuestDelivery(q: QuestGuildData) {
        val questItem = q.quest.req as? QuestItem ?: return
        val reqItem = questItem.item
        val needAmount = questItem.need
        val guild = guild ?: return
        inventory("&0&lギルドクエスト アイテム納品", 4) {
            id = "GuildQuest-${guild.id}-${q.quest.id}"
            cancel = false
            onClick = { e -> onClickQuestDelivery(q, e) }
            onClose = { _ ->
                for(ci in 18..35) {
                    val item = inventory.getItem(ci)
                    if(item != null && item.type != Material.AIR) {
                        give(CustomItemStack(item), "&bギルドクエスト納品返却", 5)
                    }
                }
            }
            item(0, q.toDisplay())
            for(i in 1..3) {
                item(i, Material.STAINED_GLASS_PANE, "", damage = 8)
            }
            item(4, Material.CHEST, "&6納品する").event(ClickType.LEFT) {
                val preProgress = q.progress
                var clear = false
                for(ci in 18..35) {
                    val item = inventory.getItem(ci)
                    if(item != null && item.type != Material.AIR) {
                        inventory.setItem(ci, null)
                        if(! clear && reqItem.isSimilar(item)) {
                            val amount = item.amount
                            if(needAmount <= q.progress + amount) {
                                q.progress = needAmount
                                clear = true
                                val set = amount - needAmount - q.progress
                                item.amount = if(0 < set) set else continue
                            } else {
                                q.progress += amount
                                continue
                            }
                        }
                        give(CustomItemStack(item), "&bギルドクエスト納品返却", 5)
                    }
                }
                if(preProgress != q.progress) {
                    q.showBar(this@openQuestDelivery)
                    guild.saveQuestProgress(q, true)
                    if(clear) {
                        endQuest(q)
                        reopenStartsWith("GuildQuest-${guild.id}") { p ->
                            p.openQuestTop()
                        }
                    } else {
                        reopen("GuildQuest-${guild.id}-${q.quest.id}") { p ->
                            p.openQuestDelivery(q)
                        }
                    }
                }
            }
            for(i in 5..7) {
                item(i, Material.STAINED_GLASS_PANE, "", damage = 8)
            }
            item(8, Material.BARRIER, "&c戻る").event(ClickType.LEFT) {
                openQuestTop()
            }
            for(i in 9..17) {
                item(i, Material.STAINED_GLASS_PANE, "", damage = 15)
            }
        }.open(this)
    }

    private fun onClickQuestDelivery(q: QuestGuildData, e: InventoryClickEvent) {
        if(e.click == ClickType.NUMBER_KEY) {
            e.isCancelled = true
            return
        }
        val questItem = q.quest.req as? QuestItem ?: return
        val reqItem = questItem.item
        val i = e.currentItem ?: return
        if(i.type != Material.AIR && ! reqItem.isSimilar(i)) {
            e.isCancelled = true
        }
    }

    fun resetQuest(type: QuestType) {
        getGuildQuestConfigDir().forEach { (n, c) ->
            c.set(type.name.toLowerCase(), null)
            val id = n.substringBefore(".yml")
            val g = getGuild(UUID.fromString(id)) ?: return@forEach
            when(type) {
                QuestType.Daily -> {
                    g.rawDailyQuest = null
                    g.announce("&b[Guild] &fデイリークエストが更新されました")
                }
                QuestType.Weekly -> {
                    g.rawWeeklyQuest = null
                    g.announce("&b[Guild] &fウィークリークエストが更新されました")
                }
            }
            reopenStartsWith("GuildQuest-${id}") { p ->
                p.openQuestTop()
            }
        }
    }

    @EventHandler
    fun on(e: MythicMobDeathEvent) {
        val p = e.killer as? Player ?: return
        val g = p.guild ?: return
        val name = e.mobType.internalName ?: return
        (g.dailyQuest + g.weeklyQuest).forEach { q ->
            if(! q.isEnd() && q.quest.req is QuestMob) {
                if(q.quest.req.mob == name) {
                    q.progress ++
                    q.showBar(p)
                    val isEnd = q.isEnd()
                    g.saveQuestProgress(q, isEnd)
                    if(isEnd) {
                        p.endQuest(q)
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: BlockBreakEvent) {
        val p = e.player ?: return
        val g = p.guild ?: return
        val block = e.block ?: return
        (g.dailyQuest + g.weeklyQuest).forEach { q ->
            if(! q.isEnd() && q.quest.req is QuestBreak) {

                if(! block.isNatural) return
                if(q.quest.req.block == block.type) {
                    q.progress ++
                    q.showBar(p)
                    val isEnd = q.isEnd()
                    g.saveQuestProgress(q, isEnd)
                    if(isEnd) {
                        p.endQuest(q)
                    }
                }
            }
        }
    }

    fun forceSave() {
        guilds.forEach { g ->
            g.rawDailyQuest?.forEach { q ->
                g.saveQuestProgress(q, true)
            }
            g.rawWeeklyQuest?.forEach { q ->
                g.saveQuestProgress(q, true)
            }
        }
    }
}