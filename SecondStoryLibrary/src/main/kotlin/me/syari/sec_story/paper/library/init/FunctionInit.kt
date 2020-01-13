package me.syari.sec_story.paper.library.init

interface FunctionInit {
    companion object {
        fun register(vararg init: FunctionInit) {
            init.forEach {
                it.init()
            }
        }
    }

    fun init()
}