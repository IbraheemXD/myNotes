package com.example.mynotes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mynotes.databinding.ItemRowBinding
import com.example.mynotes.roomDB.NoteEntity
import java.text.SimpleDateFormat
import java.util.*


// goes to main method or onCreate(Android)

class NotesAdapter(
    private val notes: List<NoteEntity>,
    private val updateListener: (id: Int) -> Unit,
    private val deleteListener: (id: Int) -> Unit
    ) : RecyclerView.Adapter<NotesAdapter.ViewHolder>() {


    inner class ViewHolder(private val binding: ItemRowBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bindItem(note: NoteEntity) {
            binding.rvNoteTitle.text = note.title
            binding.rvNoteContent.text = note.content
            binding.rvNoteDate.text = note.dateCreated

            binding.rvNoteCard.setOnClickListener {
                updateListener(note.id)
            }

            binding.rvNoteCard.setOnLongClickListener {
                deleteListener(note.id)
                true
            }

        }
    }

    /**
     * onCreateViewHolder is called when there is no existing view.
     * if there exist a view than onBindViewHolder will be call to bind
     * that view.
     * */

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = notes[position]
        holder.bindItem(note)
    }

    override fun getItemCount(): Int {
        return notes.count()
    }
}