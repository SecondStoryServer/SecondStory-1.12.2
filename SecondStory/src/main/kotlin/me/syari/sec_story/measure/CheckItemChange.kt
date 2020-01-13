package me.syari.sec_story.measure

import me.syari.sec_story.hook.CrackShot
import me.syari.sec_story.hook.MythicMobs
import me.syari.sec_story.lib.CustomItemStack
import me.syari.sec_story.lib.inv.OriginInventoryOpenEvent
import me.syari.sec_story.plugin.Init
import me.syari.sec_story.tour.Tour
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.Inventory

object CheckItemChange: Init(), Listener {
    private fun checkItemChange(inv: Inventory, index: Int){
        val item = inv.getItem(index) ?: return

        /* Check CrackShot */
        val title = CrackShot.getTitleFromCrackShot(item)
        if(title != null){
            val w = CrackShot.getItemFromCrackShot(title, 1)
            if(w != null){
                val lore = w.lore
                if(lore != item.lore){
                    item.lore = lore
                    inv.setItem(index, item)
                    return
                }
            }
        }

        /* Check MythicMobs */
        val cItem = CustomItemStack(item)
        val display = cItem.display
        if(display != null && cItem.hasColorInDisplay){
            val mItem = MythicMobs.getMythicItemFromDisplay(display)
            if(mItem != null){
                if(cItem.display == display && (cItem.hasItemMeta != mItem.hasItemMeta() || cItem.itemMeta != mItem.itemMeta)){
                    cItem.itemMeta = mItem.itemMeta
                    inv.setItem(index, cItem.toOneItemStack)
                    return
                }
            }
        }

        /* Check SecondStory */
        var ssItemType: String? = null
        cItem.editNBTTag {
            if(hasKey("ssItem") && getBoolean("ssItem")){
                ssItemType = getString("ssItemType")
            }
        }
        if(ssItemType != null){
            val sItem = when(ssItemType){
                "TourTicket" -> {
                    var ticketID: String? = null
                    cItem.editNBTTag { ticketID = getString("TourTicketID") }
                    ticketID?.let { Tour.getTicket(it, cItem.amount) }
                }
                else -> null
            }
            if(sItem != null && (cItem.hasItemMeta != sItem.hasItemMeta || cItem.itemMeta != sItem.itemMeta)){
                cItem.itemMeta = sItem.itemMeta
                inv.setItem(index, cItem.toOneItemStack)
                return
            }
        }
    }

    private fun checkItemChange(p: Player){
        val inv = p.inventory
        for(i in 0..40){
            checkItemChange(inv, i)
        }
        p.updateInventory()
    }

    @EventHandler
    fun on(e: PlayerJoinEvent){
        val p = e.player ?: return
        checkItemChange(p)
    }

    @EventHandler
    fun on(e: OriginInventoryOpenEvent){
        val p = e.player as? Player ?: return
        val inv = e.inventory
        for(i in 0 until inv.size){
            checkItemChange(inv, i)
        }
    }
}