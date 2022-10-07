package com.example.mynotes.roomDB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes-table")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id : Int = 0,
    val title : String = "",
    val content : String = "",
    val dateCreated: String = ""
)
