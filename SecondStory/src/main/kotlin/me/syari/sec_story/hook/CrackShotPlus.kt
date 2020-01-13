package me.syari.sec_story.hook

import me.DeeCaaD.CrackShotPlus.CSPapi
import me.syari.sec_story.lib.CustomItemStack

object CrackShotPlus {
    fun getItemFromCrackShotPlus(id: String, amount: Int) = CustomItemStack.fromNullable(CSPapi.getAttachmentItemStack(id), amount)
}