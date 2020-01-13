package me.syari.sec_story.perm

import me.syari.sec_story.lib.event.CustomEvent
import me.syari.sec_story.server.CommandBlock
import me.syari.sec_story.server.CommandBlock.addAllowCmd
import me.syari.sec_story.server.CommandBlock.clearAllowCmd
import org.bukkit.entity.Player

class PermissionLoadEvent(val player: Player): CustomEvent(){
    private val permission = mutableListOf<String>()

    fun addPermission(collection: Collection<String>){
        permission.addAll(collection)
    }

    fun setAllowCommand(cause: CommandBlock.CommandAddCause, collection: Collection<String>){
        player.clearAllowCmd(cause)
        collection.forEach { c -> player.addAllowCmd(cause, c) }
    }

    fun getPermission() = permission.toList()
}