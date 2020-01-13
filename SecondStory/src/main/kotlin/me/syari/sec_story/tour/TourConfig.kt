package me.syari.sec_story.tour

import me.syari.sec_story.lib.CustomItemStack
import me.syari.sec_story.lib.StringEditor.toColor
import me.syari.sec_story.lib.config.CreateConfig.configDir
import me.syari.sec_story.plugin.Plugin.plugin
import me.syari.sec_story.tour.action.*
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.command.CommandSender

object TourConfig {
    val tours = mutableListOf<TourData>()

    fun CommandSender.loadHelp(){
        tours.clear()
        configDir("Tour", false){
            output = this@loadHelp

            getSection("")?.forEach { id ->
                var delay = 0
                val list = mutableListOf<TourAction>()
                val task = mutableListOf<TourTask>()
                val hide = getBoolean("$id.hide", false)
                val npc = getString("$id.npc", false)?.toColor
                val ticketName = getString("$id.ticket", false)
                val ticket = if(ticketName != null) {
                    val item = CustomItemStack(Material.PAPER, ticketName, getStringList("$id.desc", listOf(), false))
                    item.setShine()
                    item.editNBTTag {
                        setBoolean("ssItem", true)
                        setString("ssItemType", "TourTicket")
                        setString("TourTicketID", id)
                    }
                    item
                } else {
                    null
                }
                getStringList("$id.list")?.forEach next@ { s ->
                    val t = s.split("\\s+".toRegex())
                    when(t[0].toLowerCase()){
                        "delay" -> {
                            val sec = t.getOrNull(1)?.toIntOrNull()
                            if(sec != null){
                                if(list.isNotEmpty()){
                                    task.add(TourTask(delay, list.toList()))
                                    list.clear()
                                }
                                delay += sec
                            } else {
                                send("&cHelp $id delay - sec is null")
                            }
                        }
                        "echo" -> {
                            val msg = t.joinToString(" ").substring(t[0].length + 1)
                            list.add(TourEchoMessage(msg))
                        }
                        "title" -> {
                            if(t.size == 6){
                                val main = t[1]
                                val sub = t[2]
                                val fadeIn = t[3].toIntOrNull()
                                val stay = t[4].toIntOrNull()
                                val fadeOut = t[5].toIntOrNull()
                                if(fadeIn != null && stay != null && fadeOut != null){
                                    list.add(TourTitleMessage(main.replace("_", " "), sub.replace("_", " "), fadeIn, stay, fadeOut))
                                } else {
                                    send("&cHelp $id title - sec is null")
                                }
                            } else {
                                send("&cHelp $id title - format error")
                            }
                        }
                        "action" -> {
                            val msg = t.joinToString(" ").substring(t[0].length + 1)
                            list.add(TourActionMessage(msg))
                        }
                        "cmd" -> {
                            val cmd = t.joinToString(" ").substring(t[0].length + 1)
                            list.add(TourRunCommand(cmd))
                        }
                        "move" -> {
                            val bool = when(t.getOrNull(1)?.toLowerCase()){
                                "allow" -> false
                                "deny" -> true
                                else -> return@next send("&cHelp $id move - allow/deny option")
                            }
                            list.add(TourCancelMove(bool))
                        }
                        "tp" -> {
                            if(t.size == 7){
                                val w = plugin.server.getWorld(t[1])
                                val x = t[2].toDoubleOrNull()
                                val y = t[3].toDoubleOrNull()
                                val z = t[4].toDoubleOrNull()
                                val yaw = t[5].toFloatOrNull()
                                val pitch = t[6].toFloatOrNull()
                                if(w != null && x != null && y != null && z != null && yaw != null && pitch != null){
                                    list.add(TourTeleport(Location(w, x, y, z, yaw, pitch)))
                                } else {
                                    send("&cHelp $id teleport - location null")
                                }
                            } else {
                                send("&cHelp $id teleport - format error")
                            }
                        }
                        "data" -> {
                            val bool = when(t.getOrNull(1)?.toLowerCase()){
                                "load" -> false
                                "save" -> true
                                else -> return@next send("&cHelp $id data - load/save option")
                            }
                            if(bool){
                                when(t.getOrNull(2)?.toLowerCase()){
                                    "loc" -> list.add(TourSaveLocation())
                                    else -> return@next send("&cHelp $id data save - loc option")
                                }
                            } else {
                                list.add(TourLoadData())
                            }
                        }
                        "jump" -> {
                            val to = t.getOrNull(1)
                            if(to != null){
                                if(t.size != 2){
                                    val msg = t.joinToString(" ").substring(t[0].length + t[1].length + 2)
                                    list.add(TourJump(to, if(msg.contains('[')) msg.substringBefore('[') else "", msg.substringAfter('[').substringBefore(']'), if(msg.contains(']')) msg.substringAfter(']') else ""))
                                } else {
                                    send("&cHelp $id jump - message null")
                                }
                            } else {
                                send("&cHelp $id jump - to id null")
                            }
                        }
                        "run" -> {
                            val to = t.getOrNull(1)
                            if(to != null){
                                list.add(TourRun(to))
                            } else {
                                send("&cHelp $id run - to id null")
                            }
                        }
                        "use" -> {
                            if(t.getOrNull(1)?.toLowerCase() == "ticket"){
                                list.add(TourUseTicket())
                            } else {
                                send("&cHelp $id use - ticket option")
                            }
                        }
                    }
                }
                if(list.isNotEmpty()){
                    task.add(TourTask(delay, list.toList()))
                    list.clear()
                }
                if(task.isNotEmpty()){
                    tours.add(TourData(id, hide, npc, ticket, task))
                }
            }
        }
    }
}