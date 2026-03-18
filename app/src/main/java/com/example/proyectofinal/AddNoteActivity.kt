package com.example.proyectofinal

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.example.proyectofinal.databinding.ActivityAddNoteBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla para añadir o editar una tarea.
 */
class AddNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNoteBinding
    private val viewModel: NoteViewModel by viewModels()
    private var existingNote: Note? = null
    private var category: String = "Todas"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        
        // Obtener categoría del intent si es una tarea nueva
        category = intent.getStringExtra("CATEGORY_EXTRA") ?: "Todas"
        
        // Cargar tarea si venimos de una edición
        val noteId = intent.getIntExtra("NOTE_ID", -1)
        if (noteId != -1) {
            loadExistingNote(noteId)
        } else {
            setCurrentDate()
        }
    }

    private fun loadExistingNote(id: Int) {
        lifecycleScope.launch {
            existingNote = viewModel.getNoteById(id)
            existingNote?.let { note ->
                binding.etTitle.setText(note.title)
                binding.etContent.setText(note.content)
                binding.tvDate.text = note.date
                category = note.category // Mantener la categoría original al editar
            }
        }
    }

    private fun setupUI() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                left = systemBars.left,
                right = systemBars.right,
                bottom = systemBars.bottom
            )
            binding.toolbar.updatePadding(top = systemBars.top)
            insets
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnSave.setOnClickListener {
            saveNote()
        }
    }

    private fun saveNote() {
        val titleInput = binding.etTitle.text.toString().trim()
        val content = binding.etContent.text.toString().trim()
        val date = binding.tvDate.text.toString()

        if (titleInput.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, "La tarea no puede estar vacía", Toast.LENGTH_SHORT).show()
            return
        }

        val finalTitle = if (titleInput.isEmpty()) "Sin título" else titleInput

        if (existingNote != null) {
            // Actualizar existente
            val updatedNote = existingNote!!.copy(
                title = finalTitle,
                content = content,
                category = category
            )
            viewModel.update(updatedNote)
            Toast.makeText(this, "Tarea actualizada", Toast.LENGTH_SHORT).show()
        } else {
            // Crear nueva
            val newNote = Note(
                title = finalTitle,
                content = content,
                date = date,
                category = category
            )
            viewModel.insert(newNote)
            Toast.makeText(this, "Tarea guardada", Toast.LENGTH_SHORT).show()
        }
        
        finish()
    }

    private fun setCurrentDate() {
        val sdf = SimpleDateFormat("EEEE, d 'de' MMMM 'a las' HH:mm", Locale("es", "ES"))
        val currentDate = sdf.format(Date())
        binding.tvDate.text = currentDate.replaceFirstChar { it.uppercase() }
    }
}
