package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.BuildConfig
import com.example.ui.theme.*
import com.example.ui.viewmodel.PloysaiViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: PloysaiViewModel,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val score by viewModel.relationshipScore.collectAsState()
    val level by viewModel.relationshipLevel.collectAsState()
    val currentMood by viewModel.currentMood.collectAsState()

    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // Preferences states
    var selectedVoice by remember { mutableStateOf("กาลเวลาเงียบสงบ (Calm Night)") }
    var notificationEnabled by remember { mutableStateOf(true) }
    var showApiKeyWarning by remember { mutableStateOf(true) }

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
            text = "การตั้งค่าคู่สนทนา",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "ปรับตั้งค่าคลื่นความถี่อารมณ์พลอยใสและข้อมูลส่วนตัว",
            color = SoftLavender,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // --- Ploysai Mini Profile Status ---
        Card(
            colors = CardDefaults.cardColors(containerColor = BlueGalaxyDb),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .background(
                            brush = Brush.linearGradient(listOf(GlowingTeal, CyberBlue)),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💎", fontSize = 21.sp)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "พลอยใส (Ploysai Core v1.2)",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "สัมพันธภาพปัจจุบัน: $level",
                        color = GlowingTeal,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // --- Voice Engine custom selections ---
        Text(
            text = "ระบบสังเคราะห์เสียงคู่สนทนา 🎙️",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = BlueGalaxyDb),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                val voices = listOf(
                    "กาลเวลาเงียบสงบ (Calm Night)",
                    "เพื่อนสนิทร่าเริง (Cheerful Friend)",
                    "พยัญชนะอ่อนหวาน (Sweet Melody)"
                )
                voices.forEach { voice ->
                    val isSelected = selectedVoice == voice
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedVoice = voice }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedVoice = voice },
                            colors = RadioButtonDefaults.colors(selectedColor = GlowingTeal)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = voice,
                            color = if (isSelected) Color.White else SoftLavender,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // --- Relationship progression booster shortcuts (Admin Tools) ---
        Text(
            text = "ทางลัดความก้าวหน้าสัมพันธภาพ ✨",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        
        Card(
            colors = CardDefaults.cardColors(containerColor = BlueGalaxyDb),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "กดจำลองเพื่อยกระดับสัมพันธภาพ พินิจผลลัพธ์พฤติกรรมการตอบกลับของพลอยใสในแท็กสนทนาแต่ละระดับได้ทันที",
                    color = SoftLavender,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                val current = viewModel.relationshipScore.value
                                val extra = (current + 15).coerceAtMost(100)
                                viewModel.triggerVoiceInput("จำลองแต่งความสัมพันธ์ยกระดับ XP!")
                                // Set manually
                                val repo = com.example.data.database.AppDatabase.getDatabase(context)
                                    .let { db -> com.example.data.repository.PloysaiRepository(db.chatDao(), db.memoryDao()) }
                                repo.setRelationshipScore(extra)
                                viewModel.refreshRelationship()
                                viewModel.updateMood("Happy")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberBlue),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("submit_button")
                    ) {
                        Text("เพิมพลัง +15 XP", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.clearAppData() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.85f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text("ล้างข้อมูลทั้งหมด 🧹", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- Notification and Theme Controls ---
        Text(
            text = "การคุ้มครองความเป็นส่วนตัวและทั่วไป ⚙️",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = BlueGalaxyDb),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Notifications switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("การเคียงข้างอารมณ์ตลอดค่ำคืน", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("ส่งแจ้งเตือนเป็นห่วงนุ่มนวลก่อนนอน", color = SoftLavender, fontSize = 11.sp)
                    }
                    Switch(
                        checked = notificationEnabled,
                        onCheckedChange = { notificationEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = GlowingTeal, checkedTrackColor = GlowingTeal.copy(alpha = 0.35f))
                    )
                }

                Divider(color = GlowingTeal.copy(alpha = 0.12f), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                // Connection indicator info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("เซิร์ฟเวอร์จิตสำนึกพลอยใส", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("สถานะ: เชื่อมต่อสำเร็จผ่านคลาวด์", color = SoftLavender, fontSize = 11.sp)
                    }
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color.Green, CircleShape)
                    )
                }
            }
        }

        // --- MANDATED API SECURITY WARNING KEY HOLDER ---
        Text(
            text = "ความปลอดภัยและการปกป้องคีย์ 🛡️",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = BlueGalaxyDb.copy(alpha = 0.45f)),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .border(1.dp, Color.Red.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = "คำเตือนความปลอดภัย", tint = Color.Red)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "คำแจ้งเตือนความปลอดภัย APK",
                        color = Color.Red,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Explicit security warning from the android secret management skill
                Text(
                    text = "**Security Warning**: I have included your API keys in the generated APK file for this prototype. Please be aware that Android APKs can be easily decompiled, and these keys can be extracted by anyone who has access to the file. **Do not share this APK file publicly or with unauthorized individuals** to prevent potential misuse.",
                    color = SoftLavender,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Text(
                    text = "คีย์จดจำคลาวด์ยามใช้งานปัจจุบัน: ${if(BuildConfig.GEMINI_API_KEY == "MY_GEMINI_API_KEY") "โหมดจำลองในแอป (ไม่ได้ใส่คีย์จริง)" else "โหมดคลาวด์ Gemini API คลื่นความถี่ทำงานเต็มกำลัง 🚀"}",
                    color = GlowingTeal,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
