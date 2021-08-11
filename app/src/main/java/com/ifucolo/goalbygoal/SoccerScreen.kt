package com.ifucolo.goalbygoal

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ifucolo.goalbygoal.ui.theme.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

sealed class Side {
    object Top: Side()
    object Bottom: Side()
}
sealed class AnimationState {
    object RUNNING: AnimationState()
    object ENABLE: AnimationState()
}

@Composable
fun SoccerScreen() {

    val animatableX = remember { Animatable(initialValue = 0f) }
    val animatableY = remember { Animatable(initialValue = 0f) }
    val animationScope = rememberCoroutineScope()
    var animationState = AnimationState.ENABLE as AnimationState
    var start = true

    var maxX = remember { 0 }
    var maxY = remember { 0 }
    var side: Side = Side.Top

    var xStart = 0f
    var xEnd = 0f

    var goal = remember { 0 }

    val onClick: () -> Unit = {
        animationScope.launch {
            // Start the animations without blocking each other
            // On each click x and y values will be created randomly
            start = false
            animationState = if (animatableY.value.toInt() == 0 || animatableY.value.toInt() == maxY) {
                AnimationState.ENABLE
            } else {
                AnimationState.RUNNING
            }

            if (animationState == AnimationState.ENABLE) {
                animationState = AnimationState.RUNNING
                launch {
                    animatableX.animateTo(
                        targetValue = (0..maxX)
                            .random()
                            .toFloat(),
                        animationSpec = tween(durationMillis = 1000)
                    )

                    if (animatableX.value in xStart..xEnd) {
                        goal++
                        println("goal = $goal")
                    }

                }

                launch {
                    var randomY = 0f

                    when(side) {
                        Side.Bottom -> {
                            randomY = 0f
                            side = Side.Top
                        }
                        Side.Top -> {
                            randomY = maxY.toFloat()
                            side = Side.Bottom
                        }
                    }

                    animatableY.animateTo(
                        targetValue = randomY,
                        animationSpec = tween(durationMillis = 1000)
                    )
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxSize()
    ) {


        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize()
                .background(Color.Green)
                .clickable { onClick.invoke() },
        ) {
            TopArea(drawScope = this)
            BottomArea(drawScope = this)
            MainParts(drawScope = this)

            xStart = size.width/3
            xEnd = size.width - size.width/3

            maxX = size.width.toInt()
            maxY = size.height.toInt()

            drawCircle(
                color = Color.Black,
                radius = 40f,
                center = Offset(
                    x = if (start) (size.width/2).toFloat() else animatableX.value,
                    y = if (start) (size.height/9.5).toFloat() else animatableY.value
                )
            )
        }

    }

}

fun MainParts(drawScope: DrawScope) {
    drawScope.apply {

        drawLine(
            color = Color.White,
            start = Offset(x = 0f , y = 180f),
            end = Offset(x = 0f , y = size.height - 180f ),
            strokeWidth = 12.dp.toPx()
        )

        drawLine(
            color = Color.White,
            start = Offset(x = size.width , y = 180f),
            end = Offset(x = size.width, y = size.height - 180f ),
            strokeWidth = 12.dp.toPx()
        )
        //Middle line
        drawLine(
            color = Color.White,
            start = Offset(x = 0f , y = size.height /2),
            end = Offset(x = size.width , y = size.height/2 ),
            strokeWidth = 6.dp.toPx()
        )

        //Big Circle
        drawCircle(
            color = Color.White,
            style = Stroke(
                width = 6.dp.toPx(),
            ),
            center = Offset(x = size.width / 2, y = size.height / 2),
            radius = size.minDimension / 5
        )

        //Small circle
        drawCircle(
            color = Color.White,
            center = Offset(x = size.width / 2, y = size.height / 2),
            radius = 40f
        )
    }
}

fun TopArea(drawScope: DrawScope) {
    drawScope.apply {
        val smallArea = SmallArea(
            xStart = size.width/3,
            xEnd = size.width - size.width/3,
            yStart = 180f,
            yEnd = size.height/7,
            yHorizontal = size.height/7
        )

        val bigArea = BigArea(
            xStart = size.width/6,
            xEnd = size.width - size.width/6,
            yStart = 180f,
            yEnd = size.height/5,
            yHorizontal = size.height/5,
            yFieldLine = 180f
        )

        val goal = Goal(
            xStart = size.width/3,
            xEnd = size.width - size.width/3,
            yStart = 0f,
            yEnd = 180f
        )

        val penaltyMark = PenaltyMark(
            x = size.width/2,
            y = (size.height/6.5).toFloat()
        )

        val fieldArea = FieldArea(
            smallArea = smallArea,
            bigArea = bigArea,
            goal = goal,
            penaltyMark = penaltyMark,
            size = size
        )

        drawArea(
            drawScope = this,
            fieldArea = fieldArea
        )
    }
}

fun BottomArea(drawScope: DrawScope) {
    drawScope.apply {
        val smallArea = SmallArea(
            xStart = size.width/3,
            xEnd = size.width - size.width/3,
            yStart = size.height - size.height/7,
            yEnd = size.height -180f,
            yHorizontal = size.height- size.height/7
        )

        val bigArea = BigArea(
            xStart = size.width/6,
            xEnd = size.width - size.width/6,
            yStart = size.height- size.height/5,
            yEnd = size.height - 180f,
            yHorizontal = size.height- size.height/5,
            yFieldLine = size.height - 180f
        )

        val goal = Goal(
            xStart = size.width/3,
            xEnd = size.width - size.width/3,
            yStart = size.height - 180f,
            yEnd = size.height,
        )

        val penaltyMark = PenaltyMark(
            x = size.width/2,
            y = size.height- (size.height/6).toFloat()
        )

        val fieldArea = FieldArea(
            smallArea = smallArea,
            bigArea = bigArea,
            goal = goal,
            penaltyMark = penaltyMark,
            size = size
        )

        drawArea(
            drawScope = this,
            fieldArea = fieldArea
        )
    }
}

fun drawArea(
    drawScope: DrawScope,
    fieldArea: FieldArea
) = drawScope.apply {
    drawGoal(
        drawScope = this,
        goal = fieldArea.goal
    )

    drawBigPenaltyArea(
        drawScope = this,
        bigArea = fieldArea.bigArea
    )

    drawSmallArea(
        drawScope = this,
        smallArea = fieldArea.smallArea
    )

    drawCircle(
        color = Color.White,
        center = Offset(x = fieldArea.penaltyMark.x  , y = fieldArea.penaltyMark.y),
        radius = 10f
    )
}

fun drawSmallArea(
    drawScope: DrawScope,
    smallArea: SmallArea
) = drawScope.apply {

    //left vertical line
    drawLine(
        color = Color.White,
        start = Offset(x = smallArea.xStart, y = smallArea.yStart),
        end = Offset(x = smallArea.xStart, y = smallArea.yEnd),
        strokeWidth = 6.dp.toPx()
    )

    //right vertical line
    drawLine(
        color = Color.White,
        start = Offset(x = smallArea.xEnd, y = smallArea.yStart),
        end = Offset(x = smallArea.xEnd, y = smallArea.yEnd),
        strokeWidth = 6.dp.toPx()
    )

    //horizontal line
    drawLine(
        color = Color.White,
        start = Offset(x = smallArea.xStart, y = smallArea.yHorizontal),
        end = Offset(x = smallArea.xEnd, y = smallArea.yHorizontal),
        strokeWidth = 6.dp.toPx()
    )
}
fun drawBigPenaltyArea(
    drawScope: DrawScope,
    bigArea: BigArea
) = drawScope.apply {
    //Line between goal and field
    drawLine(
        color = Color.White,
        start = Offset(x = 0f , y = bigArea.yFieldLine),
        end = Offset(x = size.width , y = bigArea.yFieldLine),
        strokeWidth = 6.dp.toPx()
    )
    drawLine(
        color = Color.White,
        start = Offset(x = bigArea.xStart, y = bigArea.yStart),
        end = Offset(x = bigArea.xStart, y = bigArea.yEnd),
        strokeWidth = 6.dp.toPx()
    )

    //right vertical line
    drawLine(
        color = Color.White,
        start = Offset(x = bigArea.xEnd, y = bigArea.yStart),
        end = Offset(x = bigArea.xEnd, y = bigArea.yEnd),
        strokeWidth = 6.dp.toPx()
    )

    //horizontal line
    drawLine(
        color = Color.White,
        start = Offset(x = bigArea.xStart, y = bigArea.yHorizontal),
        end = Offset(x = bigArea.xEnd, y = bigArea.yHorizontal),
        strokeWidth = 6.dp.toPx()
    )
}
fun drawGoal(
    drawScope: DrawScope,
    goal: Goal
) = drawScope.apply {
    var yStartPosToDraw = goal.yStart
    //horizontal lines
    while (yStartPosToDraw < goal.yEnd) {
        //line goal
        drawLine(
            color = Color.Blue,
            start = Offset(x = goal.xStart, y = yStartPosToDraw),
            end = Offset(x = goal.xEnd, y = yStartPosToDraw),
            strokeWidth = 2.dp.toPx()
        )

        yStartPosToDraw+= 20f
    }

    var xLimitLine = goal.xStart
    //vertical lines
    while (xLimitLine <= goal.xEnd) {
        //line goal
        drawLine(
            color = Color.Blue,
            start = Offset(x = xLimitLine, y = goal.yStart),
            end = Offset(x = xLimitLine, y = goal.yEnd),
            strokeWidth = 2.dp.toPx()
        )

        xLimitLine+= 20f
    }
}


@Preview("SoccerScreenPreview")
@Composable
fun SoccerScreenPreview() {
    SoccerScreen()
}