package me.syari.sec_story.paper.core.vote

import com.vexsoftware.votifier.model.VotifierEvent
import me.syari.sec_story.paper.core.vote.Vote.vote
import me.syari.sec_story.paper.library.init.EventInit
import org.bukkit.event.EventHandler

object VoteEvent: EventInit {
    @EventHandler
    fun on(e: VotifierEvent) {
        val user = e.vote.username
        vote(user)
    }
}