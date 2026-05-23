package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PloysaiAvatar
import com.example.ui.theme.*
import com.example.ui.viewmodel.PloysaiViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VoiceScreen(
    viewModel: PloysaiViewModel,
    modifier: Modifier = Modifier
) {
    val currentMood by viewModel.currentMood.collectAsState()
    val isTalking by viewModel.isTalking.collectAsState()

    var isListening by remember { mutableStateOf(false) }
    var voiceStatusText by remember { mutableStateOf("แตะไมโครโฟน แล้วลองพูดคุยดูนะคะ") }

    val scope = rememberCoroutineScope()

    // Pulse animation for the glowing button ring
    val infiniteTransition = rememberInfiniteTransition(label = "VoicePulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseAlpha"
    )

    // Animated waveform bars
    val waveHeights = remember { mutableStateListOf(0.1f, 0.3f, 0.6f, 0.4f, 0.8f, 0.5f, 0.9f, 0.3f, 0.7f, 0.2f, 0.5f, 0.1f) }
    LaunchedEffect(isListening, isTalking) {
        if (isListening || isTalking) {
            while (true) {
                for (i in waveHeights.indices) {
                    waveHeights[i] = (0.15f + Math.random().toFloat() * 0.85f)
                }
                viewModel.updateVoiceWaveform(waveHeights.toList())
                delay(120)
            }
        } else {
            // Calm waveform idle
            val idleWave = listOf(0.15f, 0.2f, 0.15f, 0.2f, 0.25f, 0.2f, 0.25f, 0.2f, 0.15f, 0.2f, 0.15f, 0.1f)
            for (i in waveHeights.indices) {
                waveHeights[i] = idleWave[i]
            }
            viewModel.updateVoiceWaveform(idleWave)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpaceDb)
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- Ploysai Avatar ---
        PloysaiAvatar(
            mood = currentMood,
            isTalking = isTalking,
            modifier = Modifier
                .size(160.dp)
                .padding(bottom = 32.dp)
        )

        // --- Status textual feedback ---
        Text(
            text = "คลื่นความถี่เชื่อมสัมพันธ์ทางจิตใจ 🌙",
            color = SoftLavender,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = voiceStatusText,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .height(60.dp)
                .padding(horizontal = 12.dp).padding(bottom = 24.dp)
        )

        // --- Audio Visual Waveform Canvas ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(bottom = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val barWidth = 7.dp.toPx()
                val gap = 6.dp.toPx()
                val totalBars = waveHeights.size
                val totalWidth = (barWidth * totalBars) + (gap * (totalBars - 1))
                val startX = (size.width - totalWidth) / 2f

                for (i in 0 until totalBars) {
                    val progress = waveHeights[i]
                    val maxBarHeight = size.height * 0.95f
                    val currentBarH = maxBarHeight * progress
                    val currentBarY = (size.height - currentBarH) / 2f
                    val currentBarX = startX + i * (barWidth + gap)

                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(
                                GlowingTeal,
                                if (isListening) CyberBlue else MoodCaring
                            )
                        ),
                        topLeft = Offset(currentBarX, currentBarY),
                        size = Size(barWidth, currentBarH),
                        cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                    )
                }
            }
        }

        // --- Mic Interactive Button ---
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            // Animated Pulse Outer Ring
            if (isListening) {
                Box(
                    modifier = Modifier
                        .size(126.dp)
                        .clip(CircleShape)
                        .border(
                            width = (12.dp * (1.5f - pulseScale)),
                            color = GlowingTeal.copy(alpha = pulseAlpha),
                            shape = CircleShape
                        )
                )
            }

            // Central Mic Button with custom design and spring feedback
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = if (isListening) {
                                listOf(CyberBlue, GlowingTeal)
                            } else {
                                listOf(BlueGalaxyDb, BlueGalaxyDb.copy(alpha = 0.8f))
                            }
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        color = if (isListening) Color.White else GlowingTeal.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
                    .clickable {
                        scope.launch {
                            if (!isListening) {
                                isListening = true
                                voiceStatusText = "พลอยใสกำลังฟังคุณคุยอยู่ค่ะ... พูดได้เลยน้า"
                                // Simulate Listening audio delay
                                delay(3200)
                                if (isListening) { // If still active on simulated complete
                                    isListening = false
                                    voiceStatusText = "กำลังแปลงข้อมูลเสียงเป็นคลื่นความห่วงใย..."

                                    // Pick a cute random Thai talk prompt to emit
                                    val simulatedQuestions = listOf(
                                        "สวัสดีจ้าพลอยใส สดใสเป็นยังไงบ้างคะ",
                                        "วันนี้ทำงานเหนื่อยมากๆ เลย อยากขอกอดฮีลใจหน่อยนะ",
                                        "คืนนี้นอนไม่หลับเลยแฮะ ดึกมากๆ แล้วด้วย",
                                        "วันนี้วันแต่งงานฉันแหละ มีเรื่องราวสำเร็จมากมากชอบกินส้มตำด้วย"
                                    )
                                    val queryText = simulatedQuestions.random()
                                    viewModel.triggerVoiceInput(queryText)
                                    
                                    voiceStatusText = "ส่งเสียงสำเร็จ: \"$queryText\""
                                    delay(2000)
                                    voiceStatusText = "แตะไมโครโฟน เพื่อพูดรอบถัดไปน้า 🎙️"
                                }
                            } else {
                                isListening = false
                                voiceStatusText = "แตะไมโครโฟน แล้วลองพูดคุยดูนะคะ"
                            }
                        }
                    }
                    .testTag("submit_button"), // standard mic prompt testing trigger Tag
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "ไมโครโฟนพูดคุย",
                    tint = if (isListening) DeepSpaceDb else GlowingTeal,
                    modifier = Modifier.size(38.dp)
                )
            }
        }

        // --- Extra simulated speech options helper ---
        Text(
            text = "แอปจำลองการส่งข้อมูลเสียงเป็นข้อความพูดคุยอัตโนมัติ",
            color = SoftLavender.copy(alpha = 0.6f),
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
