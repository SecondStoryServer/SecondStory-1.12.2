package me.syari.sec_story.paper.library.scoreboard

import me.syari.sec_story.paper.library.code.StringEditor.toColor
import me.syari.sec_story.paper.library.player.UUIDPlayer
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard

data class CustomScoreBoard(private val plugin: JavaPlugin, private val title: String, val priority: ScoreBoardPriority): CustomScoreBoardEdit {
    companion object {
        private val playerList = mutableMapOf<UUIDPlayer, ScoreBoardPlayer>()
    }

    private val list = mutableMapOf<Int, ScoreBoardLine>()

    private var line = 0

    override fun line(text: String){
        list[line] = ScoreBoardLine.Constant(text)
        line --
    }

    override fun line(text: Player.() -> String){
        list[line] = ScoreBoardLine.Invoke(text)
        line --
    }

    fun containsPlayer(player: Player): Boolean {
        return playerList.containsKey(UUIDPlayer(player))
    }

    fun addPlayer(vararg players: Player){
        players.forEach { player ->
            val uuidPlayer = UUIDPlayer(player)
            val data = playerList.getOrPut(uuidPlayer){ ScoreBoardPlayer(uuidPlayer) }
            data.addBoard(this)
        }
    }

    fun removePlayer(vararg players: Player){
        players.forEach { player ->
            val uuidPlayer = UUIDPlayer(player)
            val data = playerList[uuidPlayer] ?: return
            val size = data.removeBoard(this)
            if(size == 0){
                playerList.remove(uuidPlayer)
            }
        }
    }

    fun updatePlayer(player: Player){
        val scoreBoardPlayer = playerList[UUIDPlayer(player)] ?: return
        val use = scoreBoardPlayer.useBoard ?: return
        if(use == this){
            show(scoreBoardPlayer)
        }
    }

    fun show(scoreBoardPlayer: ScoreBoardPlayer){
        val uuidPlayer = scoreBoardPlayer.uuidPlayer
        val player = uuidPlayer.player ?: return
        val board = plugin.server.scoreboardManager.newScoreboard
        val objective = board.registerNewObjective(uuidPlayer.toString().substring(0 until 16), "dummy")
        with(objective){
            displaySlot = DisplaySlot.SIDEBAR
            displayName = title.toColor
        }
        var count = 0
        list.forEach { (index, text) ->
            objective.getScore((text.get(player) + "&" + "%x".format(count)).toColor).score = index
            count ++
        }
        val scoreboard = objective.scoreboard
        if(scoreboard != null && player.scoreboard.entries != scoreboard.entries) player.scoreboard = scoreboard
    }
}
