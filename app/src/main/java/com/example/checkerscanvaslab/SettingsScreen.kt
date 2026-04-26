// The dropdown menu currently does nothing - Nate

package com.example.checkerscanvaslab

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentDataType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun SettingsScreen(
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    onBackClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Full screen background image
    Image(
        painter = painterResource(id = R.drawable.scrollbackground),
        contentDescription = "Background",
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Box stacks the Text on top of the Image
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
                text = "Settings",
                fontSize = 45.sp,
                fontFamily = FontFamily.Cursive,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Volume",
            fontSize = 30.sp,
            fontFamily = FontFamily.Cursive,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Slider(
            value = volume,
            onValueChange = onVolumeChange,
            modifier = Modifier
                .width(300.dp)
                .padding(vertical = 8.dp),
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF23479F),
                activeTrackColor = Color(0xFF23479F),
                inactiveTrackColor = Color.Gray.copy(alpha = 0.5f)
            )
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Nate Adding Accessibility Settings
        var expanded by remember { mutableStateOf(false) }
        Box(
            modifier = Modifier.padding(16.dp)
        ) {
            IconButton(onClick = {expanded = !expanded}) {
                Icon(Icons.Default.MoreVert, contentDescription = "Select Palette")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false}
            ) {
                DropdownMenuItem(
                    text = {Text("Classic")},
                    onClick = { GameSettings.setPalette("classic") }
                )
                DropdownMenuItem(
                    text = {Text("Original")},
                    onClick = {GameSettings.setPalette("original")}
                )
                DropdownMenuItem(
                    text = {Text("Accessible")},
                    onClick = {GameSettings.setPalette("accessible")}
                )
            }
        }

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
    }
}