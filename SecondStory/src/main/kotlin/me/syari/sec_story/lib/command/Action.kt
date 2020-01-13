package me.syari.sec_story.lib.command

data class Action(val hover: String? = null, val click: Pair<ClickType, String>? = null)