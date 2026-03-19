package com.example.proyectofinal

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String = "",
    val dueDate: String, // Format: YYYY-MM-DD
    val dueTime: String, // Format: HH:mm
    val isCompleted: Boolean = false,
    val priority: Int = 0, // 0: Normal, 1: High
    val category: String = "General"
)
