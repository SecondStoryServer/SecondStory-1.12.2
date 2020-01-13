package me.syari.sec_story.paper.library.message

data class JsonAction(val hover: String? = null, val click: Pair<JsonClickType, String>? = null)