package me.syari.sec_story.paper.core.game.summonArena

import me.syari.sec_story.paper.core.game.summonArena.SummonArena.getMob
import me.syari.sec_story.paper.core.game.summonArena.SummonArena.selectMob
import me.syari.sec_story.paper.library.inv.CreateInventory.inventory

import me.syari.sec_story.paper.library.item.CustomItemStack
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType

data class SummonArenaMob(
    val id: String, val name: String, val enable: Boolean, val group: String, val child: List<String>, val exp: Int, val summon: Int, val reward: Int, val icon: CustomItemStack, val author: String, val difficulty: Int
) {
    fun getIcon(viewer: Player): CustomItemStack {
        return icon.copy {
            display = "&f$name"
            lore = getInfo(viewer)
            amount = difficulty
        }
    }

    private fun getInfo(viewer: Player): MutableList<String> {
        return if(viewer.isOp) {
            mutableListOf(
                "&a難易度: $difficulty",
                "&a消費召喚ポイント: $summon",
                "",
                "&7作者: $author",
                "",
                "&7モンスター: $id",
                "&7グループ: $group",
                "&7獲得経験値: $exp",
                "&7討伐ポイント: $reward",
                "",
                "&a左クリックで選択",
                "&a右クリックでチャイルドを表示"
            )
        } else {
            mutableListOf(
                "&a難易度: $difficulty", "&a消費召喚ポイント: $summon", "", "&7作者: $author", "", "&a左クリックで選択"
            )
        }
    }

    fun openChildInfo(p: Player, summonArenaData: SummonArenaData) {
        inventory("&9モンスターチャイルド情報") {
            child.forEachIndexed { index, id ->
                val m = getMob(id)
                if(m != null) {
                    item(index, m.getIcon(p)).event(ClickType.LEFT) {
                        summonArenaData.setMob(p, m)
                        p.closeInventory()
                    }.event(ClickType.RIGHT) {
                        if(p.isOp) {
                            m.openChildInfo(p, summonArenaData)
                        }
                    }
                }
            }
            item(26, Material.BARRIER, "&c戻る").event(ClickType.LEFT) {
                p.selectMob(summonArenaData)
            }
        }.open(p)
    }
}