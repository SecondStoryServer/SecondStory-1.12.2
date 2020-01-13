package me.syari.sec_story.lib.inv

import me.syari.sec_story.lib.CustomItemStack
import me.syari.sec_story.lib.ItemStackPlus.give
import me.syari.sec_story.lib.StringEditor.toColor
import me.syari.sec_story.plugin.Init
import me.syari.sec_story.plugin.Plugin.plugin
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.*
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

object CreateInventory: Listener, Init(){
    @EventHandler
    fun on(e: InventoryOpenEvent){
        val p = e.player as Player
        val inv = e.inventory
        if(p.isOpenCustomInv){
            OriginInventoryOpenEvent(p, inv)
        } else {
            CustomInventoryOpenEvent(p, inv)
        }.callEvent()
    }

    @EventHandler
    fun on(e: InventoryClickEvent){
        val p = e.whoClicked as Player
        if(p.menuCancel){
            e.isCancelled = true
        }
        if(e.inventory == e.clickedInventory){
            p.menuEvent[e.slot to e.click]?.invoke()
        }
        p.onClick.invoke(e)
        if(e.click == ClickType.MIDDLE && p.isOp){
            if(e.currentItem != null) {
                e.isCancelled = true
                val tmp = CustomItemStack(e.currentItem)
                tmp.editNBTTag {
                    if(hasKey("DisplayItemInGUI") && getBoolean("DisplayItemInGUI")){
                        remove("DisplayItemInGUI")
                    }
                }
                p.give(tmp, ignore = true)
            }
        }
    }

    @EventHandler
    fun on(e: InventoryCloseEvent){
        val p = e.player as Player
        p.onClose.invoke(e)
        p.removeInv()
        object : BukkitRunnable(){
            override fun run() {
                val inv = p.inventory
                for(i in 0..40){
                    val item = inv.getItem(i) ?: continue
                    val cItem = CustomItemStack(item)
                    cItem.editNBTTag {
                        if(hasKey("DisplayItemInGUI") && getBoolean("DisplayItemInGUI")) inv.setItem(i, null)
                    }
                }
                p.updateInventory()
            }
        }.runTaskLater(plugin, 5)
    }

    fun inventory(display: String, type: InventoryType, run: CustomInventory.() -> Unit): CustomInventory {
        val c = CustomInventory(Bukkit.createInventory(null, type, display.toColor))
        c.run()
        return c
    }

    fun inventory(display: String, line: Int = 3, run: CustomInventory.() -> Unit): CustomInventory {
        val c = CustomInventory(Bukkit.createInventory(null, (if (line in 1..6) line else 3) * 9, display.toColor))
        c.run()
        return c
    }

    fun CustomInventory.open(player: Player){
        player.openInventory(inventory)
        player.menuCancel = cancel
        if(onClick != null) player.onClick = onClick as (InventoryClickEvent) -> Unit
        if(onClose != null) player.onClose = onClose as (InventoryCloseEvent) -> Unit
        if(events.isNotEmpty()) player.menuEvent = events
        player.menuId = id
    }

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
            if(index in 0 until inventory.size){
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
            return item(index,
                CustomItemStack(
                    mat,
                    display,
                    *lore.toTypedArray(),
                    durability = damage,
                    amount = amount
                )
            )
        }

        fun item(index: Int, mat: Material, display: String, vararg lore: String, damage: Short = 0, amount: Int = 1): Int? {
            return item(index,
                CustomItemStack(
                    mat,
                    display,
                    *lore,
                    durability = damage,
                    amount = amount
                )
            )
        }

        fun Int?.event(clickType: ClickType, run: () -> Unit): Int? {
            return if(this != null){
                events[this to clickType] = run
                this
            } else {
                null
            }
        }
    }

    private val Player.isOpenCustomInv get() = menuId != null

    private val menuIds = mutableMapOf<UUID, String?>()

    private var Player.menuId
        get() = menuIds[uniqueId]
        private set(value) {
            menuIds[uniqueId] = value
        }

    private val menuCancels = mutableSetOf<UUID>()

    private var Player.menuCancel
        get() = menuCancels.contains(uniqueId)
        set(value) {
            if(value) menuCancels.add(uniqueId)
        }

    private val menuEvents = mutableMapOf<UUID, Map<Pair<Int, ClickType?>, () -> Unit>>()

    var Player.menuEvent
        get() = menuEvents.getOrDefault(uniqueId, mapOf())
        set(value) {
            menuEvents[uniqueId] = value
        }

    private val onClickEvents = mutableMapOf<UUID, (InventoryClickEvent) -> Unit>()

    private var Player.onClick
        get() = onClickEvents.getOrDefault(uniqueId) {}
        set(value) {
            onClickEvents[uniqueId] = value
        }

    private val onCloseEvents = mutableMapOf<UUID, (InventoryCloseEvent) -> Unit>()

    private var Player.onClose
        get() = onCloseEvents.getOrDefault(uniqueId) {}
        set(value) {
            onCloseEvents[uniqueId] = value
        }

    private fun Player.removeInv(){
        menuIds.remove(uniqueId)
        menuCancels.remove(uniqueId)
        menuEvents.remove(uniqueId)
        onClickEvents.remove(uniqueId)
        onCloseEvents.remove(uniqueId)
    }

    fun reopenStartsWith(startsWith: String, inv: (Player) -> Unit){
        val c = startsWith.toColor
        menuIds.forEach { (u, i) ->
            if(i != null && i.startsWith(c)){
                val p = plugin.server.getPlayer(u)
                if(p != null){
                    inv.invoke(p)
                }
            }
        }
    }

    fun reopen(id: String, inv: (Player) -> Unit){
        val c = id.toColor
        menuIds.forEach { (u, i) ->
            if(i == c){
                val p = plugin.server.getPlayer(u)
                if(p != null){
                    inv.invoke(p)
                }
            }
        }
    }

    fun close(id: String){
        val c = id.toColor
        menuIds.forEach { (u, i) ->
            if(i == c){
                plugin.server.getPlayer(u)?.closeInventory()
            }
        }
    }

    fun createInventory(line: Int, name: String): Inventory = plugin.server.createInventory(null, line * 9, name.toColor)
    //fun createInventory(type: InventoryType, name: String) = plugin.server.createInventory(null, type, name.toColor)
}