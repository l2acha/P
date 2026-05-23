package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val moodTag: String = "Calm"
)

@Entity(tableName = "ploysis_memories")
data class MemoryEntity(
    @PrimaryKey val key: String,
    val value: String,
    val category: String, // Interest, Habit, Emotional, Moment, Meta
    val timestamp: Long = System.currentTimeMillis()
)
