package me.syari.sec_story.paper.core.guild.altar.buff.data

class BuffData {
    private val levelList = mutableMapOf<Int, BuffLevel>()

    var lastLevel = 0
        private set

    fun addLevel(lv: Int, needGP: Int, value: Int): Int {
        val next = lastLevel + 1
        return if(next == lv) {
            levelList[lv] = BuffLevel(needGP, value)
            - 1
        } else {
            next
        }
    }

    fun getLevel(lv: Int) = levelList[lv]
}