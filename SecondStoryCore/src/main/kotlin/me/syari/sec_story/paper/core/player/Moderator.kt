package me.syari.sec_story.paper.core.player

import me.syari.sec_story.paper.core.Main.Companion.plugin
import me.syari.sec_story.paper.library.config.CreateConfig.config

object Moderator {
    fun loadModerator() {
        config(plugin, "moderator.yml") {

        }
    }
}