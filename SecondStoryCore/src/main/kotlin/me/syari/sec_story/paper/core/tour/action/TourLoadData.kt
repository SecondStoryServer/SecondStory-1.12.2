package me.syari.sec_story.paper.core.tour.action

import me.syari.sec_story.paper.core.data.SaveData.loadSave
import org.bukkit.entity.Player

class TourLoadData: TourAction {
    override fun run(p: Player) {
        p.loadSave()
    }
}