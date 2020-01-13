package me.syari.sec_story.paper.core.hook

import me.DeeCaaD.CrackShotPlus.CSPapi
import me.syari.sec_story.paper.library.item.CustomItemStack

object CrackShotPlus {
    fun getItemFromCrackShotPlus(id: String) = CustomItemStack.fromNullable(CSPapi.getAttachmentItemStack(id))
}