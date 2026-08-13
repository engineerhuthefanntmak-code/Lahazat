package com.floating.stopwatch.domain

data class Lap(
    val lapIndex: Int,
    val lapTimeMs: Long,          // duration of this specific lap
    val cumulativeTimeMs: Long,   // elapsed time from start up to this lap
    val diffFromPreviousMs: Long  // delta-from-previous lap
)
