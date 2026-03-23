package com.example.proyectofinal

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectofinal.databinding.ActivityCalendarBinding
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarBinding
    private val viewModel: NoteViewModel by viewModels()
    private lateinit var adapter: NoteAdapter
    private var allNotes: List<Note> = emptyList()
    private var selectedDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupRecyclerView()
        observeNotes()
    }

    private fun setupUI() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(left = systemBars.left, right = systemBars.right, bottom = systemBars.bottom)
            binding.appBarLayout.updatePadding(top = systemBars.top)
            insets
        }

        binding.toolbar.setNavigationOnClickListener { finish() }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        selectedDate = sdf.format(Date())

        // Configurar fecha inicial seleccionada en el calendario
        binding.calendarView.setSelectedDate(CalendarDay.today())

        binding.calendarView.setOnDateChangedListener { _, date, _ ->
            val calendar = date.calendar
            selectedDate = sdf.format(calendar.time)
            filterNotesByDate()
        }
    }

    private fun setupRecyclerView() {
        adapter = NoteAdapter(
            onItemClick = { note ->
                val intent = Intent(this, AddNoteActivity::class.java).apply {
                    putExtra("NOTE_ID", note.id)
                }
                startActivity(intent)
            },
            onItemLongClick = { _, _ -> }
        )
        binding.rvDayNotes.layoutManager = LinearLayoutManager(this)
        binding.rvDayNotes.adapter = adapter
    }

    private fun observeNotes() {
        viewModel.allNotes.observe(this) { notes ->
            allNotes = notes
            updateCalendarDecorators()
            filterNotesByDate()
        }
    }

    private fun updateCalendarDecorators() {
        val daysWithNotes = allNotes.mapNotNull { note ->
            note.endDate?.let { dateStr ->
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val date = sdf.parse(dateStr)
                    date?.let { CalendarDay.from(it) }
                } catch (e: Exception) {
                    null
                }
            }
        }.toSet()

        binding.calendarView.removeDecorators()
        // Usamos un color rosa/rojo para los puntos de evento
        binding.calendarView.addDecorator(EventDecorator(Color.parseColor("#E91E63"), daysWithNotes))
    }

    private fun filterNotesByDate() {
        val filtered = allNotes.filter { note ->
            note.endDate == selectedDate
        }
        
        adapter.submitList(filtered)
        
        if (filtered.isEmpty()) {
            binding.rvDayNotes.visibility = View.GONE
            binding.emptyStateCalendar.visibility = View.VISIBLE
        } else {
            binding.rvDayNotes.visibility = View.VISIBLE
            binding.emptyStateCalendar.visibility = View.GONE
        }
    }

    /**
     * Decorador personalizado para añadir un punto indicador
     */
    class EventDecorator(private val color: Int, private val dates: Collection<CalendarDay>) : DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay): Boolean {
            return dates.contains(day)
        }

        override fun decorate(view: DayViewFacade) {
            view.addSpan(DotSpan(8f, color))
        }
    }
}
