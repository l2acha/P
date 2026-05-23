package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.BlueGalaxyDb
import com.example.ui.theme.DeepSpaceDb
import com.example.ui.theme.GlowingTeal
import com.example.ui.theme.SoftLavender
import com.example.ui.viewmodel.PloysaiViewModel

enum class PloysaiTab {
    Chat, Dashboard, Voice, Settings
}

@Composable
fun MainContainer(
    viewModel: PloysaiViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(PloysaiTab.Chat) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = BlueGalaxyDb,
                contentColor = SoftLavender
            ) {
                // 1. Chat Tab Item
                NavigationBarItem(
                    selected = activeTab == PloysaiTab.Chat,
                    onClick = { activeTab = PloysaiTab.Chat },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "ห้องแชท"
                        )
                    },
                    label = { Text("พูดคุย", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DeepSpaceDb,
                        selectedTextColor = GlowingTeal,
                        indicatorColor = GlowingTeal,
                        unselectedIconColor = SoftLavender,
                        unselectedTextColor = SoftLavender
                    )
                )

                // 2. Dashboard Tab Item
                NavigationBarItem(
                    selected = activeTab == PloysaiTab.Dashboard,
                    onClick = { activeTab = PloysaiTab.Dashboard },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Dashboard,
                            contentDescription = "แผงควบคุมอารมณ์"
                        )
                    },
                    label = { Text("แผงควบคุม", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DeepSpaceDb,
                        selectedTextColor = GlowingTeal,
                        indicatorColor = GlowingTeal,
                        unselectedIconColor = SoftLavender,
                        unselectedTextColor = SoftLavender
                    )
                )

                // 3. Voice Tab Item
                NavigationBarItem(
                    selected = activeTab == PloysaiTab.Voice,
                    onClick = { activeTab = PloysaiTab.Voice },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "เสียงเชื่อมใจ"
                        )
                    },
                    label = { Text("เสียงนำใจ", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DeepSpaceDb,
                        selectedTextColor = GlowingTeal,
                        indicatorColor = GlowingTeal,
                        unselectedIconColor = SoftLavender,
                        unselectedTextColor = SoftLavender
                    )
                )

                // 4. Settings Tab Item
                NavigationBarItem(
                    selected = activeTab == PloysaiTab.Settings,
                    onClick = { activeTab = PloysaiTab.Settings },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "การตั้งค่า"
                        )
                    },
                    label = { Text("ตั้งค่า", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DeepSpaceDb,
                        selectedTextColor = GlowingTeal,
                        indicatorColor = GlowingTeal,
                        unselectedIconColor = SoftLavender,
                        unselectedTextColor = SoftLavender
                    )
                )
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0) // lets inner screens coordinate status/nav bars edges themselves
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DeepSpaceDb)
                .padding(innerPadding)
        ) {
            when (activeTab) {
                PloysaiTab.Chat -> ChatScreen(viewModel = viewModel)
                PloysaiTab.Dashboard -> DashboardScreen(viewModel = viewModel)
                PloysaiTab.Voice -> VoiceScreen(viewModel = viewModel)
                PloysaiTab.Settings -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
