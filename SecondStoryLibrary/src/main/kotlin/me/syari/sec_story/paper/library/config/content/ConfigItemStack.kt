package me.syari.sec_story.paper.library.config.content

import me.syari.sec_story.paper.library.Main.Companion.plugin
import me.syari.sec_story.paper.library.item.CustomItemStack
import me.syari.sec_story.paper.library.item.ItemStackPlus.removeItem
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType

open class ConfigItemStack(val item: CustomItemStack): ConfigContentAdd, ConfigContentRemove {
    companion object {
        private val itemList = mutableMapOf<String, (String) -> CustomItemStack?>()

        fun register(vararg pair: Pair<String, (String) -> CustomItemStack?>) {
            pair.forEach {
                itemList[it.first] = it.second
            }
        }

        fun getItem(label: String, id: String, amount: String?): CustomItemStack? {
            return getItem(label, id, amount?.toIntOrNull() ?: 1)
        }

        fun getItem(label: String, id: String, amount: Int = 1): CustomItemStack? {
            val item = itemList[label.toLowerCase()]?.invoke(id) ?: return null
            item.amount = amount
            return item
        }
    }

    override fun add(p: Player) {
        p.inventory.addItem(*item.toItemStack.toTypedArray())
    }

    override fun remove(p: Player) {
        p.removeItem(item.toOneItemStack)
    }

    override fun has(p: Player): Boolean {
        var count = item.amount
        val inv = plugin.server.createInventory(null, InventoryType.PLAYER)
        inv.contents = p.inventory.contents
        inv.contents.forEach { f ->
            val cf = CustomItemStack(f)
            if(cf.type == item.type && cf.durability == item.durability && (cf.hasItemMeta == item.hasItemMeta) && cf.display == item.display) {
                val a = f.amount
                if(a < count) {
                    count -= a
                    f.amount = 0
                } else {
                    f.amount = a - count
                    return true
                }
            }
        }
        return false
    }

    override fun display(p: Player) = item
}