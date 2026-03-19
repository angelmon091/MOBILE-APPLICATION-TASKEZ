package com.example.proyectofinal

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectofinal.databinding.ItemReminderBinding

class ReminderAdapter(
    private val onToggleComplete: (Reminder) -> Unit,
    private val onDelete: (Reminder) -> Unit,
    private val onClick: (Reminder) -> Unit
) : ListAdapter<Reminder, ReminderAdapter.ReminderViewHolder>(ReminderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val binding = ItemReminderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ReminderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReminderViewHolder(private val binding: ItemReminderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reminder: Reminder) {
            binding.tvReminderTitle.text = reminder.title
            binding.tvReminderDateTime.text = "${reminder.dueDate} • ${reminder.dueTime}"
            binding.tvReminderCategory.text = reminder.category
            binding.checkBoxDone.isChecked = reminder.isCompleted
            
            binding.ivPriority.visibility = if (reminder.priority > 0) View.VISIBLE else View.GONE

            if (reminder.isCompleted) {
                binding.tvReminderTitle.paintFlags = binding.tvReminderTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.root.alpha = 0.6f
            } else {
                binding.tvReminderTitle.paintFlags = binding.tvReminderTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                binding.root.alpha = 1.0f
            }

            // Remove listener before setting check state to avoid recursion if we had an observer
            binding.checkBoxDone.setOnCheckedChangeListener(null)
            binding.checkBoxDone.isChecked = reminder.isCompleted

            binding.checkBoxDone.setOnClickListener {
                onToggleComplete(reminder)
            }

            binding.root.setOnClickListener {
                onClick(reminder)
            }

            binding.root.setOnLongClickListener {
                onDelete(reminder)
                true
            }
        }
    }

    class ReminderDiffCallback : DiffUtil.ItemCallback<Reminder>() {
        override fun areItemsTheSame(oldItem: Reminder, newItem: Reminder): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Reminder, newItem: Reminder): Boolean = oldItem == newItem
    }
}
