package me.syari.sec_story.paper.core.tour

import me.syari.sec_story.paper.core.tour.action.TourAction

data class TourTask(val delay: Int, val list: List<TourAction>)