package me.syari.sec_story.guild.quest

import me.syari.sec_story.lib.CreateBossBar.createBossBar
import me.syari.sec_story.lib.CustomItemStack
import me.syari.sec_story.plugin.Plugin.plugin
import org.bukkit.Material
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.util.*

data class QuestGuildData(val quest: QuestData, var progress: Int){
    fun isEnd() = quest.req.need <= progress

    fun toDisplay(): CustomItemStack {
        return if(isEnd()){
            CustomItemStack(Material.WRITTEN_BOOK, quest.name, quest.req.toString(), "", "&aクリア済み")
        } else {
            CustomItemStack(Material.BOOK, quest.name, quest.req.toString(), "", "&6$progress &7/ &6${quest.req.need}", "", "&7&m---&d 報酬 &7&m---", "&a${String.format("%,d", quest.money)}JPY", "&6${String.format("%,d", quest.point)}GP")
        }
    }

    private val progressBar = createBossBar("&3&lギルドクエスト &b&l${quest.name}", BarColor.BLUE, BarStyle.SOLID)

    private val barHideTask = mutableMapOf<UUID, BukkitTask>()

    fun showBar(p: Player){
        progressBar.progress = progress.toDouble() / quest.req.need
        if(progressBar.containPlayer(p)){
            barHideTask[p.uniqueId]?.cancel()
        } else {
            progressBar.addPlayer(p)
        }
        barHideTask[p.uniqueId] = object : BukkitRunnable(){
            override fun run() {
                progressBar.removePlayer(p)
            }
        }.runTaskLater(plugin, 15 * 20)
    }
}