package com.example.mynotes

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mynotes.databinding.ActivityMainBinding
import com.example.mynotes.databinding.DialogAddNotesBinding
import com.example.mynotes.roomDB.NoteDao
import com.example.mynotes.roomDB.NoteDatabase
import com.example.mynotes.roomDB.NoteEntity
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.Calendar


class MainActivity : AppCompatActivity() {


    private lateinit var binding : ActivityMainBinding
    private lateinit var noteDao : NoteDao
    private lateinit var noteDialog : Dialog
    private lateinit var noteDialogBinding: DialogAddNotesBinding
    // to get the current date
    private var c : Calendar = Calendar.getInstance()
    private lateinit var df : SimpleDateFormat
    private var currentDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // database
        val db = NoteDatabase.getInstance(this)
        noteDao = db.noteDao()

        setupUi()
        setSearchButton()
    }

    private fun setupUi() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.addNoteDialog.setOnClickListener {
            setAddNoteDialog()
        }
        lifecycleScope.launch {
            noteDao.fetchAllNotes().collect {
                if (it.isNotEmpty()) {
                    setRecyclerView(it)
                } else {
                    binding.rvNotes.visibility = View.GONE
                    binding.tvNoNotes.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setSearchButton() {
        binding.searchNotesBtn.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                queryNote(query.toString())
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                queryNote(query.toString())
                return false
            }

        })
    }
    private fun queryNote(query: String) {
        lifecycleScope.launch {
            noteDao.fetchNotesByTitle(query).collect {
                setRecyclerView(it)
            }
        }
    }
/**
 * Note dialog is used for adding and updating notes. This dialog is used for
 * two functions. With the diff of add or update button only.
 * */
    private fun setNoteDialog(noteDialogTitle: String, positiveButton: () -> Unit) {
        noteDialog = Dialog(this, R.style.App_Dialog)
        noteDialogBinding = DialogAddNotesBinding.inflate(layoutInflater)
        noteDialog.setContentView(noteDialogBinding.root)
        noteDialog.setCanceledOnTouchOutside(false)

        // Add or Update button title
        noteDialogBinding.addNoteBtn.text = noteDialogTitle

        noteDialogBinding.cancelAddNoteBtn.setOnClickListener {
            noteDialog.dismiss()
        }
        noteDialogBinding.addNoteBtn.setOnClickListener {
            // add or update button function
            positiveButton()
        }
        noteDialog.show()
    }

    private fun setAddNoteDialog() {
        setNoteDialog("Add") { noteDialogAddBtnClicked()}
    }

    private fun noteDialogAddBtnClicked() {
        val title = noteDialogBinding.etAddTitle.text.trim().toString()
        val content = noteDialogBinding.etAddContent.text.trim().toString()

        lifecycleScope.launch {
            if (title.isNotEmpty() && content.isNotEmpty()) {
                noteDao.insert(NoteEntity(title=title, content=content, dateCreated = getCurrentDate()))
                noteDialog.dismiss()
                Toast.makeText(applicationContext,"Hold to delete the note.",Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(applicationContext,"Title or Content cannot be empty.",Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun setUpdateNoteDialog(id: Int) {
        lifecycleScope.async {
            noteDao.fetchNotesById(id).collect { note ->
                println("UPDATE: ${Thread.currentThread().name}")
                noteDialogBinding.etAddTitle.setText(note.title)
                noteDialogBinding.etAddContent.setText(note.content)
            }
        }

        setNoteDialog("Update") {
            noteDialogUpdateBtnClicked(id)
        }
    }
    private fun noteDialogUpdateBtnClicked(id:Int) {
        val title = noteDialogBinding.etAddTitle.text.trim().toString()
        val content = noteDialogBinding.etAddContent.text.trim().toString()

        lifecycleScope.launch {
            if (title.isNotEmpty() && content.isNotEmpty()) {
                noteDao.update(NoteEntity(id,title,content,getCurrentDate()))
                noteDialog.dismiss()
            } else {
                Toast.makeText(applicationContext,"Title or Content cannot be empty.",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteNote(id: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Note")
        builder.setMessage("Are you sure? Do you really want to delete it?")
        builder.setIcon(R.drawable.ic_baseline_warning_amber_24)

        builder.setPositiveButton("Yes"){ _,_ ->
                lifecycleScope.launch {
                    noteDao.delete(NoteEntity(id))
                    println("DELETE: ${Thread.currentThread().name}")
                    Toast.makeText(applicationContext,"Note deleted",Toast.LENGTH_LONG).show()
                }
        }

        builder.setNegativeButton("Cancel"){_,_ -> }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun setRecyclerView(notes: List<NoteEntity>) {
        val adapter = NotesAdapter(
            notes,
            { id -> setUpdateNoteDialog(id) },
            { id -> deleteNote(id) }
        )
        binding.rvNotes.adapter = adapter
        binding.rvNotes.layoutManager = GridLayoutManager(this,2)
        binding.rvNotes.visibility = View.VISIBLE
        binding.tvNoNotes.visibility = View.GONE
    }


    @SuppressLint("SimpleDateFormat")
    private fun getCurrentDate() : String {
        df = SimpleDateFormat("dd-MMM-yy")
        currentDate = df.format(c.time)
        return currentDate
    }
}
