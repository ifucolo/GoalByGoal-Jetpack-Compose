package com.ifucolo.goalbygoal

import android.graphics.drawable.Icon
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.ifucolo.goalbygoal.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

sealed class Side(open var ySide: Float) {
    object Top: Side(1f)
    object Bottom: Side(-1f)
}


@Composable
fun SoccerScreen() {

    val animatableX = remember { Animatable(initialValue = 500f) }
    val animatableY = remember { Animatable(initialValue = 1900f) }
    val animationScope = rememberCoroutineScope()

    var side: Side by remember { mutableStateOf(Side.Bottom) }
    var attempts: Int by remember { mutableStateOf(3) }

    var sizeCanva: Size? = null

    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxSize()
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize()
                .background(Color.Green)
                //.clickable { onClick.invoke() },
        ) {
            TopArea(drawScope = this)
            BottomArea(drawScope = this)
            MainParts(drawScope = this)

            sizeCanva = size

            drawCircle(
                color = Color.Black,
                radius = 40f,
                center = Offset(
                    x = animatableX.value,
                    y = animatableY.value
                )
            )
        }

        val (buttonLeft, buttonRight) = createRefs()

        FloatingActionButton(
            modifier = Modifier.constrainAs(buttonLeft) {
                bottom.linkTo(parent.bottom)
                this.start.linkTo(parent.start)
            },
            onClick = {
                val targetX = animatableX.value - 300
                val targetY = animatableY.value + (side.ySide * (sizeCanva!!.height/4))
                xAnimation(
                    animationScope,
                    animatableX,
                    animatableY,
                    targetX,
                    targetY,
                    sizeCanva!!
                )
                attempts -=1
                checkAttempts(attempts, side, onAttemptChange = { attempts = it }, onSideChange = { side = it})
            },
            contentColor = Color.White
        ) {
            Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = "asdas")
        }

        FloatingActionButton(
            modifier = Modifier.constrainAs(buttonRight) {
                bottom.linkTo(parent.bottom)
                end.linkTo(parent.end)
            },
            onClick = {
                val targetX = animatableX.value + 300
                val targetY = animatableY.value + (side.ySide * (sizeCanva!!.height/4))
                xAnimation(
                    animationScope,
                    animatableX,
                    animatableY,
                    targetX,
                    targetY,
                    sizeCanva!!
                )
                attempts -=1
                checkAttempts(attempts, side, onAttemptChange = { attempts = it }, onSideChange = { side = it})
            },
            contentColor = Color.White
        ) {
            Icon(imageVector = Icons.Outlined.ArrowForward, contentDescription = "asdas")
        }

        //start = false
    }
}

fun xAnimation(
    scope: CoroutineScope,
    animatableX: Animatable<Float, AnimationVector1D>,
    animatableY: Animatable<Float, AnimationVector1D>,
    targetX: Float,
    targetY: Float,
    size: Size
) {
    val x = if (targetX > size.width) size.width else if (targetX < 0f) 0f else targetX

    scope.launch {
        animatableX.animateTo(
            targetValue = x,
            animationSpec = tween(durationMillis = 1000)
        )
    }

    println("xAnimation targetY: $targetY")
    val y = if (targetY > size.height) size.height - 180f else if (targetY < 0f) 180f else targetY
    println("xAnimation y: $y")

    scope.launch {
        animatableY.animateTo(
            targetValue = y,
            animationSpec = tween(durationMillis = 1000)
        )
    }
}

fun checkAttempts(
    attempts: Int,
    side: Side,
    onAttemptChange: (Int) -> Unit,
    onSideChange: (Side) -> Unit
) {
    println("checkAttempts att: $attempts")
    if (attempts < 0) {
        println("checkAttempts cr side $side")
        if (side is Side.Bottom) {
            onSideChange.invoke(Side.Top)
        } else {
            onSideChange.invoke(Side.Bottom)
        }
        onAttemptChange.invoke(3)
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

/*
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
 */