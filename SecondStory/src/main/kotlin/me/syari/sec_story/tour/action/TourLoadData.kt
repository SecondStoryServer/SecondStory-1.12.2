package me.syari.sec_story.tour.action

import me.syari.sec_story.data.SaveData.loadSave
import org.bukkit.entity.Player

class TourLoadData: TourAction {
    override fun run(p: Player) {
        p.loadSave()
    }
}