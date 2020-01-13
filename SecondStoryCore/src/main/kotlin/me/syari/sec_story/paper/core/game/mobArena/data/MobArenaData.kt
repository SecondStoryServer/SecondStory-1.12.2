package me.syari.sec_story.paper.core.game.mobArena.data

import me.syari.sec_story.paper.core.Main.Companion.plugin
import me.syari.sec_story.paper.core.command.CommandCancel
import me.syari.sec_story.paper.core.command.CommandCancel.addDisAllowCmd
import me.syari.sec_story.paper.core.command.CommandCancel.addIgnoreWildCmd
import me.syari.sec_story.paper.core.command.CommandCancel.clearDisAllowCmd
import me.syari.sec_story.paper.core.command.CommandCancel.clearIgnoreWildCmd
import me.syari.sec_story.paper.core.data.SaveData.loadSave
import me.syari.sec_story.paper.core.data.SaveData.saveInventory
import me.syari.sec_story.paper.core.data.SaveData.saveLocation
import me.syari.sec_story.paper.core.game.kit.GameKitData
import me.syari.sec_story.paper.core.game.mobArena.MobArena.arenaPlayer
import me.syari.sec_story.paper.core.game.mobArena.MobArena.inMobArena
import me.syari.sec_story.paper.core.game.mobArena.wave.MobArenaWave
import me.syari.sec_story.paper.core.item.GiveItem.give
import me.syari.sec_story.paper.core.server.Server.board
import me.syari.sec_story.paper.library.config.CustomConfig
import me.syari.sec_story.paper.library.display.CreateBossBar.createBossBar
import me.syari.sec_story.paper.library.display.CustomBossBar
import me.syari.sec_story.paper.library.inv.CreateInventory.createInventory
import me.syari.sec_story.paper.library.message.JsonAction
import me.syari.sec_story.paper.library.message.SendMessage.broadcast
import me.syari.sec_story.paper.library.message.SendMessage.send
import me.syari.sec_story.paper.library.scheduler.CustomScheduler.runLater
import me.syari.sec_story.paper.library.scheduler.CustomScheduler.runRepeatTimes
import me.syari.sec_story.paper.library.scheduler.CustomTask
import me.syari.sec_story.paper.library.scoreboard.CreateScoreBoard.createBoard
import me.syari.sec_story.paper.library.scoreboard.ScoreBoardPriority
import me.syari.sec_story.paper.library.server.Server.getEntity
import me.syari.sec_story.paper.library.world.CustomLocation
import me.syari.sec_story.paper.library.world.Region
import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import java.util.*

class MobArenaData(
    val id: String, name: String, kits: List<String>, play: Region?, playerSpawn: Location?, lobby: Region?, lobbySpawn: Location?, spec: Region?, specSpawn: Location?, mobSpawn: List<Location>, waveInterval: Long, playerLimit: Int, kitLimit: Int, val enable: Boolean, private val config: CustomConfig
) {
    var players = mutableListOf<MobArenaPlayer>()
    var status = MobArenaStatus.StandBy
    var mob = mutableListOf<UUID>()
    var wave = 0
    var firstMemberSize = 0

    var waveList = listOf<MobArenaWave>()
    var lastWave = 0
    var waitAllKill = false

    private val board = createBoard(plugin, "&a&lMobArena", ScoreBoardPriority.High){
        line("&e&m------------------------")
        line("&a&lウェーブ &7≫ &e${wave}")
        space()
        line("&a&l残り人数 &7≫ &e${livingPlayers.count()}人")
        space()
        line {
            val m = getPlayer(this)
            "&a&lキット &7≫ &e${if(m != null && m.play) m.getKit()?.name ?: "&c未設定" else "&b&l観戦者"}"
        }
        line("&e&m------------------------")
    }

    fun getPlayer(player: Player) = players.firstOrNull { f -> f.player == player }

    val livingPlayers get() = players.filter { it.play }

    private val isEmptyLivingPlayers get() = livingPlayers.isEmpty()

    fun canUseKit(kit: GameKitData) = players.count { m -> m.kit == kit.id } < kitLimit

    fun announce(msg: String) {
        players.forEach { m ->
            m.player.send(msg)
        }
    }

    fun announce(vararg msg: Pair<String, JsonAction?>) {
        players.forEach { m ->
            m.player.send(*msg)
        }
    }

    private fun updateAllBoard(){
        players.forEach { m ->
            updateBoard(m.player)
        }
    }

    fun updateBoard(p: Player){
        board.updatePlayer(p)
    }

    private fun reloadProgress() {
        if(status == MobArenaStatus.NowPlay) {
            bar?.title = if(lastWave < wave) "&7>> &e&lAll Clear &7<<" else "&e&lWave &e&l$wave &7/ &e&l$lastWave"
            bar?.progress = wave.toDouble() / lastWave
            bar?.setPlayer(players.map { it.player })
        } else {
            bar?.clearPlayer()
        }
    }

    private fun checkReady(): Int {
        val count = players.count { f -> f.play && ! f.ready }
        if(count == 0) {
            if(allowStart) {
                start()
            }
        }
        return count
    }

    fun checkReady(p: Player) {
        val count = checkReady()
        announce("&b[MobArena] &a${p.displayName}&fが準備完了しました &f残り${count}人です")
    }

    var bar: CustomBossBar? = null
    var mainTask: CustomTask? = null
    var allowStart = false
    var publicChest: Inventory? = createInventory(2, "§0§l共有チェスト")

    private fun firstJoin() {
        bar = createBossBar("&f&lモブアリーナが始まります &a&l/ma-debug j $id &f&lで参加しましょう", BarColor.GREEN, BarStyle.SOLID, true)
        allowStart = false
        status = MobArenaStatus.WaitReady
        mainTask = runRepeatTimes(plugin, 20, 90) {
            bar?.progress = repeatRemain.toDouble() / 90
        }?.onEndRepeat {
            allowStart = true
            if(checkReady() != 0) {
                announce("&b[MobArena] &f全員が準備完了をしたらゲームを開始します")
            }
        }
    }

    fun join(p: Player) {
        if(status == MobArenaStatus.NowPlay) {
            return p.send("&b[MobArena] &c既にゲームが始まっています /ma-debug s $id で観戦しましょう")
        }
        val m = p.arenaPlayer
        if(m != null) {
            if(m.play) {
                return p.send("&b[MobArena] &c既にモブアリーナに参加しています")
            } else {
                m.arena.leave(p)
            }
        }
        if(playerLimit <= players.size) {
            return p.send("&b[MobArena] &c制限人数に達しています /ma-debug s $id で観戦しましょう")
        }
        if(players.isEmpty()) {
            firstJoin()
        }
        players.add(MobArenaPlayer(this, p, true))
        p.addDisAllowCmd(CommandCancel.CommandAddCause.MobArena, "*")
        p.addIgnoreWildCmd(CommandCancel.CommandAddCause.MobArena, "ma-debug")
        p.saveInventory()
        p.saveLocation()
        p.closeInventory()
        p.teleport(lobbySpawn)
        updateAllBoard()
    }

    fun spec(p: Player) {
        players.add(MobArenaPlayer(this, p, false))
        p.teleport(specSpawn)
        p.addDisAllowCmd(CommandCancel.CommandAddCause.MobArena, "*")
        p.addIgnoreWildCmd(CommandCancel.CommandAddCause.MobArena, "ma")
        p.saveInventory()
        p.saveLocation()
        p.closeInventory()
        updateBoard(p)
    }

    fun leave(p: Player) {
        if(! p.inMobArena) {
            return p.send("&b[MobArena] &cモブアリーナに参加していません")
        }
        val m = getPlayer(p)
        if(m != null) {
            m.reward.forEach { c ->
                c.addContentsToPlayer(p)
            }
            players.remove(m)
        }
        if(isEmptyLivingPlayers && status != MobArenaStatus.StandBy) {
            end(false)
        }
        p.loadSave()
        p.closeInventory()
        p.clearDisAllowCmd(CommandCancel.CommandAddCause.MobArena)
        p.clearIgnoreWildCmd(CommandCancel.CommandAddCause.MobArena)
        updateAllBoard()
        board.removePlayer(p)
    }

    fun start() {
        bar?.delete()
        bar = createBossBar("&e&lWave", BarColor.BLUE, BarStyle.SOLID)
        status = MobArenaStatus.NowPlay
        bar?.delete()
        mainTask?.cancel()
        players.forEach { m ->
            if(m.play) {
                val p = m.player
                p.teleport(playerSpawn)
                p.activePotionEffects.clear()
                p.health = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).baseValue
                p.foodLevel = 20
                firstMemberSize ++
            }
        }
        reloadProgress()
        mainTask = runLater(plugin, 10 * 20) {
            nextWave()
        }
    }

    fun end(force: Boolean) {
        when(status) {
            MobArenaStatus.NowPlay -> {
                nextWaveTask?.cancel()
                mob.toList().forEach {
                    getEntity(it)?.remove()
                }
                mob.clear()
            }
            MobArenaStatus.WaitReady -> {
                mainTask?.cancel()
                bar?.delete()
            }
            MobArenaStatus.StandBy -> return
        }
        if(force) {
            announce("&b[MobArena] &f強制終了しました")
            players.toList().forEach {
                leave(it.player)
            }
            players.clear()
        } else {
            broadcast("&b[MobArena] &a${name}&fのゲームが終わりました &a/ma-debug j $id &fで始めましょう")
            updateAllBoard()
        }
        wave = 0
        firstMemberSize = 0
        val tmp = publicChest
        if(tmp != null) {
            tmp.clear()
            publicChest = tmp
        }
        status = MobArenaStatus.StandBy
        reloadProgress()
    }

    fun onDeath(p: Player) {
        runLater(plugin, 3) {
            leave(p)
            spec(p)
        }
    }

    private fun clearGame() {
        broadcast(
            """
            &b[MobArena] &fモブアリーナ&a${name}&fがクリアされました！！
            &fクリア者: &a${livingPlayers.joinToString(", ") { it.player.displayName }}
        """.trimIndent()
        )
        runLater(plugin, 10 * 20) {
            livingPlayers.forEach { m ->
                leave(m.player)
                spec(m.player)
            }
            status = MobArenaStatus.StandBy
            players.clear()
        }
    }

    var nextWaveTask: CustomTask? = null

    private fun giveItem(waveData: MobArenaWave) {
        players.forEach { m ->
            if(m.play) {
                val upgrade = waveData.upgrade
                if(upgrade.isNotEmpty()) {
                    m.player.give(upgrade, ignore = true)
                }
                val reward = waveData.reward
                if(reward.isNotEmpty()) {
                    m.player.send("&b[MobArena] &f報酬が追加されました")
                    m.reward.add(waveData.reward)
                }
            }
        }
    }

    fun nextWave() {
        if(status != MobArenaStatus.NowPlay) return
        wave ++
        val waveData = waveList.firstOrNull { w -> wave in w.waveNum }
        if(wave < lastWave + 1 && waveData != null) {
            announce("&b[MobArena] &a${wave}ウェーブ&fに突入します")
            val stop = waveData.stop
            waitAllKill = stop
            if(! stop) {
                giveItem(waveData)
                mainTask = runLater(plugin, 5 * 20) {
                    nextWaveTask = runLater(plugin, waveInterval) {
                        nextWave()
                    }
                }
            }
            waveData.spawn()
        } else {
            clearGame()
            wave = lastWave
        }
        reloadProgress()
        updateAllBoard()
    }

    var checkDisTask: CustomTask? = null

    private fun checkDis() {
        if(status != MobArenaStatus.NowPlay) return
        if(mob.isEmpty() && waitAllKill) {
            val waveData = waveList.firstOrNull { w -> wave in w.waveNum } ?: return
            giveItem(waveData)
            nextWave()
        } else {
            checkDisTask?.cancel()
            checkDisTask = runLater(plugin, 40 * 20) {
                mob.removeIf { uuid ->
                    getEntity(uuid) == null
                }
                checkDis()
            }
        }
    }

    fun onKillEntity(e: LivingEntity) {
        mob.remove(e.uniqueId)
        checkDis()
    }

    var name = name
        set(value) {
            config.set("name", value)
            field = value
        }

    var play = play
        set(value) {
            config.set("play.pos1", value?.max?.toString(), false)
            config.set("play.pos2", value?.min?.toString(), false)
            config.save()
            field = value
        }

    var playerSpawn = playerSpawn
        set(value) {
            config.set("play.spawn", value?.let { CustomLocation(it).toStringWithYawPitch })
            field = value
        }

    var lobby = lobby
        set(value) {
            config.set("lobby.pos1", value?.max?.toString(), false)
            config.set("lobby.pos2", value?.min?.toString(), false)
            config.save()
            field = value
        }

    var lobbySpawn = lobbySpawn
        set(value) {
            config.set("lobby.spawn", value?.let { CustomLocation(it).toStringWithYawPitch })
            field = value
        }

    var spec = spec
        set(value) {
            config.set("spec.pos1", value?.max?.toString(), false)
            config.set("spec.pos2", value?.min?.toString(), false)
            config.save()
            field = value
        }

    var specSpawn = specSpawn
        set(value) {
            config.set("spec.spawn", value?.let { CustomLocation(it).toStringWithYawPitch })
            field = value
        }

    var mobSpawn = mobSpawn
        set(value) {
            config.set("spawn", value.map { m -> CustomLocation(m).toString() })
            field = value
        }

    var kits = kits
        set(value) {
            config.set("kit", value)
            field = value
        }

    fun containsKit(kit: String) = kits.contains(kit)

    fun addKit(kit: String) {
        val k = kits.toMutableList()
        k.add(kit)
        kits = k
    }

    fun remKit(kit: String) {
        val k = kits.toMutableList()
        k.remove(kit)
        kits = k
    }

    var waveInterval = waveInterval
        set(value) {
            config.set("wave-interval", value)
            field = value
        }

    var kitLimit = kitLimit
        set(value) {
            config.set("limit.kit", value)
            field = value
        }

    var playerLimit = playerLimit
        set(value) {
            config.set("limit.player", value)
            field = value
        }

    val randomSpawn get() = mobSpawn.random()
}