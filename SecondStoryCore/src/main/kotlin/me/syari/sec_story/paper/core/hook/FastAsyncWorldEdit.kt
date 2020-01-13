package me.syari.sec_story.paper.core.hook

import com.boydti.fawe.FaweAPI
import com.sk89q.worldedit.Vector
import com.sk89q.worldedit.world.World
import me.syari.sec_story.paper.core.Main
import me.syari.sec_story.paper.core.Main.Companion.plugin
import org.bukkit.Location
import org.bukkit.entity.Player

object FastAsyncWorldEdit {
    val Player.fawePlayer get() = FaweAPI.wrapPlayer(this)

    fun toLocation(w: World, pos: Vector): Location {
        return Location(plugin.server.getWorld(w.name), pos.x, pos.y, pos.z)
    }
}