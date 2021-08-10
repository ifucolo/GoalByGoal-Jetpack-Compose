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
        val borderPath = Path()

        borderPath.apply {
            // border
            //horizontal lines
            lineTo(size.width, 0f)
            lineTo(size.width, size.height)
            //vertical lines
            lineTo(0f, size.height)
            lineTo(0f, 0f)
        }

        //Rectangle around
        drawPath(
            path = borderPath,
            color = Color.White,
            style = Stroke(
                width = 15.dp.toPx(),
            )
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

        //Big area
        //Top horiontal Area line
        drawLine(
            color = Color.White,
            start = Offset(x = size.width/6  , y = size.height /7),
            end = Offset(x = size.width - size.width/6 , y = size.height/7 ),
            strokeWidth = 6.dp.toPx()
        )

        //left vertical line
        drawLine(
            color = Color.White,
            start = Offset(x = size.width/6  , y = 0f),
            end = Offset(x = size.width/6 , y = size.height/7 ),
            strokeWidth = 6.dp.toPx()
        )

        //right vertical line
        drawLine(
            color = Color.White,
            start = Offset(x = size.width - size.width/6, y = 0f),
            end = Offset(x = size.width - size.width/6, y = size.height/7 ),
            strokeWidth = 6.dp.toPx()
        )

        //Small area
        drawLine(
            color = Color.White,
            start = Offset(x = size.width/3  , y = size.height/16),
            end = Offset(x = size.width - size.width/3 , y = size.height/16),
            strokeWidth = 6.dp.toPx()
        )

        //left vertical line
        drawLine(
            color = Color.White,
            start = Offset(x = size.width/3  , y = 0f),
            end = Offset(x = size.width/3 , y = size.height/16 ),
            strokeWidth = 6.dp.toPx()
        )

        //right vertical line
        drawLine(
            color = Color.White,
            start = Offset(x = size.width - size.width/3, y = 0f),
            end = Offset(x = size.width - size.width/3, y = size.height/16 ),
            strokeWidth = 6.dp.toPx()
        )

        //semi circle

//        drawArc(
//            brush = SolidColor(Color.White),
//            size = Size(360f, 180f),
//            startAngle = 0f,
//            sweepAngle = 180f,
//            useCenter = true,
//            style = Stroke(12f, cap = StrokeCap.Round),
//            topLeft = Offset(x = (size.width/3).toFloat()  , y = (size.height/9.5).toFloat())
//        )

        //penalty mark
        drawCircle(
            color = Color.White,
            center = Offset(x = (size.width/2).toFloat()  , y = (size.height/9.5).toFloat()),
            radius = 10f
        )
    }
}

fun BottomArea(drawScope: DrawScope) {
    drawScope.apply {
        //Top horizontal Area line
        drawLine(
            color = Color.White,
            start = Offset(x = size.width/6, y = size.height - size.height /7),
            end = Offset(x = size.width - size.width/6, y = size.height -  size.height/7 ),
            strokeWidth = 6.dp.toPx()
        )

        //left vertical line
        drawLine(
            color = Color.White,
            start = Offset(x = size.width/6, y = size.height- size.height /7),
            end = Offset(x = size.width/6, y = size.height ),
            strokeWidth = 6.dp.toPx()
        )

        //right vertical line
        drawLine(
            color = Color.White,
            start = Offset(x = size.width - size.width/6, y = size.height- size.height /7),
            end = Offset(x = size.width - size.width/6, y = size.height),
            strokeWidth = 6.dp.toPx()
        )

        //Small area
        drawLine(
            color = Color.White,
            start = Offset(x = size.width/3, y = size.height - size.height /17),
            end = Offset(x = size.width - size.width/3, y = size.height -  size.height/17 ),
            strokeWidth = 6.dp.toPx()
        )

        //left vertical line
        drawLine(
            color = Color.White,
            start = Offset(x = size.width/3, y = size.height- size.height /17),
            end = Offset(x = size.width/3, y = size.height ),
            strokeWidth = 6.dp.toPx()
        )

        //right vertical line
        drawLine(
            color = Color.White,
            start = Offset(x = size.width - size.width/3, y = size.height- size.height /17),
            end = Offset(x = size.width - size.width/3, y = size.height),
            strokeWidth = 6.dp.toPx()
        )

        //semi circle
//        drawArc(
//            brush = SolidColor(Color.White),
//            size = Size(360f, 180f),
//            startAngle = 180f,
//            sweepAngle = 180f,
//            useCenter = true,
//            style = Stroke(12f, cap = StrokeCap.Round),
//            topLeft = Offset(x = (size.width/3).toFloat(), y = size.height- (size.height/5.5).toFloat())
//        )

        //penalty mark
        drawCircle(
            color = Color.White,
            center = Offset(x = (size.width/2).toFloat(), y = size.height- (size.height/10).toFloat()),
            radius = 10f
        )

    }
}

@Preview("SoccerScreenPreview")
@Composable
fun SoccerScreenPreview() {
    SoccerScreen()
}