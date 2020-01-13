package me.syari.sec_story.paper.core.item

import me.syari.sec_story.paper.library.event.CustomEvent
import org.bukkit.OfflinePlayer

class GiveItemEvent(val offlinePlayer: OfflinePlayer, val ignore: Boolean): CustomEvent() {
    var isAddPost = false
}