package me.syari.sec_story.paper.library.command

import org.bukkit.command.CommandSender

data class CommandTab(val arg: List<String>, val tab: (CommandSender) -> CommandTabElement?)