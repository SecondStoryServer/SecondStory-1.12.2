package me.syari.sec_story.hook

import com.boydti.fawe.FaweAPI
import org.bukkit.entity.Player

object FastAsyncWorldEdit {
    val Player.fawePlayer get() = FaweAPI.wrapPlayer(this)
}