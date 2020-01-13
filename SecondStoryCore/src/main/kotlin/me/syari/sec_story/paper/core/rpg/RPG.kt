package me.syari.sec_story.paper.core.rpg

import me.syari.sec_story.paper.core.Main.Companion.plugin
import me.syari.sec_story.paper.core.command.CommandCancel
import me.syari.sec_story.paper.core.command.CommandCancel.addDisAllowCmd
import me.syari.sec_story.paper.core.command.CommandCancel.addIgnoreWildCmd
import me.syari.sec_story.paper.core.command.CommandCancel.clearDisAllowCmd
import me.syari.sec_story.paper.core.command.CommandCancel.clearIgnoreWildCmd
import me.syari.sec_story.paper.core.data.SaveData.loadSave
import me.syari.sec_story.paper.core.data.SaveData.saveExp
import me.syari.sec_story.paper.core.data.SaveData.saveInventory
import me.syari.sec_story.paper.core.data.SaveData.saveLocation
import me.syari.sec_story.paper.library.code.StringEditor.toColor
import me.syari.sec_story.paper.library.command.CreateCommand.createCmd
import me.syari.sec_story.paper.library.command.CreateCommand.element
import me.syari.sec_story.paper.library.command.CreateCommand.tab
import me.syari.sec_story.paper.library.display.CreateBossBar.createBossBar
import me.syari.sec_story.paper.library.init.FunctionInit
import me.syari.sec_story.paper.library.message.SendMessage.send
import me.syari.sec_story.paper.library.message.SendMessage.title
import me.syari.sec_story.paper.library.scheduler.CustomScheduler.runRepeatTimes
import me.syari.sec_story.paper.library.scoreboard.CreateScoreBoard.createBoard
import me.syari.sec_story.paper.library.scoreboard.ScoreBoardPriority
import org.bukkit.Location
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.Player

object RPG: FunctionInit {

    // TODO Respawn

    override fun init() {
        createCmd("rpg", tab { element("join", "leave") }) { sender, args ->
            fun help() = sender.send(
                """
                &b[RPG] &fコマンド
                &a/rpg join &7RPGに参加します
                &a/rpg leave &7RPGから脱退します
            """.trimIndent()
            )
            when(args.whenIndex(0)) {
                "end" -> {
                    if(! sender.isOp) return@createCmd help()
                    if(! held) return@createCmd sender.send("&b[RPG] &cRPGは開催されていません")
                    end()
                }
                "join" -> {
                    if(sender is Player) {
                        if(! wait) return@createCmd sender.send("&b[RPG] &c既に開始しています")
                        if(sender.nowRPG) return@createCmd sender.send("&b[RPG] &c既に参加しています")
                        if(! held) held()
                        sender.joinRPG()
                    }
                }
                "leave" -> {
                    if(sender is Player) {
                        if(! sender.nowRPG) return@createCmd sender.send("&b[RPG] &c参加していません")
                        sender.leaveRPG()
                    }
                }
                else -> help()
            }
        }
    }

    var lobby: Location? = null
    var spawn: Location? = null

    private var Player.isEnableBoard: Boolean
        get() = board.containsPlayer(this)
        set(value) {
            if(value){
                board.addPlayer(this)
            } else {
                board.removePlayer(this)
            }
        }

    private fun updateBoard(){
        players.forEach {
            val player = it.player
            board.updatePlayer(player)
        }
    }

    private val board by lazy {
        createBoard(plugin, "&a&lRPG", ScoreBoardPriority.High){
            line("&e&m------------------------")
            line { "&a&l残り時間 &7≫ &e$remainTime" }
            space()
            line { "&a&lお金 &7≫ &e$eme EME" }
            space()
            line("&a&lプレイヤー数 &7≫ &e${players.size}")
            line("&e&m------------------------")
        }
    }

    private var held = false

    private fun held() {
        val bar = createBossBar("&f&lRPGが始まります &a&l/rpg join &f&lで参加しましょう", BarColor.GREEN, BarStyle.SOLID, true)
        players.clear()
        held = true
        runRepeatTimes(plugin, 20, 2 * 60) {
            if(! held) {
                bar.delete()
                cancel()
                return@runRepeatTimes
            }
            bar.progress = repeatRemain / 120.0
            updateRemainTime(repeatRemain)
        }?.onEndRepeat {
            bar.delete()
            start()
        }
    }

    private var remainTime = ""

    private fun updateRemainTime(time: Int){
        remainTime = "%02d:%02d".format(time / 60, time % 60)
        updateBoard()
    }

    fun start() {
        wait = false
        players.forEach { r ->
            if(! r.player.isOnline || r.player.isDead) {
                players.remove(r)
                r.player.loadSave()
                r.player.isEnableBoard = true
            } else {
                r.player.teleport(spawn)
                r.player.title("&a&l冒険開始", "", 0, 50, 0)
            }
        }
        if(players.isEmpty()) {
            held = false
            return
        }
        runRepeatTimes(plugin, 20, 5 * 60) {
            if(!held) {
                cancel()
                return@runRepeatTimes
            }
            updateRemainTime(repeatRemain)
        }?.onEndRepeat {
            end()
        }
    }

    fun end() {
        held = false
        wait = true
        players.forEach { r ->
            r.player.isEnableBoard = false
            r.player.loadSave()
            r.player.clearDisAllowCmd(CommandCancel.CommandAddCause.RPG)
            r.player.clearIgnoreWildCmd(CommandCancel.CommandAddCause.RPG)
        }
        players.clear()
        clearQuests.clear()
    }

    fun announce(msg: String) {
        val c = msg.toColor
        players.forEach { r -> r.player.send(c) }
    }

    val players = mutableListOf<RPGPlayer>()

    data class RPGPlayer(val player: Player) {
        var eme = 0
        var nowQuest: Quest? = null
    }

    val clearQuests = mutableListOf<Quest>()

    private fun Player.joinRPG() {
        players.add(RPGPlayer(this))
        isEnableBoard = true
        closeInventory()
        saveInventory()
        saveLocation()
        saveExp()
        announce("&7 >> &aRPG&fに&a$displayName&fが参加しました")
        teleport(lobby)
        addDisAllowCmd(CommandCancel.CommandAddCause.RPG, "*")
        addIgnoreWildCmd(CommandCancel.CommandAddCause.RPG, "rpg")
    }

    fun Player.leaveRPG() {
        if(! players.contains(RPGPlayer(this))) return
        announce("&7 >> &aRPG&fから&a$displayName&fが脱退しました")
        players.remove(RPGPlayer(this))
        if(players.isEmpty()) {
            end()
        }
        isEnableBoard = false
        loadSave()
        clearDisAllowCmd(CommandCancel.CommandAddCause.RPG)
        clearIgnoreWildCmd(CommandCancel.CommandAddCause.RPG)
    }

    val Player.nowRPG get() = players.contains(RPGPlayer(this))

    private var wait = true

    val Player.data get() = players.firstOrNull { r -> r.player == this }

    var Player.eme: Int
        get() = data?.eme ?: 0
        set(value) {
            data?.eme = value
        }
}