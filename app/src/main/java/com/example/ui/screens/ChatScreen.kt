package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.ChatMessageEntity
import com.example.ui.components.PloysaiAvatar
import com.example.ui.theme.*
import com.example.ui.viewmodel.PloysaiViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: PloysaiViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.chatMessages.collectAsState()
    val isThinking by viewModel.isThinking.collectAsState()
    val currentMood by viewModel.currentMood.collectAsState()
    val isTalking by viewModel.isTalking.collectAsState()
    val relationshipLevel by viewModel.relationshipLevel.collectAsState()

    var textInput by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()

    // Keep chat scrolled to bottom when a new message arrives
    LaunchedEffect(messages.size, isThinking) {
        if (messages.isNotEmpty()) {
            lazyListState.animateScrollToItem(messages.size)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpaceDb)
    ) {
        // --- Custom Polish Header with Tiny Active Avatar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BlueGalaxyDb.copy(alpha = 0.85f))
                .statusBarsPadding()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // High fidelity tiny reactive avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(DeepSpaceDb)
                    .border(1.dp, GlowingTeal.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                PloysaiAvatar(
                    mood = currentMood,
                    isTalking = isTalking,
                    modifier = Modifier.size(75.dp) // larger canvas bounds but fits inside small clip
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "พลอยใส (Ploysai)",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    // Mood dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = when (currentMood) {
                                    "Happy" -> MoodHappy
                                    "Caring" -> MoodCaring
                                    "Shy" -> MoodShy
                                    "Sleepy" -> MoodSleepy
                                    else -> MoodCalm
                                },
                                shape = CircleShape
                            )
                    )
                }
                
                Text(
                    text = "ความคุ้นเคย: $relationshipLevel",
                    color = SoftLavender.copy(alpha = 0.85f),
                    fontSize = 12.sp
                )
            }
        }

        Divider(color = GlowingTeal.copy(alpha = 0.12f), thickness = 1.dp)

        // --- Chat Bubble Feed ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
        ) {
            if (messages.isEmpty() && !isThinking) {
                // Empty state layout beautifully decorated matching design guidelines
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🪐",
                        fontSize = 42.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text(
                        text = "คืนนี้คุณเป้นยังไงบ้างคะ?",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "เริ่มพิมพ์คุยกับพลอยใสได้เลยน้า คุยปรึกษา บอกเล่าเรื่องเหนื่อยใจ หรือกิจกรรมที่ชื่นชอบ เดี๋ยวพลอยใสจดจำไว้คุยพรุ่งนี้เองค่ะ",
                        color = SoftLavender,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("message_list"),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(messages) { message ->
                        ChatBubbleRow(message)
                    }

                    // Floating typing indicator for Ploysai
                    if (isThinking) {
                        item {
                            TypingBubble()
                        }
                    }
                }
            }
        }

        // --- Chat Text Input Dock with proper Window Insets ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(), // keeps space for gesture bar or system buttons
            color = BlueGalaxyDb,
            tonalElevation = 4.dp
        ) {
            Column {
                Divider(color = GlowingTeal.copy(alpha = 0.12f), thickness = 1.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = {
                            Text(
                                "พูดคุยกับพลอยใส...",
                                color = SoftLavender.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = DeepSpaceDb.copy(alpha = 0.8f),
                            unfocusedContainerColor = DeepSpaceDb.copy(alpha = 0.5f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = GlowingTeal,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("text_input"),
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (textInput.isNotBlank()) {
                                    viewModel.sendTextMessage(textInput.trim())
                                    textInput = ""
                                }
                            }
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                viewModel.sendTextMessage(textInput.trim())
                                textInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                brush = Brush.linearGradient(listOf(GlowingTeal, CyberBlue)),
                                shape = CircleShape
                            )
                            .testTag("send_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "ส่งข้อความ",
                            tint = DeepSpaceDb
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubbleRow(message: ChatMessageEntity) {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    val formattedTime = sdf.format(Date(message.timestamp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
        ) {
            // Main Bubble Outer Wrapper
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .background(
                        brush = if (message.isUser) {
                            Brush.linearGradient(
                                colors = listOf(CyberBlue, CyberBlue.copy(alpha = 0.85f))
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(BlueGalaxyDb, BlueGalaxyDb.copy(alpha = 0.9f))
                            )
                        },
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (message.isUser) 16.dp else 2.dp,
                            bottomEnd = if (message.isUser) 2.dp else 16.dp
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = if (message.isUser) {
                            Brush.linearGradient(listOf(Color.White.copy(alpha = 0.15f), Color.Transparent))
                        } else {
                            Brush.linearGradient(
                                listOf(
                                    when (message.moodTag) {
                                        "Happy" -> MoodHappy
                                        "Caring" -> MoodCaring
                                        "Shy" -> MoodShy
                                        "Sleepy" -> MoodSleepy
                                        else -> GlowingTeal
                                    }.copy(alpha = 0.35f),
                                    Color.Transparent
                                )
                            )
                        },
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (message.isUser) 16.dp else 2.dp,
                            bottomEnd = if (message.isUser) 2.dp else 16.dp
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 11.dp)
            ) {
                Column {
                    Text(
                        text = message.text,
                        color = Color.White,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                }
            }
            
            // Sub-metrics (Timestamp + Mood mood emoji markers for Ploysai)
            Row(
                modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!message.isUser) {
                    val moodEmoji = when (message.moodTag) {
                        "Happy" -> "✨ อารมณ์ดี"
                        "Caring" -> "💖 ห่วงใย"
                        "Shy" -> "😳 เขินอาย"
                        "Sleepy" -> "🌙 ง่วงนอน"
                        else -> "🍃 สงบเสงี่ยม"
                    }
                    Text(
                        text = moodEmoji,
                        color = SoftLavender.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                Text(
                    text = formattedTime,
                    color = SoftLavender.copy(alpha = 0.5f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun TypingBubble() {
    val infiniteTransition = rememberInfiniteTransition(label = "Typing")
    val dotAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Dot1"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = BlueGalaxyDb,
                    shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp)
                )
                .border(
                    width = 1.dp,
                    color = GlowingTeal.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp)
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("พลอยใสกำลังพิมพ์", color = SoftLavender, fontSize = 12.sp)
                Box(modifier = Modifier.size(5.dp).background(GlowingTeal.copy(alpha = dotAlpha1), CircleShape))
                Box(modifier = Modifier.size(5.dp).background(GlowingTeal.copy(alpha = (dotAlpha1 + 0.3f).coerceIn(0f, 1f)), CircleShape))
                Box(modifier = Modifier.size(5.dp).background(GlowingTeal.copy(alpha = (dotAlpha1 + 0.6f).coerceIn(0f, 1f)), CircleShape))
            }
        }
    }
}
