package me.syari.sec_story.paper.library.inv

import me.syari.sec_story.paper.library.inv.CreateInventory.menuPlayer
import me.syari.sec_story.paper.library.item.CustomItemStack
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class CustomInventory(val inventory: Inventory) {
    val events = mutableMapOf<Pair<Int, ClickType?>, () -> Unit>()
    var cancel = true
    var onClick: ((InventoryClickEvent) -> Unit)? = null
    var onClose: ((InventoryCloseEvent) -> Unit)? = null
    var id: String = inventory.name
    var contents: Array<ItemStack> = inventory.contents
        set(value) {
            inventory.contents = value
            field = value
        }

    private val firstEmpty get() = inventory.firstEmpty()

    fun item(item: CustomItemStack): Int? {
        return item(firstEmpty, item)
    }

    fun item(index: Int, item: ItemStack): Int? {
        if(index in 0 until inventory.size) {
            val tmp = CustomItemStack(item)
            tmp.editNBTTag {
                setBoolean("DisplayItemInGUI", true)
            }
            inventory.setItem(index, tmp.toOneItemStack)
            return index
        }
        return null
    }

    fun item(index: Int, item: CustomItemStack): Int? {
        return item(index, item.toOneItemStack)
    }

    fun item(index: Int, mat: Material, display: String, lore: List<String>, damage: Short = 0, amount: Int = 1): Int? {
        return item(
            index, CustomItemStack(
                mat, display, *lore.toTypedArray(), durability = damage, amount = amount
            )
        )
    }

    fun item(
        index: Int, mat: Material, display: String, vararg lore: String, damage: Short = 0, amount: Int = 1
    ): Int? {
        return item(
            index, CustomItemStack(
                mat, display, *lore, durability = damage, amount = amount
            )
        )
    }

    fun Int?.event(clickType: ClickType, run: () -> Unit): Int? {
        return if(this != null) {
            events[this to clickType] = run
            this
        } else {
            null
        }
    }

    fun open(player: Player) {
        player.openInventory(inventory)
        player.menuPlayer = InventoryPlayerData(
            id, cancel, onClick, onClose, events
        )
    }
}