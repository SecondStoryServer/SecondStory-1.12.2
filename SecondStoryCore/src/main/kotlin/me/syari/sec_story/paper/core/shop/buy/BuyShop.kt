package me.syari.sec_story.paper.core.shop.buy

import me.syari.sec_story.paper.core.shop.Shop
import me.syari.sec_story.paper.library.config.content.ConfigContentRemove
import me.syari.sec_story.paper.library.inv.CreateInventory.inventory

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType

class BuyShop(npc: String, id: String, name: String, line: Int): Shop(npc, id, name, line) {
    private val con = mutableMapOf<Int, BuyItem>()

    fun setItem(index: Int, item: BuyItem) {
        con[index] = item
    }

    override fun open(p: Player) {
        inventory(name, line) {
            con.forEach { f ->
                val buy = f.value
                val item = buy.item.display(p).copy()
                val need = buy.need.getDisplay(p)
                val can = buy.canBuy(p) && need.isEmpty()
                item.addLore(
                    "", "&7左クリック : &d交換を開く", "&7シフト+左クリック : ${if(can) "&a" else "&c"}今すぐ買う"
                )
                if(need.isNotEmpty()) {
                    item.addLore("")
                    item.addLore(need)
                }
                item(f.key, item).event(ClickType.LEFT) {
                    p.openBuyPage(buy)
                }.event(ClickType.SHIFT_LEFT) {
                    if(can) {
                        buy.buy(p)
                        this@BuyShop.open(p)
                    }
                }
            }
            loadJump(p)
        }.open(p)
    }

    private enum class ShopItemStatus(val display: String, val damage: Short) {
        Can("&a購入する", 5),
        NotHasReq("&c素材が足りていません", 14),
        NotHasNeed("&c購入できません", 14)
    }

    private fun Player.openBuyPage(buy: BuyItem) {
        inventory("&9&l交換ページ", 2) {
            val indexList = listOf(
                1, 2, 3, 4, 10, 11, 12, 13
            )
            buy.req.getContents().forEachIndexed { i, f ->
                val index = indexList.getOrNull(i)
                if(index != null && f is ConfigContentRemove) item(index, f.display(this@openBuyPage))
            }
            item(6, buy.item.display(this@openBuyPage))
            val status = when {
                buy.need.getDisplay(this@openBuyPage).isNotEmpty() -> ShopItemStatus.NotHasNeed
                buy.canBuy(this@openBuyPage) -> ShopItemStatus.Can
                else -> ShopItemStatus.NotHasReq
            }
            item(
                15, Material.STAINED_GLASS_PANE, status.display, buy.need.getDisplay(this@openBuyPage), status.damage
            ).event(ClickType.LEFT) {
                if(status == ShopItemStatus.Can) {
                    buy.buy(this@openBuyPage)
                }
                openBuyPage(buy)
            }
            item(17, Material.BARRIER, "&c戻る").event(ClickType.LEFT) { this@BuyShop.open(this@openBuyPage) }
            listOf(0, 9, 5, 14, 7, 16).forEach { i ->
                item(i, Material.STAINED_GLASS_PANE, "", damage = 15)
            }
            item(8, Material.PAPER, "&c左側のアイテムを素材に右側のアイテムを交換します")
        }.open(this)
    }
}