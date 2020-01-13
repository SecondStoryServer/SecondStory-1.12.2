package me.syari.sec_story.paper.library.scheduler

interface CustomTask {
    val isCanceled: Boolean

    // val isAsync: Boolean

    val repeatRemain: Int

    fun cancel(): Boolean

    fun onEndRepeat(run: () -> Unit): CustomTask?
}