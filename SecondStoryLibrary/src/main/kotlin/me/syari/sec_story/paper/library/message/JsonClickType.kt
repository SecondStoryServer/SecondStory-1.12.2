package me.syari.sec_story.paper.library.message

import net.md_5.bungee.api.chat.ClickEvent

enum class JsonClickType(val event: ClickEvent.Action) {
    RunCommand(ClickEvent.Action.RUN_COMMAND),
    TypeText(ClickEvent.Action.SUGGEST_COMMAND),
    OpenURL(ClickEvent.Action.OPEN_URL)
}