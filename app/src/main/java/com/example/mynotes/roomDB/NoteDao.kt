package com.example.mynotes.roomDB

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Insert
    suspend fun insert(noteEntity: NoteEntity)

    @Update
    suspend fun update(noteEntity: NoteEntity)

    @Delete
    suspend fun delete(noteEntity: NoteEntity)

    @Query("SELECT * FROM `notes-table`")
    fun fetchAllNotes() : Flow<List<NoteEntity>>

    @Query("SELECT * FROM `notes-table` WHERE id=:id")
    fun fetchNotesById(id:Int) : Flow<NoteEntity>

    @Query("SELECT * FROM `notes-table` WHERE title like :title || '%' ")
    fun fetchNotesByTitle(title: String) : Flow<List<NoteEntity>>

}