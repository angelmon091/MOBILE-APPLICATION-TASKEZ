package com.example.proyectofinal

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.proyectofinal.databinding.ActivityAddNoteBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla para añadir o editar una nota.
 */
class AddNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNoteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setCurrentDate()
    }

    private fun setupUI() {
        // Manejo de Insets para respetar las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                left = systemBars.left,
                right = systemBars.right,
                bottom = systemBars.bottom
            )
            // La Toolbar superior maneja el padding del status bar
            binding.toolbar.updatePadding(top = systemBars.top)
            insets
        }

        // Botón para regresar a la pantalla principal
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    /**
     * Establece la fecha actual automáticamente en el formato solicitado.
     */
    private fun setCurrentDate() {
        val sdf = SimpleDateFormat("EEEE, d 'de' MMMM 'a las' HH:mm", Locale("es", "ES"))
        val currentDate = sdf.format(Date())
        // Capitalizar la primera letra
        binding.tvDate.text = currentDate.replaceFirstChar { it.uppercase() }
    }
}
