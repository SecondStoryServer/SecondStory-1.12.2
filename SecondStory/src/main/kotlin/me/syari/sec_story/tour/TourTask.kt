package me.syari.sec_story.tour

import me.syari.sec_story.tour.action.TourAction

data class TourTask(val delay: Int, val list: List<TourAction>)