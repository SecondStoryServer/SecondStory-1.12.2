package me.syari.sec_story.paper.core.tour.action

import org.bukkit.entity.Player

interface TourAction {
    fun run(p: Player)
}