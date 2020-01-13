package me.syari.sec_story.paper.core.perm

import me.syari.sec_story.paper.core.command.CommandCancel
import me.syari.sec_story.paper.core.command.CommandCancel.addAllowCmd
import me.syari.sec_story.paper.core.command.CommandCancel.clearAllowCmd
import me.syari.sec_story.paper.library.event.CustomEvent
import org.bukkit.entity.Player

class PermissionLoadEvent(val player: Player): CustomEvent() {
    private val permission = mutableListOf<String>()

    fun addPermission(collection: Collection<String>) {
        permission.addAll(collection)
    }

    fun setAllowCommand(cause: CommandCancel.CommandAddCause, collection: Collection<String>) {
        player.clearAllowCmd(cause)
        collection.forEach { c -> player.addAllowCmd(cause, c) }
    }

    fun getPermission() = permission.toList()
}