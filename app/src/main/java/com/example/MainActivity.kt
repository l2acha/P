package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.database.AppDatabase
import com.example.data.repository.PloysaiRepository
import com.example.ui.screens.MainContainer
import com.example.ui.theme.PloysaiTheme
import com.example.ui.viewmodel.PloysaiViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Initialize DB and DAOs
        val database = AppDatabase.getDatabase(this)
        
        // 2. Instantiate repository
        val repository = PloysaiRepository(database.chatDao(), database.memoryDao())
        
        // 3. Create viewmodel with factory
        val viewModel = ViewModelProvider(
            this, 
            PloysaiViewModel.Factory(repository)
        )[PloysaiViewModel::class.java]

        setContent {
            PloysaiTheme {
                MainContainer(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
