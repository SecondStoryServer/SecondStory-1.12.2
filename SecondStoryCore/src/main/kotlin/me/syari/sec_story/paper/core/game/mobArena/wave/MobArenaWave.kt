package me.syari.sec_story.paper.core.game.mobArena.wave

import me.syari.sec_story.paper.core.Main.Companion.plugin
import me.syari.sec_story.paper.core.game.mobArena.data.MobArenaData
import me.syari.sec_story.paper.core.game.mobArena.wave.boss.MobArenaBoss
import me.syari.sec_story.paper.core.game.mobArena.wave.mob.MobArenaMob
import me.syari.sec_story.paper.library.config.content.ConfigContents
import me.syari.sec_story.paper.library.display.CreateBossBar.createBossBar
import me.syari.sec_story.paper.library.item.CustomItemStack
import me.syari.sec_story.paper.library.scheduler.CustomScheduler.runTimer
import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle

class MobArenaWave(
    private val arena: MobArenaData, val waveNum: IntRange, private val healthMulti: Double, private val all: MobArenaWaveAll, private val spawnOverride: Location?, val stop: Boolean, mobs: List<MobArenaMob>, private val boss: MobArenaBoss?, val upgrade: List<CustomItemStack>, val reward: ConfigContents
) {
    private val withPriorityList: List<Pair<IntRange, MobArenaMob>>
    private val allSum: Int

    init {
        val list = mutableListOf<Pair<IntRange, MobArenaMob>>()
        var sum = 0
        mobs.forEach { m ->
            val pre = sum
            sum += m.priority
            list.add(pre..sum to m)
        }
        allSum = sum
        withPriorityList = list
    }

    fun spawn() {
        for(i in 0 until all.getAll(arena.firstMemberSize)) {
            val r = withPriorityList.firstOrNull { f -> (0..allSum).random() in f.first } ?: continue
            val e = r.second.spawn(healthMulti, spawnOverride ?: arena.randomSpawn) ?: continue
            arena.mob.add(e.uniqueId)
        }
        if(boss != null) {
            val e = boss.spawn(spawnOverride ?: arena.randomSpawn, arena) ?: return
            arena.mob.add(e.uniqueId)
            val bar = createBossBar(e.customName, BarColor.RED, BarStyle.SOLID)
            runTimer(plugin, 20) {
                if(e.isDead) {
                    bar.delete()
                    cancel()
                } else {
                    bar.progress = e.health / e.getAttribute(Attribute.GENERIC_MAX_HEALTH).baseValue
                    bar.setPlayer(arena.players.map { it.player })
                }

            }
        }
    }
}