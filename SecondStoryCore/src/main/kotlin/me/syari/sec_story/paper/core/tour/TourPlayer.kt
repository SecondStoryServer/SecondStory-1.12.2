package me.syari.sec_story.paper.core.tour

import me.syari.sec_story.paper.library.scheduler.CustomTask
import java.util.*

data class TourPlayer(val player: UUID, val id: String, val tasks: List<CustomTask>) {
    val number = String.format("%04d", (0..9999).random())
}