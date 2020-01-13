package me.syari.sec_story.game.mobArena.data

import me.syari.sec_story.data.SaveData.loadSave
import me.syari.sec_story.data.SaveData.saveInventory
import me.syari.sec_story.data.SaveData.saveLocation
import me.syari.sec_story.game.kit.GameKitData
import me.syari.sec_story.game.mobArena.MobArena.arenaPlayer
import me.syari.sec_story.game.mobArena.MobArena.inMobArena
import me.syari.sec_story.game.mobArena.wave.MobArenaWave
import me.syari.sec_story.lib.CreateBossBar
import me.syari.sec_story.lib.CreateBossBar.createBossBar
import me.syari.sec_story.lib.CreateScoreBoard.createBoard
import me.syari.sec_story.lib.CustomLocation
import me.syari.sec_story.lib.ItemStackPlus.give
import me.syari.sec_story.lib.Region
import me.syari.sec_story.lib.message.SendMessage.Action
import me.syari.sec_story.lib.message.SendMessage.broadcast
import me.syari.sec_story.lib.message.SendMessage.send
import me.syari.sec_story.lib.config.CustomConfig
import me.syari.sec_story.lib.inv.CreateInventory.createInventory
import me.syari.sec_story.plugin.Plugin.plugin
import me.syari.sec_story.server.CommandBlock
import me.syari.sec_story.server.CommandBlock.addDisAllowCmd
import me.syari.sec_story.server.CommandBlock.addIgnoreWildCmd
import me.syari.sec_story.server.CommandBlock.clearDisAllowCmd
import me.syari.sec_story.server.CommandBlock.clearIgnoreWildCmd
import me.syari.sec_story.server.Server.board
import me.syari.sec_story.server.Server.hideBoard
import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.util.*

class MobArenaData(val id: String, name: String, kits: List<String>, play: Region?, playerSpawn: Location?, lobby: Region?, lobbySpawn: Location?, spec: Region?, specSpawn: Location?, mobSpawn: List<Location>, waveInterval: Long, playerLimit: Int, kitLimit: Int, val enable: Boolean, private val config: CustomConfig){
    var players = mutableListOf<MobArenaPlayer>()
    var status = MobArenaStatus.StandBy
    var mob = mutableListOf<UUID>()
    var wave = 0
    var firstMemberSize = 0

    var waveList = listOf<MobArenaWave>()
    var lastWave = 0
    var waitAllKill = false

    fun getPlayer(player: Player) = players.firstOrNull { f -> f.player == player }

    fun getLivingPlayers() = players.filter { it.play }

    private fun isEmptyLivingPlayers() = getLivingPlayers().isEmpty()

    fun canUseKit(kit: GameKitData) = players.count { m -> m.kit == kit.id } < kitLimit

    fun announce(msg: String){
        players.forEach { m ->
            m.player.send(msg)
        }
    }

    fun announce(vararg msg: Pair<String, Action?>){
        players.forEach { m ->
            m.player.send(*msg)
        }
    }

    private fun reloadBoard(){
        players.forEach { m ->
            val p = m.player
            showBoard(p)
        }
    }

    fun showBoard(p: Player){
        if(!p.hideBoard) p.hideBoard = true
        val m = getPlayer(p) ?: return
        createBoard("&a&lMobArena", p,
            -1 to "&e&m------------------------&1",
            -2 to "&a&lウェーブ &7≫ &e${wave}&2",
            -3 to "&3",
            -4 to "&a&l残り人数 &7≫ &e${getLivingPlayers().count()}人&4",
            -5 to "&5",
            -6 to "&a&lキット &7≫ &e${if(m.play) m.getKit()?.name ?:  "&c未設定" else "&b&l観戦者"}&6",
            -7 to "&e&m------------------------&7"
        )
    }

    private fun reloadProgress(){
        if(status == MobArenaStatus.NowPlay){
            bar?.title = if(lastWave < wave) "&7>> &e&lAll Clear &7<<" else "&e&lWave &e&l$wave &7/ &e&l$lastWave"
            bar?.progress = wave.toDouble() / lastWave
            bar?.setPlayer(players.map { it.player })
        } else {
            bar?.clearPlayer()
        }
    }

    fun checkReady(): Int{
        val count = players.count { f -> f.play && !f.ready }
        if(count == 0){
            if(allowStart){
                start()
            }
        }
        return count
    }

    fun checkReady(p: Player){
        val count = checkReady()
        announce("&b[MobArena] &a${p.displayName}&fが準備完了しました &f残り${count}人です")
    }

    var bar: CreateBossBar.CustomBossBar? = null
    var mainTask: BukkitTask? = null
    var allowStart = false
    var publicChest: Inventory? = createInventory(2, "§0§l共有チェスト")

    private fun firstJoin(){
        bar = createBossBar("&f&lモブアリーナが始まります &a&l/ma-debug j $id &f&lで参加しましょう", BarColor.GREEN, BarStyle.SOLID, true)
        allowStart = false
        status = MobArenaStatus.WaitReady
        var time = 90
        mainTask = object : BukkitRunnable(){
            override fun run() {
                bar?.progress = time.toDouble() / 90
                time --
                if(time == 0){
                    allowStart = true
                    if(checkReady() != 0){
                        announce("&b[MobArena] &f全員が準備完了をしたらゲームを開始します")
                    }
                    cancel()
                }
            }

            override fun cancel() {
                super.cancel()
                mainTask = null
            }
        }.runTaskTimer(plugin, 0, 20)
    }

    fun join(p: Player){
        if(status == MobArenaStatus.NowPlay){
            return p.send("&b[MobArena] &c既にゲームが始まっています /ma-debug s $id で観戦しましょう")
        }
        val m = p.arenaPlayer
        if(m != null){
            if(m.play){
                return p.send("&b[MobArena] &c既にモブアリーナに参加しています")
            } else {
                m.arena.leave(p)
            }
        }
        if(playerLimit <= players.size) {
            return p.send("&b[MobArena] &c制限人数に達しています /ma-debug s $id で観戦しましょう")
        }
        if(players.isEmpty()){
            firstJoin()
        }
        players.add(MobArenaPlayer(this, p, true))
        p.addDisAllowCmd(CommandBlock.CommandAddCause.MobArena, "*")
        p.addIgnoreWildCmd(CommandBlock.CommandAddCause.MobArena, "ma-debug")
        p.saveInventory()
        p.saveLocation()
        p.closeInventory()
        p.teleport(lobbySpawn)
        reloadBoard()
    }

    fun spec(p: Player){
        players.add(MobArenaPlayer(this, p, false))
        p.teleport(specSpawn)
        p.addDisAllowCmd(CommandBlock.CommandAddCause.MobArena, "*")
        p.addIgnoreWildCmd(CommandBlock.CommandAddCause.MobArena, "ma")
        p.saveInventory()
        p.saveLocation()
        p.closeInventory()
        showBoard(p)
    }

    fun leave(p: Player){
        if(!p.inMobArena){
            return p.send("&b[MobArena] &cモブアリーナに参加していません")
        }
        val m = getPlayer(p)
        if(m != null){
            m.reward.forEach { c ->
                c.addContentsToPlayer(p)
            }
            players.remove(m)
        }
        if(isEmptyLivingPlayers() && status != MobArenaStatus.StandBy){
            end(false)
        }
        p.loadSave()
        p.closeInventory()
        p.clearDisAllowCmd(CommandBlock.CommandAddCause.MobArena)
        p.clearIgnoreWildCmd(CommandBlock.CommandAddCause.MobArena)
        reloadBoard()
        p.hideBoard = false
        board(p)
    }

    fun start(){
        bar?.delete()
        bar = createBossBar("&e&lWave", BarColor.BLUE, BarStyle.SOLID)
        status = MobArenaStatus.NowPlay
        bar?.delete()
        mainTask?.cancel()
        players.forEach { m ->
            if(m.play){
                val p = m.player
                p.teleport(playerSpawn)
                p.activePotionEffects.clear()
                p.health = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).baseValue
                p.foodLevel = 20
                firstMemberSize ++
            }
        }
        reloadProgress()
        mainTask = object : BukkitRunnable(){
            override fun run() {
                nextWave()
            }
        }.runTaskLater(plugin, 10 * 20)
    }

    fun end(force: Boolean){
        when(status){
            MobArenaStatus.NowPlay -> {
                nextWaveTask?.cancel()
                mob.toList().forEach {
                    plugin.server.getEntity(it)?.remove()
                }
                mob.clear()
            }
            MobArenaStatus.WaitReady -> {
                mainTask?.cancel()
                bar?.delete()
            }
            MobArenaStatus.StandBy -> return
        }
        if(force){
            announce("&b[MobArena] &f強制終了しました")
            players.toList().forEach {
                leave(it.player)
            }
            players.clear()
        } else {
            broadcast("&b[MobArena] &a${name}&fのゲームが終わりました &a/ma-debug j $id &fで始めましょう")
            reloadBoard()
        }
        wave = 0
        firstMemberSize = 0
        val tmp = publicChest
        if(tmp != null){
            tmp.clear()
            publicChest = tmp
        }
        status = MobArenaStatus.StandBy
        reloadProgress()
    }

    fun onDeath(p: Player){
        object : BukkitRunnable(){
            override fun run() {
                leave(p)
                spec(p)
            }
        }.runTaskLater(plugin, 3)
    }

    private fun clearGame(){
        broadcast("""
            &b[MobArena] &fモブアリーナ&a${name}&fがクリアされました！！
            &fクリア者: &a${getLivingPlayers().joinToString(", ") { it.player.displayName }}
        """.trimIndent())
        object : BukkitRunnable(){
            override fun run() {
                getLivingPlayers().forEach { m ->
                    leave(m.player)
                    spec(m.player)
                }
                status = MobArenaStatus.StandBy
                players.clear()
            }
        }.runTaskLater(plugin, 10 * 20)
    }

    var nextWaveTask: BukkitTask? = null

    private fun giveItem(waveData: MobArenaWave){
        players.forEach { m ->
            if(m.play){
                val upgrade = waveData.upgrade
                if(upgrade.isNotEmpty()){
                    m.player.give(upgrade, ignore = true)
                }
                val reward = waveData.reward
                if(reward.isNotEmpty()){
                    m.player.send("&b[MobArena] &f報酬が追加されました")
                    m.reward.add(waveData.reward)
                }
            }
        }
    }

    fun nextWave(){
        if(status != MobArenaStatus.NowPlay) return
        wave ++
        val waveData = waveList.firstOrNull { w -> wave in w.waveNum }
        if(wave < lastWave + 1 && waveData != null){
            announce("&b[MobArena] &a${wave}ウェーブ&fに突入します")
            val stop = waveData.stop
            waitAllKill = stop
            if(!stop){
                giveItem(waveData)
                mainTask = object : BukkitRunnable(){
                    override fun run() {
                        nextWaveTask = object : BukkitRunnable(){
                            override fun run() {
                                nextWave()
                            }
                        }.runTaskLater(plugin, waveInterval)
                    }
                }.runTaskLater(plugin, 5 * 20)
            }
            waveData.spawn()
        } else {
            clearGame()
            wave = lastWave
        }
        reloadProgress()
        reloadBoard()
    }

    private fun checkDis(){
        if(status != MobArenaStatus.NowPlay) return
        if(mob.isEmpty() && waitAllKill){
            val waveData = waveList.firstOrNull { w -> wave in w.waveNum } ?: return
            giveItem(waveData)
            nextWave()
        } else {
            checkDisTask?.cancel()
            checkDisTask = object : BukkitRunnable(){
                override fun run() {
                    mob.removeIf { uuid ->
                        plugin.server.getEntity(uuid) == null
                    }
                    checkDis()
                }
            }.runTaskLater(plugin, 40 * 20)
        }
    }

    var checkDisTask: BukkitTask? = null

    fun onKillEntity(e: LivingEntity){
        mob.remove(e.uniqueId)
        checkDis()
    }

    var name = name
        set(value) {
            config.set("name", value)
            field = value
        }

    var play = play
        set(value){
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

    fun addKit(kit: String){
        val k = kits.toMutableList()
        k.add(kit)
        kits = k
    }

    fun remKit(kit: String){
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


    fun getRandomSpawn() = mobSpawn.random()
}