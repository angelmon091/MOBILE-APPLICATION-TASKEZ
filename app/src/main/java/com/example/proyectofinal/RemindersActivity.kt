package com.example.proyectofinal

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectofinal.databinding.ActivityRemindersBinding
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import java.text.SimpleDateFormat
import java.util.*

class RemindersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRemindersBinding
    private val viewModel: ReminderViewModel by viewModels()
    private lateinit var adapter: ReminderAdapter
    
    private var selectedDate = Calendar.getInstance()
    private var selectedTime = Calendar.getInstance()
    private var currentReminders: List<Reminder> = emptyList()
    private var showCompleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRemindersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupRecyclerView()
        setupTabs()
        observeReminders()
    }

    private fun setupUI() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            binding.appBarLayout.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.fabAddReminder.setOnClickListener {
            showAddReminderDialog()
        }
    }

    private fun setupRecyclerView() {
        adapter = ReminderAdapter(
            onToggleComplete = { reminder ->
                viewModel.updateCompletionStatus(reminder.id, !reminder.isCompleted)
            },
            onDelete = { reminder ->
                showDeleteConfirmation(reminder)
            },
            onClick = { reminder ->
                // Opcional: Editar recordatorio
            }
        )
        binding.rvReminders.layoutManager = LinearLayoutManager(this)
        binding.rvReminders.adapter = adapter
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                showCompleted = tab?.position == 1
                filterReminders()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun observeReminders() {
        viewModel.allReminders.observe(this) { reminders ->
            currentReminders = reminders
            filterReminders()
        }
    }

    private fun filterReminders() {
        val filteredList = currentReminders.filter { it.isCompleted == showCompleted }
        adapter.submitList(filteredList)
        
        if (filteredList.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.rvReminders.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.rvReminders.visibility = View.VISIBLE
        }
    }

    private fun showAddReminderDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_reminder, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etReminderTitle)
        val btnDate = dialogView.findViewById<Button>(R.id.btnPickDate)
        val btnTime = dialogView.findViewById<Button>(R.id.btnPickTime)
        val cbHighPriority = dialogView.findViewById<MaterialCheckBox>(R.id.cbHighPriority)
        
        selectedDate = Calendar.getInstance()
        selectedTime = Calendar.getInstance()
        
        val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
        
        btnDate.text = getString(R.string.today)
        btnTime.text = sdfTime.format(selectedTime.time)

        btnDate.setOnClickListener {
            val datePicker = DatePickerDialog(this, { _, year, month, dayOfMonth ->
                selectedDate.set(year, month, dayOfMonth)
                btnDate.text = sdfDate.format(selectedDate.time)
            }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH))
            datePicker.show()
        }

        btnTime.setOnClickListener {
            val timePicker = TimePickerDialog(this, { _, hourOfDay, minute ->
                selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedTime.set(Calendar.MINUTE, minute)
                btnTime.text = sdfTime.format(selectedTime.time)
            }, selectedTime.get(Calendar.HOUR_OF_DAY), selectedTime.get(Calendar.MINUTE), true)
            timePicker.show()
        }
        
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.dialog_new_reminder))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.btn_add)) { _, _ ->
                val title = etTitle.text.toString().trim()
                if (title.isNotEmpty()) {
                    val dbDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val dbTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    
                    val reminder = Reminder(
                        title = title,
                        dueDate = dbDateFormat.format(selectedDate.time),
                        dueTime = dbTimeFormat.format(selectedTime.time),
                        priority = if (cbHighPriority.isChecked) 1 else 0,
                        category = "General"
                    )
                    viewModel.insert(reminder)
                } else {
                    Toast.makeText(this, getString(R.string.toast_task_empty), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }

    private fun showDeleteConfirmation(reminder: Reminder) {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.dialog_delete_reminder))
            .setMessage(getString(R.string.msg_delete_reminder_confirm))
            .setPositiveButton(getString(R.string.btn_delete)) { _, _ ->
                viewModel.delete(reminder)
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }
}
