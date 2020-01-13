package me.syari.sec_story.tour

import me.syari.sec_story.lib.CustomItemStack
import me.syari.sec_story.plugin.Plugin.plugin
import me.syari.sec_story.tour.Tour.end
import me.syari.sec_story.tour.Tour.nowPlay
import me.syari.sec_story.tour.Tour.start
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

class TourData(val id: String, val hide: Boolean, val npc: String?, val ticket: CustomItemStack?, private val task: List<TourTask>){
    fun canStart(p: Player): Boolean{
        if(ticket == null) {
            return true
        } else {
            val inv = p.inventory
            for(i in 0..40){
                val item = inv.getItem(i) ?: continue
                if(ticket.isSimilar(item)) return true
            }
            return false
        }
    }

    fun remTicket(p: Player){
        if(ticket == null) {
            return
        } else {
            val inv = p.inventory
            for(i in 0..40){
                val item = inv.getItem(i) ?: continue
                if(ticket.isSimilar(item)){
                    item.amount --
                    return
                }
            }
        }
    }

    fun start(p: Player) {
        if (p.nowPlay()) return
        val tasks = mutableListOf<BukkitTask>()
        var taskCount = 0
        val endTask = task.size
        task.forEach { t ->
            tasks.add(object : BukkitRunnable() {
                override fun run() {
                    t.list.forEach { l ->
                        l.run(p)
                    }
                    taskCount++
                    if (taskCount == endTask) {
                        p.end(false)
                    }
                }
            }.runTaskLater(plugin, t.delay.toLong()))
        }
        p.start(id, tasks)
    }
}