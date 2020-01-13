package me.syari.sec_story.tour

import org.bukkit.scheduler.BukkitTask
import java.util.*

data class TourPlayer(val player: UUID, val id: String, val tasks: List<BukkitTask>){
    val number = String.format("%04d", (0..9999).random())
}