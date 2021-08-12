package com.ifucolo.goalbygoal

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.ifucolo.goalbygoal.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


const val NUMBER_ATTEMPTS = 3
const val DISTANCE_FROM_BORDER_TO_FIRST_LINE = 180f
const val GOAL_SPACE_LINE = 20f
const val LEFT_X = -300
const val RIGHT_X = 300

@Composable
fun SoccerScreen() {
    val animateX = remember { Animatable(initialValue = 500f) }
    val animateY = remember { Animatable(initialValue = 1900f) }
    val animationScope = rememberCoroutineScope()

    var side: Side by remember { mutableStateOf(Side.Bottom) }
    var attempts: Int by remember { mutableStateOf(NUMBER_ATTEMPTS) }

    var scoreTop: Int by remember { mutableStateOf(0) }
    var scoreBottom: Int by remember { mutableStateOf(0) }
    var sizeCanva: Size? = null

    val onClickArrow: (Int) -> Unit = { x ->
        val targetX = animateX.value + x
        val targetY = animateY.value + (side.ySide * (sizeCanva!!.height/4))

        xAnimation(
            scope = animationScope,
            animatableX = animateX,
            animatableY = animateY,
            targetX = targetX,
            targetY = targetY,
            size = sizeCanva!!
        )

        attempts -=1
        checkAttemptsAndGoal(
            attempts = attempts,
            side = side,
            size = sizeCanva!!,
            animatableX = targetX,
            onAttemptChange = { attempts = it },
            onSideChange = { side = it},
            onScoreBottom = { scoreBottom++ },
            onScoreTop = { scoreTop++ }
        )
    }

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
            sizeCanva = size

            drawField(
                drawScope = this,
                animateX = animateX,
                animateY = animateY
            )
        }

        val (buttonLeft, buttonRight) = createRefs()

        FloatingActionButton(
            modifier = Modifier.padding(5.dp).constrainAs(buttonLeft) {
                bottom.linkTo(parent.bottom)
                this.start.linkTo(parent.start)
            },
            backgroundColor = Color.Black,
            onClick = { onClickArrow(LEFT_X) },
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Outlined.ArrowBack,
                contentDescription = "ArrowBack"
            )
        }

        FloatingActionButton(
            modifier = Modifier.padding(5.dp).constrainAs(buttonRight) {
                bottom.linkTo(parent.bottom)
                end.linkTo(parent.end)
            },
            backgroundColor = Color.Black,
            onClick = { onClickArrow(RIGHT_X) },
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Outlined.ArrowForward,
                contentDescription = "ArrowForward"
            )
        }

        Card(
            backgroundColor = Color.White
        ) {
            Text(
                modifier = Modifier.padding(8.dp),
                text = "$scoreTop X $scoreBottom",
                style = TextStyle(
                    color = Color.Black,
                    fontWeight = FontWeight(800),
                    fontSize = 20.sp
                )
            )
        }
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
    val x = when {
        targetX > size.width -> size.width
        targetX < 0f -> 0f
        else -> targetX
    }

    scope.launch {
        animatableX.animateTo(
            targetValue = x,
            animationSpec = tween(durationMillis = 1000)
        )
    }

    val y = when {
        targetY > size.height -> size.height - DISTANCE_FROM_BORDER_TO_FIRST_LINE
        targetY < 0f -> DISTANCE_FROM_BORDER_TO_FIRST_LINE
        else ->targetY
    }

    scope.launch {
        animatableY.animateTo(
            targetValue = y,
            animationSpec = tween(durationMillis = 1000)
        )
    }
}

fun checkAttemptsAndGoal(
    attempts: Int,
    side: Side,
    size: Size,
    animatableX: Float,

    onAttemptChange: (Int) -> Unit,
    onSideChange: (Side) -> Unit,
    onScoreTop: () -> Unit,
    onScoreBottom: () -> Unit,
) {
    if (attempts < 0) {
        if (side is Side.Bottom) {
            checkIfGoal(animatableX, size, onScore = {
                onScoreBottom.invoke()
            })

            onSideChange.invoke(Side.Top)
        } else {
            checkIfGoal(animatableX, size, onScore = {
                onScoreTop.invoke()
            })

            onSideChange.invoke(Side.Bottom)
        }
        onAttemptChange.invoke(3)
    }
}

fun checkIfGoal(
    xPos: Float,
    size: Size,
    onScore: () -> Unit
) {
    val xStart = size.width/3
    val xEnd = size.width - size.width/3

    if (xPos in xStart..xEnd) {
        onScore.invoke()
    }
}

fun drawField(
    drawScope: DrawScope,
    animateX: Animatable<Float, AnimationVector1D>,
    animateY: Animatable<Float, AnimationVector1D>,
) = drawScope.apply {
    TopArea(drawScope = this)
    BottomArea(drawScope = this)
    MainParts(drawScope = this)

    drawCircle(
        color = Color.Black,
        radius = 30f,
        style = Stroke(35f),
        center = Offset(
            x = animateX.value,
            y = animateY.value
        )
    )
}

fun MainParts(drawScope: DrawScope) {
    drawScope.apply {

        drawLine(
            color = Color.White,
            start = Offset(x = 0f , y = DISTANCE_FROM_BORDER_TO_FIRST_LINE),
            end = Offset(x = 0f , y = size.height - DISTANCE_FROM_BORDER_TO_FIRST_LINE),
            strokeWidth = 12.dp.toPx()
        )

        drawLine(
            color = Color.White,
            start = Offset(x = size.width , y = DISTANCE_FROM_BORDER_TO_FIRST_LINE),
            end = Offset(x = size.width, y = size.height - DISTANCE_FROM_BORDER_TO_FIRST_LINE),
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
            yStart = DISTANCE_FROM_BORDER_TO_FIRST_LINE,
            yEnd = size.height/7,
            yHorizontal = size.height/7
        )

        val bigArea = BigArea(
            xStart = size.width/6,
            xEnd = size.width - size.width/6,
            yStart = DISTANCE_FROM_BORDER_TO_FIRST_LINE,
            yEnd = size.height/5,
            yHorizontal = size.height/5,
            yFieldLine = DISTANCE_FROM_BORDER_TO_FIRST_LINE
        )

        val goal = Goal(
            xStart = size.width/3,
            xEnd = size.width - size.width/3,
            yStart = 0f,
            yEnd = DISTANCE_FROM_BORDER_TO_FIRST_LINE
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
            yEnd = size.height - DISTANCE_FROM_BORDER_TO_FIRST_LINE,
            yHorizontal = size.height- size.height/7
        )

        val bigArea = BigArea(
            xStart = size.width/6,
            xEnd = size.width - size.width/6,
            yStart = size.height- size.height/5,
            yEnd = size.height - DISTANCE_FROM_BORDER_TO_FIRST_LINE,
            yHorizontal = size.height- size.height/5,
            yFieldLine = size.height - DISTANCE_FROM_BORDER_TO_FIRST_LINE
        )

        val goal = Goal(
            xStart = size.width/3,
            xEnd = size.width - size.width/3,
            yStart = size.height - DISTANCE_FROM_BORDER_TO_FIRST_LINE,
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

        yStartPosToDraw+= GOAL_SPACE_LINE
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

        xLimitLine+= GOAL_SPACE_LINE
    }
}


@Preview("SoccerScreenPreview")
@Composable
fun SoccerScreenPreview() {
    SoccerScreen()
}