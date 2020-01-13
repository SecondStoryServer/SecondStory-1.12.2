package me.syari.sec_story.hook

import net.coreprotect.CoreProtect
import org.bukkit.block.Block

object CoreProtect {
    private val hook = CoreProtect.getInstance().api

    val Block.isNatural get() = hook.blockLookup(this, 60 * 60 * 24 * 7).isEmpty()
}