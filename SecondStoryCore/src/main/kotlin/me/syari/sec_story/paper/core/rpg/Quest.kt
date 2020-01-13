package me.syari.sec_story.paper.core.rpg

import me.syari.sec_story.paper.core.rpg.Quests.openQuest
import me.syari.sec_story.paper.core.rpg.RPG.announce
import me.syari.sec_story.paper.core.rpg.RPG.data
import me.syari.sec_story.paper.core.rpg.RPG.players
import me.syari.sec_story.paper.library.code.StringEditor.toUncolor
import me.syari.sec_story.paper.library.config.content.ConfigContentItem
import me.syari.sec_story.paper.library.config.content.ConfigContents
import me.syari.sec_story.paper.library.inv.CreateInventory.inventory
import me.syari.sec_story.paper.library.inv.CreateInventory.reopen
import me.syari.sec_story.paper.library.inv.CustomInventory
import me.syari.sec_story.paper.library.message.SendMessage.title
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType

/*
"name": # クエスト名
  npc: "" # 受注可能なNPCの名前
  desc:
  - "クエストの説明"
  request: # 要求するアイテム
  - mm itemID amount
  reward: # 報酬のアイテム
  - mm itemID amount
  money: 20
  exp: 30
 */

data class Quest(
    val name: String, val npc: String, val desc: List<String>, val req: ConfigContents, val rew: ConfigContents
) {
    val nowOrder
        get() = players.firstOrNull { f -> f.nowQuest == this } != null

    val clearOrder
        get() = this in RPG.clearQuests

    fun openOrderPage(p: Player, back: String) {
        inventory("&9&lクエスト確認", 1) {
            id = "RPG-Quest-$npc"
            val t = when {
                clearOrder -> Pair(Material.WRITTEN_BOOK, "&a&l解決済み")
                nowOrder -> Pair(
                    Material.BOOK_AND_QUILL,
                    "&e&l進行中 &7- &e${players.firstOrNull { f -> f.nowQuest == this@Quest }?.player?.displayName}"
                )
                else -> Pair(Material.BOOK, "&b&l受注可能")
            }
            item(0, t.first, name, *desc.toTypedArray(), "", t.second).event(ClickType.LEFT) {
                if(! nowOrder && p.data != null) {
                    p.data?.nowQuest = this@Quest
                    p.closeInventory()
                    p.title("&a&lクエストを受注しました", "&7${name.toUncolor}", 0, 40, 0)
                    announce("&7 >> &a${p.displayName}&fが${name}&fを受注しました")
                    reopen(id) { p -> p.openQuest(npc) }
                }
            }
            item(2, Material.STORAGE_MINECART, "&a要求アイテム", "&6クリックで表示").event(ClickType.LEFT) { openReq(p, back) }
            item(4, Material.CHEST, "&a報酬", "&6クリックで表示").event(ClickType.LEFT) { openRew(p, back) }
            //item(5, Material.EMERALD, "&a報酬金", "&6$money EME")
            //item(6, Material.EXP_BOTTLE, "&a獲得経験値", "&6$exp Exp")
            item(8, Material.ARROW, "&c戻る").event(ClickType.LEFT) { p.openQuest(back) }
        }.open(p)
    }

    private fun openReq(p: Player, back: String) {
        inventory("&9&l要求アイテム確認", 1) {
            id = "RPG-Quest-$npc"
            item(0, Material.STORAGE_MINECART, "&a要求アイテム")
            setItem(req, p, back)
            if(p.data?.nowQuest == this@Quest) item(7, Material.HOPPER, "&a納品する").event(ClickType.LEFT) { delivery(p) }
        }.open(p)
    }

    private fun openRew(p: Player, back: String) {
        inventory("&9&l報酬アイテム確認", 1) {
            id = "RPG-Quest-$npc"
            item(0, Material.CHEST, "&a報酬アイテム")
            setItem(rew, p, back)
        }.open(p)
    }

    private fun CustomInventory.setItem(items: ConfigContents, p: Player, back: String) {
        var cnt = 2
        items.getContents().forEach { f ->
            if(f is ConfigContentItem) {
                item(cnt, f.display(p))
                cnt ++
            }
        }
        item(8, Material.ARROW, "&c戻る").event(ClickType.LEFT) { openOrderPage(p, back) }
    }

    private fun delivery(p: Player) {
        if(! req.hasContents(p)) return
        req.removeContentsFromPlayer(p)
        rew.addContentsToPlayer(p)
        p.closeInventory()
        p.title("&a&lクエストを達成しました", "&7${name.toUncolor}", 0, 40, 0)
        //p.action("&a&lアイテム &f&lと &a&lお金${money}EME &f&lと &a&l経験値${exp}Exp &f&lを受け取りました")
        announce("&7 >> &a${p.displayName}&fが${name}&fを達成しました")
        p.data?.nowQuest = null
        RPG.clearQuests.add(this)
        reopen("RPG-Quest-$npc") { p -> p.openQuest(npc) }
    }
}