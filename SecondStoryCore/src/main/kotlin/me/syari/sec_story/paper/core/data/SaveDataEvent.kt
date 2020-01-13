package me.syari.sec_story.paper.core.data

import me.syari.sec_story.paper.core.Main.Companion.plugin
import me.syari.sec_story.paper.core.data.SaveData.hasSave
import me.syari.sec_story.paper.core.data.SaveData.loadSave
import me.syari.sec_story.paper.core.data.SaveData.waitLoad
import me.syari.sec_story.paper.core.data.event.save.InventorySaveEvent
import me.syari.sec_story.paper.core.hook.Magic
import me.syari.sec_story.paper.core.item.GiveItemEvent
import me.syari.sec_story.paper.core.trade.TradeInviteEvent
import me.syari.sec_story.paper.core.trade.TradeStartEvent
import me.syari.sec_story.paper.library.init.EventInit
import me.syari.sec_story.paper.library.scheduler.CustomScheduler.runLater
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerRespawnEvent

object SaveDataEvent: EventInit {
    @EventHandler
    fun on(e: PlayerRespawnEvent) {
        runLater(plugin, 3) {
            if(e.player.waitLoad) e.player.loadSave()
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: TradeStartEvent) {
        if(e.player.hasSave || e.partner.hasSave) {
            e.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: TradeInviteEvent) {
        if(e.player.hasSave || e.partner.hasSave) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun on(e: InventorySaveEvent) {
        val p = e.player
        val m = Magic.getMage(p) ?: return
        val w = m.activeWand ?: return
        w.deactivate()
    }

    @EventHandler
    fun on(e: GiveItemEvent) {
        val p = e.offlinePlayer
        if(p.hasSave) {
            e.isAddPost = true
        }
    }
}