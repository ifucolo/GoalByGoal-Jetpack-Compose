package com.ifucolo.goalbygoal.ui.theme

import androidx.compose.ui.geometry.Size


class FieldArea(
    val smallArea: SmallArea,
    val bigArea: BigArea,
    val goal: Goal,
    val penaltyMark: PenaltyMark,
    val size: Size
)

data class SmallArea(
    val xStart: Float,
    val xEnd: Float,
    val yStart: Float,
    val yEnd: Float,
    val yHorizontal: Float
)
data class BigArea(
    val xStart: Float,
    val xEnd: Float,
    val yStart: Float,
    val yEnd: Float,
    val yHorizontal: Float,
    val yFieldLine: Float
)

data class Goal(
    val xStart: Float,
    val xEnd: Float,
    val yStart: Float,
    val yEnd: Float
)

data class PenaltyMark(
    val x: Float,
    val y: Float
)