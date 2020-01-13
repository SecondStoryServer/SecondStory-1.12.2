package me.syari.sec_story.paper.core.hook

import io.lumine.artifacts.Artifacts
import io.lumine.artifacts.profiles.Profile
import me.syari.sec_story.paper.core.data.event.DataEventType
import me.syari.sec_story.paper.core.data.event.PlayerDataEvent
import me.syari.sec_story.paper.core.game.kit.event.KitSetEvent
import me.syari.sec_story.paper.library.init.EventInit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler

object MythicArtifact: EventInit {
    private val hook = Artifacts.inst()

    private val Player.profile: Profile
        get() = hook.profileManager.getPlayerProfile(this)

    private fun reloadItems(p: Player) {
        val profile = p.profile
        profile.parseWeapons()
        profile.parseArmor()
    }

    @EventHandler
    fun on(e: PlayerDataEvent) {
        if(e.type == DataEventType.Inventory) {
            reloadItems(e.player)
        }
    }

    @EventHandler
    fun on(e: KitSetEvent) {
        reloadItems(e.player)
    }
}