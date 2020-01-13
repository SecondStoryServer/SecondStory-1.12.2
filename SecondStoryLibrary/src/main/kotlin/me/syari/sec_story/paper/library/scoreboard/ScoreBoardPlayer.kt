package me.syari.sec_story.paper.library.scoreboard

import me.syari.sec_story.paper.library.player.UUIDPlayer
import org.bukkit.scoreboard.DisplaySlot

data class ScoreBoardPlayer(val uuidPlayer: UUIDPlayer){
    private val boardList = mutableSetOf<CustomScoreBoard>()

    var usePriority = ScoreBoardPriority.None
        private set

    var useBoard: CustomScoreBoard? = null
        private set

    private fun reloadUse(){
        val preUse = useBoard
        var highest: CustomScoreBoard? = null
        var priority = -1
        boardList.forEach { board ->
            val level = board.priority.level
            if(priority < level){
                highest = board
                priority = level
            }
        }
        if(preUse != highest){
            updateBoard(highest)
            useBoard = highest
            usePriority = highest?.priority ?: ScoreBoardPriority.None
        }
    }

    fun addBoard(board: CustomScoreBoard){
        boardList.add(board)
        reloadUse()
    }

    fun removeBoard(board: CustomScoreBoard): Int{
        boardList.remove(board)
        reloadUse()
        return boardList.size
    }

    fun updateBoard(board: CustomScoreBoard? = useBoard){
        board?.show(this) ?: uuidPlayer.player?.scoreboard?.clearSlot(DisplaySlot.SIDEBAR)
    }
}