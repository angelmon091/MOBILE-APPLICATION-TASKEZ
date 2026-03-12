package com.example.proyectofinal

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.proyectofinal.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

/**
 * Pantalla principal de la aplicación TaskEzz.
 * Gestiona la navegación y el diseño minimalista en blanco y azul.
 */
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Habilita el diseño de borde a borde (edge-to-edge)
        enableEdgeToEdge()
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupNavigation()
        setupBackNavigation()
    }

    /**
     * Configura la interfaz de usuario y gestiona los Insets del sistema.
     */
    private fun setupUI() {
        // Ajuste de padding para las barras del sistema (Status Bar y Navigation Bar)
        ViewCompat.setOnApplyWindowInsetsListener(binding.drawerLayout) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            // Aplicamos padding al contenido principal
            binding.mainContent.updatePadding(
                left = systemBars.left,
                right = systemBars.right,
                bottom = systemBars.bottom
            )
            
            // Padding superior para la AppBar para respetar el notch/reloj
            binding.appBarLayout.updatePadding(top = systemBars.top)
            
            // El sidebar (NavigationView) también debe respetar los insets
            binding.navView.updatePadding(
                top = systemBars.top,
                bottom = systemBars.bottom
            )
            
            insets
        }

        // Configuración de la Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        
        // Listener para abrir el menú lateral
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // Conexión con la pantalla de añadir nota
        binding.fab.setOnClickListener {
            val intent = Intent(this, AddNoteActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Configura los listeners de navegación.
     */
    private fun setupNavigation() {
        binding.navView.setNavigationItemSelectedListener(this)
    }

    /**
     * Gestiona el comportamiento del botón Atrás.
     */
    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    /**
     * Gestiona la selección de items en el menú lateral.
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        handleNavigationAction(item.itemId)
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     * Procesa la acción correspondiente al item seleccionado.
     */
    private fun handleNavigationAction(id: Int) {
        when (id) {
            R.id.nav_inicio -> {
                showToast(getString(R.string.toast_home_selected))
            }
            R.id.nav_recordatorio, R.id.nav_favoritos, R.id.nav_todas,
            R.id.nav_escuela, R.id.nav_proyecto, R.id.nav_trabajo, R.id.nav_ajustes -> {
                showToast("Opción seleccionada: ${getString(getMenuTitleRes(id))}")
            }
        }
    }

    /**
     * Obtiene el recurso de string del título según el ID del menú.
     */
    private fun getMenuTitleRes(id: Int): Int {
        return when (id) {
            R.id.nav_inicio -> R.string.menu_home
            R.id.nav_recordatorio -> R.string.menu_reminders
            R.id.nav_favoritos -> R.string.menu_favorites
            R.id.nav_todas -> R.string.menu_all
            R.id.nav_escuela -> R.string.menu_school
            R.id.nav_proyecto -> R.string.menu_project
            R.id.nav_trabajo -> R.string.menu_work
            R.id.nav_ajustes -> R.string.menu_settings
            else -> R.string.app_name
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> true
            R.id.action_more -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
