package me.syari.sec_story.paper.core.tour

import me.syari.sec_story.paper.core.Main.Companion.plugin
import me.syari.sec_story.paper.core.tour.Tour.end
import me.syari.sec_story.paper.core.tour.Tour.nowPlay
import me.syari.sec_story.paper.core.tour.Tour.start
import me.syari.sec_story.paper.library.item.CustomItemStack
import me.syari.sec_story.paper.library.scheduler.CustomScheduler.runLater
import org.bukkit.entity.Player

class TourData(
    val id: String, val hide: Boolean, val npc: String?, val ticket: CustomItemStack?, private val task: List<TourTask>
) {
    fun canStart(p: Player): Boolean {
        if(ticket == null) {
            return true
        } else {
            val inv = p.inventory
            for(i in 0..40) {
                val item = inv.getItem(i) ?: continue
                if(ticket.isSimilar(item)) return true
            }
            return false
        }
    }

    fun remTicket(p: Player) {
        if(ticket == null) {
            return
        } else {
            val inv = p.inventory
            for(i in 0..40) {
                val item = inv.getItem(i) ?: continue
                if(ticket.isSimilar(item)) {
                    item.amount --
                    return
                }
            }
        }
    }

    fun start(p: Player) {
        if(p.nowPlay()) return
        var taskCount = 0
        val endTask = task.size
        val tasks = task.mapNotNull {
            runLater(plugin, it.delay.toLong()) {
                it.list.forEach { l ->
                    l.run(p)
                }
                taskCount ++
                if(taskCount == endTask) {
                    p.end(false)
                }
            }
        }
        p.start(id, tasks)
    }
}