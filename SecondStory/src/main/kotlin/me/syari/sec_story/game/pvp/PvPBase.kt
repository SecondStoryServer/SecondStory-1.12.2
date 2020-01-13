package me.syari.sec_story.game.pvp

import me.syari.sec_story.game.kit.GameKitData
import me.syari.sec_story.game.pvp.field.PvPField
import me.syari.sec_story.lib.inv.CreateInventory.inventory
import me.syari.sec_story.lib.inv.CreateInventory.open
import me.syari.sec_story.plugin.Plugin.plugin
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

open class PvPBase(val id: String, val display: String, private val endTime: Long?, private val timerPeriod: Long, val field: PvPField, private val kit: List<GameKitData>) {
    fun selectKit(p: Player){
        inventory("&9&lキット選択", (kit.size - 1) / 9 + 1){
            kit.forEachIndexed { i, k ->
                item(i, k.icon)
            }
        }.open(p)
    }

    fun start(){
        onStart()
        timer.runTaskTimer(plugin, 0, timerPeriod)
        if(endTime != null){
            endTimer.runTaskLater(plugin, endTime)
        }
    }

    open fun onStart(){}

    private var passTime = 0L

    fun getDisplayTime() = if(endTime != null) "%02d:%02d".format((endTime - passTime) / 60, (endTime - passTime) % 60) else "%02d:%02d".format(passTime / 60, passTime % 60)

    private val timer = object : BukkitRunnable(){
        override fun run() {
            onTimer()
            passTime += timerPeriod
        }
    }

    private val endTimer = object : BukkitRunnable(){
        override fun run() {
            onTimeUp()
        }
    }

    open fun onTimer(){}

    fun end(){
        onEnd()
        timer.cancel()
        endTimer.cancel()
    }

    open fun onEnd(){}

    open fun onTimeUp(){}
}