package me.syari.sec_story.shop.sell

import me.syari.sec_story.lib.CustomItemStack
import me.syari.sec_story.lib.message.SendMessage.send
import me.syari.sec_story.lib.inv.CreateInventory.inventory
import me.syari.sec_story.lib.inv.CreateInventory.open
import me.syari.sec_story.player.Money.money
import me.syari.sec_story.plugin.Plugin
import me.syari.sec_story.shop.Shop
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.scheduler.BukkitRunnable

class SellShop(npc: String, id: String, name: String, line: Int, private val display: Int) : Shop(npc, id, name, line){
    private val sells = mutableMapOf<SellItem, Int>()

    override fun open(p: Player){
        inventory(name, line) {
            onClick = { e -> onSellClick(e) }
            onClose = { e -> onSellClose(e) }
            if(display in 0 until line * 9) item(display, Material.EMERALD, "&6&l売却額 : 0", "&a閉じることで売却できます")
            loadJump(p)
        }.open(p)
    }

    private fun onSellClick(e: InventoryClickEvent){
        if(e.click == ClickType.NUMBER_KEY){
            e.isCancelled = true
        } else {
            val i = CustomItemStack(e.currentItem, 1)
            val inv = e.inventory
            val p = e.whoClicked as Player
            if(i.type == Material.AIR || i.containSell) {
                e.isCancelled = false
                object : BukkitRunnable(){
                    override fun run() {
                        if(display in 0 until line * 9) p.openInventory.topInventory.setItem(display, CustomItemStack(Material.EMERALD, "&6&l売却額 : ${String.format("%,d", inv.countPrice)}").toOneItemStack)
                        p.updateInventory()
                    }
                }.runTaskLater(Plugin.plugin, 3)
            }
        }
    }

    private fun onSellClose(e: InventoryCloseEvent){
        val p = e.player as Player
        val inv = e.inventory
        val price = inv.countPrice
        if(0 < price){
            p.money += price
            p.send("&b[Shop] &fアイテムを売却して&a${String.format("%,d", price)}JPY&f手に入れました")
        }
    }

    val Inventory.countPrice: Int
        get() {
            var sum = 0
            contents.filterNotNull().forEach { i ->
                sum += CustomItemStack(i, 1).getPrice * i.amount
            }
            return sum
        }

    private val CustomItemStack.containSell get() = sells.containsKey(this.toSellItem)

    private val CustomItemStack.getPrice get() = sells.getOrDefault(this.toSellItem, 0)

    private val CustomItemStack.toSellItem get() = SellItem(display, type, durability)

    fun addSellItem(item: CustomItemStack, price: Int) {
        sells[item.toSellItem] = price
    }
}