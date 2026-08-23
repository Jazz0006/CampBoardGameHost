package com.codex.campboardgamehost

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
internal fun ClocktowerLandingScreen(
    hasSavedGame: Boolean,
    onStartGame: () -> Unit,
    onContinueGame: () -> Unit,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    val brass = Color(0xFFC4A469)
    val warmWhite = Color(0xFFF3EFE5)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030911)),
    ) {
        Image(
            painter = painterResource(R.drawable.clocktower_launch_hero),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to Color(0xA6030911),
                            0.28f to Color(0x16030911),
                            0.58f to Color(0x32030911),
                            1f to Color(0xF5030911),
                        ),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(54.dp))
            Canvas(modifier = Modifier.size(42.dp)) {
                val stroke = 1.dp.toPx()
                drawCircle(
                    color = brass.copy(alpha = 0.75f),
                    radius = size.minDimension * 0.46f,
                    style = Stroke(width = stroke),
                )
                drawLine(
                    color = brass,
                    start = center,
                    end = Offset(center.x, center.y - size.height * 0.27f),
                    strokeWidth = stroke,
                )
                drawLine(
                    color = brass,
                    start = center,
                    end = Offset(center.x + size.width * 0.19f, center.y - size.height * 0.13f),
                    strokeWidth = stroke,
                )
                drawLine(
                    color = brass,
                    start = Offset(center.x, 0f),
                    end = Offset(center.x, size.height * 0.1f),
                    strokeWidth = stroke,
                )
                drawLine(
                    color = brass,
                    start = Offset(center.x, size.height * 0.9f),
                    end = Offset(center.x, size.height),
                    strokeWidth = stroke,
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = text("说书人专用控制台", "STORYTELLER CONSOLE"),
                color = brass,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.2.sp,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = text("血染钟楼说书人助手", "Clocktower Storyteller Assistant"),
                color = warmWhite,
                fontSize = 30.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = text(
                    "离线主持工具，解决说书的所有问题。",
                    "An offline host toolkit for every part of storytelling.",
                ),
                color = warmWhite.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onStartGame,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = brass,
                    contentColor = Color(0xFF101319),
                ),
            ) {
                Text(
                    text("开始游戏", "Start game"),
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                )
            }
            Spacer(modifier = Modifier.height(11.dp))
            OutlinedButton(
                onClick = onContinueGame,
                enabled = hasSavedGame,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(15.dp),
                border = BorderStroke(1.dp, brass.copy(alpha = if (hasSavedGame) 0.62f else 0.22f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = warmWhite.copy(alpha = 0.9f),
                    disabledContentColor = warmWhite.copy(alpha = 0.3f),
                    containerColor = Color(0xA8060D16),
                    disabledContainerColor = Color(0x78060D16),
                ),
            ) {
                Text(
                    text("继续上次游戏", "Continue last game"),
                    fontWeight = FontWeight.Bold,
                )
            }
            if (!hasSavedGame) {
                Text(
                    text = text("暂无进行中的游戏", "No game in progress"),
                    modifier = Modifier.padding(top = 8.dp),
                    color = warmWhite.copy(alpha = 0.38f),
                    style = MaterialTheme.typography.labelSmall,
                )
            } else {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
