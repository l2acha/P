package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.ChatMessageEntity
import com.example.data.database.MemoryEntity
import com.example.data.repository.PloysaiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PloysaiViewModel(private val repository: PloysaiRepository) : ViewModel() {

    // Chat Message flow
    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.allMessages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // User Memories flow
    val userMemories: StateFlow<List<MemoryEntity>> = repository.allMemories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Relation Score state
    private val _relationshipScore = MutableStateFlow(5)
    val relationshipScore: StateFlow<Int> = _relationshipScore.asStateFlow()

    // Relation Level text
    private val _relationshipLevel = MutableStateFlow("Stranger (คนแปลกหน้า)")
    val relationshipLevel: StateFlow<String> = _relationshipLevel.asStateFlow()

    // Temporary active Ploysai mood
    private val _currentMood = MutableStateFlow("Calm")
    val currentMood: StateFlow<String> = _currentMood.asStateFlow()

    // Talking indicator (mouth animations and pulse visualization)
    private val _isTalking = MutableStateFlow(false)
    val isTalking: StateFlow<Boolean> = _isTalking.asStateFlow()

    // Typing/thinking indicator
    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    // Dynamic Voice audio animation state
    private val _voiceWaveform = MutableStateFlow(List(12) { 0.2f })
    val voiceWaveform: StateFlow<List<Float>> = _voiceWaveform.asStateFlow()

    init {
        refreshRelationship()
    }

    fun refreshRelationship() {
        viewModelScope.launch {
            val score = repository.getRelationshipScore()
            _relationshipScore.value = score
            _relationshipLevel.value = repository.getRelationshipLevel(score)
        }
    }

    fun updateMood(mood: String) {
        _currentMood.value = mood
    }

    // Text message sending
    fun sendTextMessage(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            _isThinking.value = true
            _isTalking.value = false

            // Interaction
            repository.sendMessage(text) { mood ->
                _currentMood.value = mood
            }

            // Completed thinking, start subtle talking animation
            _isThinking.value = false
            _isTalking.value = true

            // Update relationship scores after interaction completes
            refreshRelationship()

            // Run speaking animation for 3.5 seconds
            kotlinx.coroutines.delay(3500)
            _isTalking.value = false
        }
    }

    // Voice interaction trigger
    fun triggerVoiceInput(transcribedText: String) {
        // Simple voice simulation: User taps microphone, we simulate voice input text or custom voice input
        sendTextMessage(transcribedText)
    }

    fun updateVoiceWaveform(wave: List<Float>) {
        _voiceWaveform.value = wave
    }

    fun clearAppData() {
        viewModelScope.launch {
            repository.clearAllData()
            _currentMood.value = "Calm"
            _isTalking.value = false
            _isThinking.value = false
            _relationshipScore.value = 5
            _relationshipLevel.value = "Stranger (คนแปลกหน้า)"
        }
    }

    // Manual relationship increment (e.g. petting or gifting)
    fun interactWithPloysai() {
        viewModelScope.launch {
            val score = repository.getRelationshipScore()
            val newScore = score + 2
            repository.setRelationshipScore(newScore)
            refreshRelationship()

            // Flash happy or hybrid/shy expressions!
            val randomMoods = listOf("Happy", "Caring", "Shy")
            _currentMood.value = randomMoods.random()

            _isTalking.value = true
            kotlinx.coroutines.delay(2000)
            _isTalking.value = false
        }
    }

    // Factory Provider
    class Factory(private val repository: PloysaiRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PloysaiViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PloysaiViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
