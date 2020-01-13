package me.syari.sec_story.tour.action

import me.syari.sec_story.data.SaveData.saveLocation
import org.bukkit.entity.Player

class TourSaveLocation: TourAction {
    override fun run(p: Player) {
        p.saveLocation()
    }
}