package me.syari.sec_story.world.spawn

import me.syari.sec_story.hook.MultiverseCore.toMVWorld
import me.syari.sec_story.lib.message.SendMessage.action
import me.syari.sec_story.lib.command.CreateCommand.createCmd
import me.syari.sec_story.plugin.Init
import org.bukkit.entity.Player
import org.bukkit.event.Listener

object SpawnCmd : Listener, Init(){
    override fun init() {
        createCmd("spawn") { sender, _ ->
            if (sender is Player) {
                val world = sender.world
                val run = SpawnTeleportEvent(sender, world).callEvent()
                if(run){
                    val w = world.toMVWorld
                    val r = w.respawnToWorld?.toMVWorld
                    val loc = if(r != null) r.spawnLocation else w.spawnLocation
                    sender.teleport(loc)
                }
            }
        }

        createCmd("reset-spawn"){ sender, _ ->
            if(sender is Player){
                sender.bedSpawnLocation = null
                sender.action("&4&lリスポーン地点を削除しました")
            }
        }
    }
}