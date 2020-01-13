package me.syari.sec_story.paper.library.event

import org.bukkit.event.Cancellable

open class CustomCancellableEvent: CustomEvent(), Cancellable {
    private var cancelled: Boolean = false

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }
}