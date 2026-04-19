package com.example.checkerscanvaslab
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(onPlayClick: () -> Unit) {

    Image(
        painter = painterResource(id = R.drawable.scrollbackground),
        contentDescription = "Background",
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(id = R.drawable.thewizard),
            contentDescription = "THE Wizard",
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 350.dp)
        )

        Text(
            text = "Welcome",
            fontSize = 40.sp,
            fontFamily = FontFamily.Cursive,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "to Checkers Wizard!",
            fontSize = 40.sp,
            fontFamily = FontFamily.Cursive,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        val buttonModifier = Modifier
            .width(200.dp)
            .height(60.dp)
        
        val customButtonColor = Color(0xFF23479F)

        Button(
            onClick = { onPlayClick() },
            shape = RectangleShape,
            modifier = buttonModifier,
            colors = ButtonDefaults.buttonColors(containerColor = customButtonColor)
        ) {
            Text(
                text = "Play",
                fontSize = 24.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onPlayClick() },
            shape = RectangleShape,
            modifier = buttonModifier,
            colors = ButtonDefaults.buttonColors(containerColor = customButtonColor)
        ) {
            Text(
                text = "Tutorial",
                fontSize = 24.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onPlayClick() },
            shape = RectangleShape,
            modifier = buttonModifier,
            colors = ButtonDefaults.buttonColors(containerColor = customButtonColor)
        ) {
            Text(
                text = "Settings",
                fontSize = 24.sp
            )
        }
    }
}
