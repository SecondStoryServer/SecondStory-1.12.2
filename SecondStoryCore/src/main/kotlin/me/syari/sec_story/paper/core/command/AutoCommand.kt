package me.syari.sec_story.paper.core.command

import me.syari.sec_story.paper.library.command.RunCommand

object AutoCommand {
    private val auto = mutableMapOf<Pair<Int, String>, Set<String>>()

    fun add(day: Int, time: String, set: Set<String>) {
        auto[day to time] = set
    }

    fun clear() {
        auto.clear()
    }

    fun run(day: Int, time: String) {
        auto[day to time]?.forEach { f ->
            RunCommand.runCommandFromConsole(f)
        }
        auto[0 to time]?.forEach { f ->
            RunCommand.runCommandFromConsole(f)
        }
    }
}