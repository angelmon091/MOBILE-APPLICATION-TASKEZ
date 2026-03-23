package com.example.proyectofinal

import android.content.Context
import android.util.Log
import androidx.work.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    fun schedule(context: Context, reminder: Reminder) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        try {
            val dateObj = sdf.parse("${reminder.dueDate} ${reminder.dueTime}")
            if (dateObj != null) {
                var triggerTime = dateObj.time
                val now = System.currentTimeMillis()
                
                if (triggerTime <= now && reminder.repeatType != "None") {
                    triggerTime = getNextOccurrence(triggerTime, reminder.repeatType)
                }

                if (triggerTime > now) {
                    val delay = triggerTime - now
                    
                    val data = Data.Builder()
                        .putInt("REMINDER_ID", reminder.id)
                        .putString("REMINDER_TITLE", reminder.title)
                        .putString("REMINDER_DESCRIPTION", reminder.description)
                        .putString("REMINDER_CATEGORY", reminder.category)
                        .build()

                    val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .setInputData(data)
                        .addTag("reminder_${reminder.id}")
                        .build()

                    WorkManager.getInstance(context).enqueueUniqueWork(
                        "reminder_${reminder.id}",
                        ExistingWorkPolicy.REPLACE,
                        workRequest
                    )
                    
                    Log.d("Reminders", "WorkManager programado para ID: ${reminder.id} con delay de ${delay/1000}s")
                }
            }
        } catch (e: Exception) {
            Log.e("Reminders", "Error en scheduler: ${e.message}")
        }
    }

    private fun getNextOccurrence(startTime: Long, repeatType: String): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = startTime
        val now = Calendar.getInstance()
        
        while (calendar.before(now)) {
            when (repeatType) {
                "Daily" -> calendar.add(Calendar.DAY_OF_YEAR, 1)
                "Weekly" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
                "Monthly" -> calendar.add(Calendar.MONTH, 1)
                else -> return startTime
            }
        }
        return calendar.timeInMillis
    }

    fun cancel(context: Context, reminderId: Int) {
        WorkManager.getInstance(context).cancelUniqueWork("reminder_$reminderId")
    }
}
