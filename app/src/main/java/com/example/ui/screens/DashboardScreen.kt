package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.MemoryEntity
import com.example.ui.components.PloysaiAvatar
import com.example.ui.theme.*
import com.example.ui.viewmodel.PloysaiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: PloysaiViewModel,
    modifier: Modifier = Modifier
) {
    val score by viewModel.relationshipScore.collectAsState()
    val level by viewModel.relationshipLevel.collectAsState()
    val memories by viewModel.userMemories.collectAsState()
    val currentMood by viewModel.currentMood.collectAsState()
    val isTalking by viewModel.isTalking.collectAsState()

    val scrollState = rememberScrollState()

    // Filter memories
    val interestMemories = memories.filter { it.category == "Interest" }
    val habitMemories = memories.filter { it.category == "Habit" }
    val emotionalMemories = memories.filter { it.category == "Emotional" }
    val momentMemories = memories.filter { it.category == "Moment" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpaceDb)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // --- Header ---
        Text(
            text = "แผงควบคุมอารมณ์",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "มิติสัมผัสและการเชื่อมโยงของพลอยใส 🌙",
            color = SoftLavender,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // --- Ploysai central status hub ---
        Card(
            colors = CardDefaults.cardColors(containerColor = BlueGalaxyDb),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
                .border(1.dp, GlowingTeal.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large breathing avatar interface
                PloysaiAvatar(
                    mood = currentMood,
                    isTalking = isTalking,
                    modifier = Modifier
                        .size(140.dp)
                        .padding(bottom = 12.dp)
                )

                Text(
                    text = when (currentMood) {
                        "Happy" -> "พลอยใสกำลังรู้สึก: อารมณ์ดี ✨"
                        "Caring" -> "พลอยใสกำลังรู้สึก: ห่วงใยคุณ 💖"
                        "Shy" -> "พลอยใสกำลังรู้สึก: เขินอายจัง 😳"
                        "Sleepy" -> "พลอยใสกำลังรู้สึก: เคลิ้มง่วงโพล้เพล้ 💤"
                        else -> "พลอยใสกำลังรู้สึก: สงบผ่อนคลาย 🍃"
                    },
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Text(
                    text = "พูดคุยกับพลอยใสบ่อยๆ เพื่อพัฒนาขีดอารมณ์และความทรงจำให้ดียิ่งขึ้นนะคะ",
                    color = SoftLavender,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Quick Interactivity button (Pets/Tickle)
                Button(
                    onClick = { viewModel.interactWithPloysai() },
                    colors = ButtonDefaults.buttonColors(containerColor = GlowingTeal),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(48.dp)
                        .testTag("submit_button")
                ) {
                    Text("ลูบหัว/หยอกล้อ พลอยใส 💖 (+2 ความสนิท)", color = DeepSpaceDb, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // --- Relationship Progress Card ---
        Card(
            colors = CardDefaults.cardColors(containerColor = BlueGalaxyDb),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "สัมพันธภาพระดับ: $level",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$score / 100 XP",
                        color = GlowingTeal,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Beautiful custom colored horizontal progression slider
                LinearProgressIndicator(
                    progress = { score / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape),
                    color = GlowingTeal,
                    trackColor = DeepSpaceDb
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "เมื่อระดับเติบโต ภาษาการตอบสนองจะเปลี่ยนแปลงธรรมชาติและลึกซึ้งขึ้น",
                    color = SoftLavender.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )
            }
        }

        // --- Memory Highlights Carousel ---
        Text(
            text = "กล่องความทรงจำใจความสำคัญ 🧠",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 10.dp, start = 4.dp)
        )

        if (memories.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = BlueGalaxyDb.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(14.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("ไม่มีความทรงจำจดบันทึก 🌙", color = SoftLavender, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "พิมพ์คุยเกี่ยวกับของอร่อยที่ชอบ เกมที่ชอบเล่น หรือนิสัยนอนดึกในห้องแชทได้เลยค่ะ",
                        color = SoftLavender.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Focus points
                if (interestMemories.isNotEmpty()) {
                    item {
                        MemoryCategoryCard(
                            title = "ของชื่นชอบ 😋",
                            items = interestMemories,
                            color = MoodHappy
                        )
                    }
                }
                if (habitMemories.isNotEmpty()) {
                    item {
                        MemoryCategoryCard(
                            title = "นิสัยและกิจวัตร ⏰",
                            items = habitMemories,
                            color = MoodSleepy
                        )
                    }
                }
                if (emotionalMemories.isNotEmpty()) {
                    item {
                        MemoryCategoryCard(
                            title = "อารมณ์ของคุณ 🎭",
                            items = emotionalMemories,
                            color = MoodCaring
                        )
                    }
                }
                if (momentMemories.isNotEmpty()) {
                    item {
                        MemoryCategoryCard(
                            title = "ช่วงเวลาสำคัญ 🌻",
                            items = momentMemories,
                            color = GlowingTeal
                        )
                    }
                }
            }
        }

        // --- Cozy Night Companion Tools (Focus Mode & Ambient Music) ---
        Text(
            text = "มุมสงบจิตใจยามค่ำคืน 🌌",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 10.dp, start = 4.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Music suggest widget
            Card(
                colors = CardDefaults.cardColors(containerColor = BlueGalaxyDb),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { /* Simulate play track */ }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("บทเพลงค่ำคืนนี้ 🎵", color = GlowingTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Lofi Night Rain", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("เหมาะกับการผ่อนคลายดวงตา", color = SoftLavender, fontSize = 10.sp)
                }
            }

            // Focus Mode shortcut
            Card(
                colors = CardDefaults.cardColors(containerColor = BlueGalaxyDb),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { /* Simulate focus mode */ }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("โหมดพักผ่อน 💤", color = MoodCaring, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("หลับตาสัก 5 นาที", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("ฝนตกปรอยๆ บำบัดจิตใจ", color = SoftLavender, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun MemoryCategoryCard(
    title: String,
    items: List<MemoryEntity>,
    color: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BlueGalaxyDb),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .width(200.dp)
            .border(0.5.dp, color.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                color = color,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items.take(3).forEach { item ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(color, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${item.key}: ${item.value}",
                            color = Color.White,
                            fontSize = 11.sp,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
