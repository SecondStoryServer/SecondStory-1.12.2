package me.syari.sec_story.paper.core.rpg

import me.syari.sec_story.paper.library.inv.CreateInventory.inventory

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType

object Quests {
    private val quests = mutableListOf<Quest>()

    fun clear() {
        quests.clear()
    }

    fun add(q: Quest) {
        quests.add(q)
    }

    fun Player.openQuest(npc: String) {
        val qs = quests.filter { f -> f.npc == npc }
        if(qs.isEmpty()) return
        inventory("&9&lクエスト", 1) {
            id = "RPG-Quest-$npc"
            var cnt = 0
            qs.forEach { q ->
                val t = when {
                    q.clearOrder -> Triple(Material.WRITTEN_BOOK, "&a&l解決済み") { q.openOrderPage(this@openQuest, npc) }
                    q.nowOrder -> Triple(
                        Material.BOOK_AND_QUILL,
                        "&e&l進行中 &7- &e${RPG.players.firstOrNull { f -> f.nowQuest == q }?.player?.displayName}"
                    ) { q.openOrderPage(this@openQuest, npc) }
                    else -> Triple(Material.BOOK, "&b&l受注可能") { q.openOrderPage(this@openQuest, npc) }
                }
                item(cnt, t.first, q.name, *q.desc.toTypedArray(), "", t.second).event(ClickType.LEFT, t.third)
                cnt ++
            }
        }.open(this)
    }
}