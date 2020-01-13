package me.syari.sec_story.server

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.reflect.FieldAccessException
import me.syari.sec_story.lib.message.SendMessage.send
import me.syari.sec_story.lib.config.CreateConfig.config
import me.syari.sec_story.player.Donate.donateCmd
import me.syari.sec_story.plugin.Init
import me.syari.sec_story.plugin.Plugin.plugin
import me.syari.sec_story.rank.Ranks.rankCmd
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import java.lang.reflect.InvocationTargetException
import java.util.*

object CommandBlock : Init(), Listener {
    private fun Player.canAdd(label: String): Boolean{
        disallow[uniqueId]?.forEach { c ->
            return c.second != "*" || isIgnoreWildCmd(c.first, label)
        }
        return true
    }

    override fun init() {
        ProtocolLibrary.getProtocolManager().addPacketListener(
            object : PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Client.TAB_COMPLETE) {
                @EventHandler
                override fun onPacketReceiving(e: PacketEvent){
                    if (e.packetType === PacketType.Play.Client.TAB_COMPLETE) {
                        try {
                            val packet = e.packet
                            val message = (packet.getSpecificModifier(String::class.java).read(0) as String).toLowerCase()
                            val s = message.split(Regex("\\s+"))
                            val label = s[0].substring(1)
                            val p = e.player
                            if(p.isOp) return
                            if(s.size == 1){
                                val cmd = mutableListOf<String>()
                                allow[p.uniqueId]?.forEach { c ->
                                    if(!hideTab.contains(c.second) && p.canAdd(c.second) && c.second.startsWith(label)){
                                        cmd.add("/${c.second}")
                                    }
                                }
                                donateCmd().forEach { c ->
                                    if(!hideTab.contains(c) && p.canAdd(c) && c.startsWith(label)){
                                        cmd.add("/$c")
                                    }
                                }
                                val completions = PacketContainer(PacketType.Play.Server.TAB_COMPLETE)
                                completions.stringArrays.write(0, cmd.toTypedArray())
                                try {
                                    ProtocolLibrary.getProtocolManager().sendServerPacket(p, completions)
                                    e.isCancelled = true
                                } catch (e: InvocationTargetException) {
                                    e.printStackTrace()
                                }
                                return
                            } else if(p.isAllowCmd(label) && !p.isDisAllowCmd(label)) {
                                return
                            }
                            e.isCancelled = true
                        } catch (e: FieldAccessException) {}
                    }
                }
            }
        )
    }

    enum class CommandAddCause {
        Rank,
        Donate,
        GuildWar,
        RPG,
        Help,
        MobArena
    }

    private var hideTab = listOf<String>()

    fun CommandSender.loadCommand(){
        config("Command/hide.yml", false){
            output = this@loadCommand

            hideTab = getStringList("list", listOf())
        }
        config("Command/auto.yml", false) {
            output = this@loadCommand

            Commands.clear()
            getSection("")?.forEach { f ->
                Commands.add(f.substringBefore('-').toInt(), f.substringAfter('-'), getStringList(f, listOf()).toSet())
            }
        }
    }

    private val ignoreWild = mutableMapOf<UUID, List<Pair<CommandAddCause, String>>>()

    fun Player.addIgnoreWildCmd(cause: CommandAddCause, cmd: String){
        val list = ignoreWild.getOrDefault(uniqueId, listOf()).toMutableList()
        list.add(cause to cmd)
        ignoreWild[uniqueId] = list
    }

    fun Player.clearIgnoreWildCmd(cause: CommandAddCause){
        ignoreWild[uniqueId] = ignoreWild[uniqueId]?.filter { f -> f.first != cause } ?: return
    }

    private fun Player.isIgnoreWildCmd(cause: CommandAddCause, cmd: String): Boolean{
        ignoreWild[uniqueId]?.forEach { f ->
            if(f.first == cause && f.second == cmd) return true
        }
        return false
    }

    private val allow = mutableMapOf<UUID, List<Pair<CommandAddCause, String>>>()

    private fun Player.isAllowCmd(cmd: String): Boolean {
        allow[uniqueId]?.forEach { f ->
            if(f.second == cmd || (f.second == "*" && !isIgnoreWildCmd(f.first, cmd))) return true
        }
        return false
    }

    fun Player.addAllowCmd(cause: CommandAddCause, cmd: String){
        val list = allow.getOrDefault(uniqueId, listOf()).toMutableList()
        list.add(cause to cmd)
        allow[uniqueId] = list
    }

    fun Player.clearAllowCmd(cause: CommandAddCause){
        allow[uniqueId] = allow[uniqueId]?.filter { f -> f.first != cause } ?: return
    }

    private val disallow = mutableMapOf<UUID, List<Pair<CommandAddCause, String>>>()

    private fun Player.isDisAllowCmd(cmd: String): Boolean {
        disallow[uniqueId]?.forEach { f ->
            if(f.second == cmd || (f.second == "*" && !isIgnoreWildCmd(f.first, cmd))) return true
        }
        return false
    }

    fun Player.addDisAllowCmd(cause: CommandAddCause, cmd: String){
        val list = disallow.getOrDefault(uniqueId, listOf()).toMutableList()
        list.add(cause to cmd)
        disallow[uniqueId] = list
    }

    fun Player.clearDisAllowCmd(cause: CommandAddCause){
        disallow[uniqueId] = disallow[uniqueId]?.filter { f -> f.first != cause } ?: return
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: PlayerCommandPreprocessEvent){
        val p = e.player
        val label = e.message.split(Regex("\\s+"))[0].substring(1) /* .substringAfter(':') */
        if(p.isDisAllowCmd(label)){
            p.send("&b[Command] &f実行できないコマンドです")
            e.isCancelled = true
        } else if (!p.isOp && !p.isAllowCmd(label)) {
            when(label){
                in donateCmd() -> p.send("""
                        &b[Command] &f寄付者限定のコマンドです
                        &f寄付について : &ahttps://web.2nd-story.info/pages/3123115/donate
                    """.trimIndent())
                in rankCmd -> p.send("&b[Command] &f今のランクだと使えないコマンドです &a/help")
                else -> p.send("&b[Command] &f存在しないコマンドです &a/help")
            }
            e.isCancelled = true
        }
    }
}