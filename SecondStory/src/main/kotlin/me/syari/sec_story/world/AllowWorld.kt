package me.syari.sec_story.world

import com.elmakers.mine.bukkit.api.event.PreCastEvent
import com.shampaggon.crackshot.events.WeaponDamageEntityEvent
import com.shampaggon.crackshot.events.WeaponPrepareShootEvent
import com.shampaggon.crackshot.events.WeaponTriggerEvent
import me.syari.sec_story.hook.MultiverseCore.firstSpawnWorld
import me.syari.sec_story.lib.message.SendMessage.action
import me.syari.sec_story.lib.config.CreateConfig.config
import me.syari.sec_story.plugin.Init
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerRespawnEvent

object AllowWorld : Init(), Listener {
    fun CommandSender.loadAllow(){
        clearAllow()
        config("allow.yml", false){
            output = this@loadAllow

            getStringList("Move")?.forEach { name ->
                moveAllow.add(name)
            }
            getStringList("CrackShot")?.forEach { name ->
                crackShotAllow.add(name)
            }
            getStringList("Magic")?.forEach { name ->
                magicAllow.add(name)
            }
        }
    }

    private val moveAllow = mutableListOf<String>()
    private val crackShotAllow = mutableListOf<String>()
    private val magicAllow = mutableListOf<String>()

    private fun clearAllow(){
        moveAllow.clear()
        crackShotAllow.clear()
        magicAllow.clear()
    }

    @EventHandler
    fun onMove(e: PlayerJoinEvent){
        val p = e.player ?: return
        if(p.world.name !in moveAllow && !p.isOp){
            p.teleport(firstSpawnWorld.spawnLocation)
        }
    }

    @EventHandler
    fun onMove(e: PlayerChangedWorldEvent){
        val p = e.player
        val to = p.location
        if(to.world.name !in moveAllow && !p.isOp){
            p.teleport(firstSpawnWorld.spawnLocation)
        }
    }

    @EventHandler
    fun onMove(e: PlayerRespawnEvent){
        if(!e.isBedSpawn) return
        val p = e.player ?: return
        val loc = e.respawnLocation ?: return
        if(loc.world.name !in moveAllow && !p.isOp){
            p.setBedSpawnLocation(null, true)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onCrackShot(e: WeaponPrepareShootEvent){
        val p = e.player ?: return
        val loc = p.location ?: return
        if(loc.world.name !in crackShotAllow && !p.isOp){
            p.action("&c&lこのワールドで使用できないアイテムです")
            e.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onCrackShot(e: WeaponDamageEntityEvent){
        val p = e.player ?: return
        val loc = p.location ?: return
        if(loc.world.name !in crackShotAllow && !p.isOp){
            p.action("&c&lこのワールドで使用できないアイテムです")
            e.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onCrackShot(e: WeaponTriggerEvent){
        val p = e.player ?: return
        val loc = p.location ?: return
        if(loc.world.name !in crackShotAllow && !p.isOp){
            p.action("&c&lこのワールドで使用できないアイテムです")
            e.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onMagic(e: PreCastEvent){
        val p = e.mage.player ?: return
        val loc = p.location ?: return
        if(loc.world.name !in magicAllow && !p.isOp){
            p.action("&c&lこのワールドで使用できないアイテムです")
            e.isCancelled = true
        }
    }
}