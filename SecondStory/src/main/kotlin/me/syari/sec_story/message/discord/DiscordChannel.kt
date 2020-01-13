package me.syari.sec_story.message.discord

enum class DiscordChannel(val raw: String){
    Global("global"),
    ServerLog("log"),
    Guid("guid"),
    Join("join")
}