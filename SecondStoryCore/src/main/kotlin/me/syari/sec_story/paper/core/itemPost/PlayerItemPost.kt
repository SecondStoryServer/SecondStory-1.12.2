package me.syari.sec_story.paper.core.itemPost

import me.syari.sec_story.paper.core.item.GiveItem.give
import me.syari.sec_story.paper.core.plugin.SQL.sql
import me.syari.sec_story.paper.library.display.CreateBossBar.createBossBar
import me.syari.sec_story.paper.library.item.CustomItemStack
import me.syari.sec_story.paper.library.message.SendMessage.send
import me.syari.sec_story.paper.library.player.UUIDPlayer
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.Player

data class PlayerItemPost(val uuidPlayer: UUIDPlayer) {
    companion object {
        private val bar = createBossBar("&f&lポストにアイテムが届いています &a&l/post &f&lで受け取りましょう", BarColor.GREEN, BarStyle.SOLID)
    }

    var element = mutableMapOf<ItemPostData, ItemPostElement>()

    var isLoaded = false

    private fun MutableMap<ItemPostData, ItemPostElement>.addElement(data: ItemPostData, item: CustomItemStack) {
        getOrPut(data) { ItemPostElement() }.add(item)
    }

    fun loadPost(reload: Boolean): PlayerItemPost {
        val element = mutableMapOf<ItemPostData, ItemPostElement>()
        sql {
            val res = executeQuery(
                "SELECT PostName, ReceiveDate, LimitDate, ItemStack FROM Story.ItemPost WHERE UUID = '$uuidPlayer';"
            )
            while(res.next()) {
                val json = res.getString("ItemStack")
                val item = CustomItemStack.fromJson(json)
                val name = res.getString("PostName")
                val receive = res.getDate("ReceiveDate")
                val limit = res.getDate("LimitDate")
                element.addElement(ItemPostData(name, receive, limit), item)
            }
        }
        this.element = element
        if(reload) reloadPost()
        return this
    }

    private fun reloadPost() {
        val player = uuidPlayer.player ?: return
        if(element.isEmpty()) {
            bar.removePlayer(player)
        } else {
            bar.addPlayer(player)
        }
    }

    fun addPost(data: ItemPostData, items: Collection<CustomItemStack>) {
        if(! isLoaded) loadPost(false)
        sql {
            items.forEach {
                if(it.isAir) return@forEach
                element.addElement(data, it)
                executeUpdate(
                    "INSERT Story.ItemPost VALUE ('$uuidPlayer', '${data.name}', '${data.receive}', '${data.limit}', '${it.toJson()}')"
                )
            }
        }
        reloadPost()
    }

    fun deletePost(data: ItemPostData) {
        sql {
            executeUpdate(
                "DELETE FROM Story.ItemPost WHERE UUID = '$uuidPlayer' AND PostName = '${data.name}' AND ReceiveDate = '${data.receive}' AND LimitDate = '${data.limit}'"
            )
        }
        element.remove(data)
        reloadPost()
    }

    fun givePost(data: ItemPostData, receiver: Player?, delete: Boolean) {
        val player = receiver ?: uuidPlayer.player ?: return
        val e = element[data] ?: return
        player.give(e.items)
        player.send("&b[Post] &fアイテムを受け取りました")
        if(delete) {
            deletePost(data)
        }
    }
}