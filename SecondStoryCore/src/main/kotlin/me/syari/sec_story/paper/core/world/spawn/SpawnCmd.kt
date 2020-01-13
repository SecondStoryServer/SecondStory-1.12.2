package me.syari.sec_story.paper.core.world.spawn

import me.syari.sec_story.paper.core.hook.MultiverseCore.toMVWorld
import me.syari.sec_story.paper.library.command.CreateCommand.createCmd
import me.syari.sec_story.paper.library.init.FunctionInit
import me.syari.sec_story.paper.library.message.SendMessage.action
import org.bukkit.entity.Player

object SpawnCmd: FunctionInit {
    override fun init() {
        createCmd("spawn") { sender, _ ->
            if(sender is Player) {
                val world = sender.world
                val run = SpawnTeleportEvent(sender, world).callEvent()
                if(run) {
                    val w = world.toMVWorld
                    val r = w.respawnToWorld?.toMVWorld
                    val loc = if(r != null) r.spawnLocation else w.spawnLocation
                    sender.teleport(loc)
                }
            }
        }

        createCmd("reset-spawn") { sender, _ ->
            if(sender is Player) {
                sender.bedSpawnLocation = null
                sender.action("&4&lリスポーン地点を削除しました")
            }
        }
    }
}