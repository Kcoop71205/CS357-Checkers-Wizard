// Implemented by Kadin

package com.example.checkerscanvaslab

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class TutorialStep(
    val title: String,
    val description: String,
    val imageRes: Int
)

@Composable
fun TutorialScreen(onBackClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }

    val steps = listOf(
        TutorialStep(
            "Movement",
            "Pieces move diagonally forward to an empty adjacent dark square.",
            R.drawable.tutorial_1 // Replace with movement screenshot
        ),
        TutorialStep(
            "Capturing",
            "Jump over an opponent's piece to capture it. You can perform multiple jumps in one turn if available!",
            R.drawable.tutorial_2 // Replace with capturing screenshot
        ),
        TutorialStep(
            "Kings",
            "When a piece reaches the opposite end, it becomes a King. Kings can move and jump both forward and backward.",
            R.drawable.tutorial_3 // Replace with king screenshot
        ),
        TutorialStep(
            "Winning",
            "Capture all of your opponent's pieces to win the game!",
            R.drawable.tutorial_4 // Replace with winning screenshot
        )
    )

    val pagerState = rememberPagerState(pageCount = { steps.size })

    Box(modifier = Modifier.fillMaxSize()) {
        // Full screen background image
        Image(
            painter = painterResource(id = R.drawable.scrollbackground),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Box stacks the Text on top of the Image for Title
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.parchmentscroll),
                    contentDescription = "Title background",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
                Text(
                    text = "Tutorial",
                    fontSize = 45.sp,
                    fontFamily = FontFamily.Cursive,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                val step = steps[page]
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)
                ) {
                    Image(
                        painter = painterResource(id = step.imageRes),
                        contentDescription = step.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RectangleShape),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = step.title,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF23479F),
                        fontFamily = FontFamily.Cursive
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = step.description,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
            }

            // Pager Indicators (Circles)
            Row(
                Modifier
                    .height(30.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(steps.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration) Color(0xFF23479F) else Color.Gray.copy(alpha = 0.5f)
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Custom Back Button using parchment scroll image
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(240.dp)
                    .height(120.dp)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { onBackClick() }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.parchmentscroll),
                    contentDescription = "Button background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
                Text(
                    text = "Back to Home",
                    fontSize = 30.sp,
                    fontFamily = FontFamily.Cursive,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
